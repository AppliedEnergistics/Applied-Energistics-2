package appeng.crafting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.me.cache.CraftingCache;
import appeng.util.Platform;

import com.google.common.base.Stopwatch;

public class CraftingJob
{

	IAEItemStack output;

	IItemList<IAEItemStack> storage;

	HashSet<IAEItemStack> prophecies;

	ICraftingHost jobHost;

	public CraftingJob(ICraftingHost host, NBTTagCompound data) {
		jobHost = host;
		storage = AEApi.instance().storage().createItemList();
		prophecies = new HashSet();
	}

	public CraftingJob(ICraftingHost host, IAEItemStack what, Actionable mode) {
		jobHost = host;
		output = what.copy();
		storage = AEApi.instance().storage().createItemList();
		prophecies = new HashSet();

		CraftingCache cc = host.getGrid().getCache( CraftingCache.class );
		IStorageGrid sg = host.getGrid().getCache( IStorageGrid.class );

		IItemList<IAEItemStack> missing = AEApi.instance().storage().createItemList();

		MECraftingInventory meci = new MECraftingInventory( sg.getItemInventory(), true, false, true );
		meci.ignore( what );

		CraftingTreeNode tree = getCraftingTree( cc, what );

		try
		{
			Stopwatch timer = Stopwatch.createStarted();
			tree.request( meci, what.getStackSize(), host.getActionSrc() );
			tree.dive( this );

			for (String s : opsAndMultiplier.keySet())
			{
				twoIntegers ti = opsAndMultiplier.get( s );
				AELog.info( s + " * " + ti.times + " = " + (ti.perOp * ti.times) );
			}

			AELog.info( "-------------" + timer.elapsed( TimeUnit.MILLISECONDS ) + "ms" );
			// if ( mode == Actionable.MODULATE )
			// meci.moveItemsToStorage( storage );
		}
		catch (CraftBranchFailure e)
		{
			AELog.error( e );
		}
		catch (CraftingCalculationFailure f)
		{
			AELog.error( f );
		}
	}

	private CraftingTreeNode getCraftingTree(CraftingCache cc, IAEItemStack what)
	{
		return new CraftingTreeNode( cc, this, what, null, -1, 0 );
	}

	public void writeToNBT(NBTTagCompound out)
	{

	}

	public void addTask(IAEItemStack what, long crafts, ICraftingPatternDetails details, int depth)
	{
		if ( crafts > 0 )
		{
			postOp( "new task: " + Platform.getItemDisplayName( what ) + " x " + what.getStackSize(), what.getStackSize(), crafts );
		}
	}

	public void addMissing(IAEItemStack what)
	{
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
}
