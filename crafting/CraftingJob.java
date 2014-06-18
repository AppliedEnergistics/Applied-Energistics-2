package appeng.crafting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.me.cache.CraftingCache;
import appeng.util.Platform;

import com.google.common.base.Stopwatch;

public class CraftingJob implements Runnable
{

	IAEItemStack output;

	IItemList<IAEItemStack> storage;

	HashSet<IAEItemStack> prophecies;

	ICraftingHost jobHost;

	boolean simulate = false;
	final MECraftingInventory original;

	public CraftingTreeNode tree;
	private BaseActionSource actionSrc;

	public CraftingJob(ICraftingHost host, NBTTagCompound data) {
		jobHost = host;
		storage = AEApi.instance().storage().createItemList();
		prophecies = new HashSet();
		original = null;
	}

	public CraftingJob(ICraftingHost host, IAEItemStack what, Actionable mode) {
		jobHost = host;
		output = what.copy();
		storage = AEApi.instance().storage().createItemList();
		prophecies = new HashSet();
		actionSrc = host.getActionSrc();

		CraftingCache cc = host.getGrid().getCache( CraftingCache.class );
		IStorageGrid sg = host.getGrid().getCache( IStorageGrid.class );
		original = new MECraftingInventory( sg.getItemInventory(), false, false, false );

		tree = getCraftingTree( cc, what );
	}

	private CraftingTreeNode getCraftingTree(CraftingCache cc, IAEItemStack what)
	{
		return new CraftingTreeNode( cc, this, what, null, -1, 0 );
	}

	public void writeToNBT(NBTTagCompound out)
	{

	}

	IItemList<IAEItemStack> crafting = AEApi.instance().storage().createItemList();
	IItemList<IAEItemStack> missing = AEApi.instance().storage().createItemList();

	public void addTask(IAEItemStack what, long crafts, ICraftingPatternDetails details, int depth)
	{
		if ( crafts > 0 )
		{
			postOp( "new task: " + Platform.getItemDisplayName( what ) + " x " + what.getStackSize(), what.getStackSize(), crafts );
			what = what.copy();
			what.setStackSize( what.getStackSize() * crafts );
			crafting.add( what );
		}
	}

	public void addMissing(IAEItemStack what)
	{
		what = what.copy();
		missing.add( what );

		postOp( "required material: " + Platform.getItemDisplayName( what ), 1, what.getStackSize() );
	}

	class twoIntegers
	{

		public long perOp = 0;
		public long times = 0;
	};

	HashMap<String, twoIntegers> opsAndMultiplier = new HashMap();

	private void postOp(String string, long stackSize, long crafts)
	{
		twoIntegers ti = opsAndMultiplier.get( string );
		if ( ti == null )
			opsAndMultiplier.put( string, ti = new twoIntegers() );

		ti.perOp = stackSize;
		ti.times += crafts;
	}

	@Override
	public void run()
	{

		try
		{
			Stopwatch timer = Stopwatch.createStarted();

			MECraftingInventory meci = new MECraftingInventory( original, true, false, true );
			meci.ignore( output );

			tree.request( meci, output.getStackSize(), actionSrc );
			tree.dive( this );

			for (String s : opsAndMultiplier.keySet())
			{
				twoIntegers ti = opsAndMultiplier.get( s );
				AELog.info( s + " * " + ti.times + " = " + (ti.perOp * ti.times) );
			}

			AELog.info( "------------- real" + timer.elapsed( TimeUnit.MILLISECONDS ) + "ms" );
			// if ( mode == Actionable.MODULATE )
			// meci.moveItemsToStorage( storage );
		}
		catch (CraftBranchFailure e)
		{
			simulate = true;

			try
			{
				Stopwatch timer = Stopwatch.createStarted();
				MECraftingInventory meci = new MECraftingInventory( original, true, false, true );
				meci.ignore( output );

				tree.setSimulate();
				tree.request( meci, output.getStackSize(), actionSrc );
				tree.dive( this );

				for (String s : opsAndMultiplier.keySet())
				{
					twoIntegers ti = opsAndMultiplier.get( s );
					AELog.info( s + " * " + ti.times + " = " + (ti.perOp * ti.times) );
				}

				AELog.info( "------------- simulate" + timer.elapsed( TimeUnit.MILLISECONDS ) + "ms" );
			}
			catch (CraftBranchFailure e1)
			{
				AELog.error( e1 );
			}
			catch (CraftingCalculationFailure f)
			{
				AELog.error( f );
			}
			catch (InterruptedException e1)
			{
				AELog.info( "Crafting calculation canceled." );
				return;
			}
		}
		catch (CraftingCalculationFailure f)
		{
			AELog.error( f );
		}
		catch (InterruptedException e1)
		{
			AELog.info( "Crafting calculation canceled." );
			return;
		}

	}
}
