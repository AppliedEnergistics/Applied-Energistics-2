package appeng.crafting;

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
		return new CraftingTreeNode( cc, this, what, null, 0 );
	}

	public void writeToNBT(NBTTagCompound out)
	{

	}

	public void addTask(IAEItemStack what, int crafts, ICraftingPatternDetails details, int depth)
	{
		if ( crafts > 0 )
		{
			AELog.info( "new task: " + Platform.getItemDisplayName( what ) + " x " + what.getStackSize() + " * " + crafts + " = "
					+ (what.getStackSize() * crafts) + " @ " + depth );
		}
	}

	public void addMissing(IAEItemStack what)
	{
		AELog.info( "required material: " + Platform.getItemDisplayName( what ) + " x " + what.getStackSize() );
	}

}
