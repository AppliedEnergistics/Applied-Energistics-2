package appeng.crafting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cache.CraftingCache;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class CraftingJob implements ICraftingParent
{

	IAEItemStack output;

	IItemList<IAEItemStack> storage;

	HashSet<IAEItemStack> prophecies;

	Multimap<IAEItemStack, CraftingTask> bottom;
	CraftingTask top;

	ICraftingHost jobHost;

	public CraftingJob(ICraftingHost host, NBTTagCompound data) {
		jobHost = host;
		storage = AEApi.instance().storage().createItemList();
		prophecies = new HashSet();
		bottom = ArrayListMultimap.create();
	}

	public CraftingJob(ICraftingHost host, IAEItemStack what, Actionable mode) throws CraftingMissingItemsException {
		jobHost = host;
		output = what.copy();
		storage = AEApi.instance().storage().createItemList();
		prophecies = new HashSet();
		bottom = ArrayListMultimap.create();

		CraftingCache cc = host.getGrid().getCache( CraftingCache.class );
		IStorageGrid sg = host.getGrid().getCache( IStorageGrid.class );
		

		IItemList<IAEItemStack> available = AEApi.instance().storage().createItemList();
		IItemList<IAEItemStack> missing = AEApi.instance().storage().createItemList();
		
		calculateCrafting( cc, this, sg.getItemInventory().getAvailableItems( available ), missing, what, mode );
		
		if ( ! missing.isEmpty() )
		{
			if ( mode == Actionable.MODULATE )
			{
				IMEInventory<IAEItemStack> netStorage = sg.getItemInventory();
				
				Iterator<IAEItemStack> i = storage.iterator();
				while ( i.hasNext() )
				{
					IAEItemStack item = i.next();
					netStorage.injectItems( item, mode, host.getActionSrc() );
				}
			}
			
			throw new CraftingMissingItemsException( missing );
		}
	}

	public void calculateCrafting( CraftingCache cc, ICraftingParent parent, IItemList<IAEItemStack> available, IItemList<IAEItemStack> missing, IAEItemStack what, Actionable mode) {
		
	}

	public Collection<CraftingTask> getBottom()
	{
		return bottom.values();
	}

	public IAEItemStack addProgress(IAEItemStack progress)
	{
		Collection<CraftingTask> task = bottom.get( progress );

		if ( task == null )
			return progress;

		Iterator<CraftingTask> i = task.iterator();

		while (i.hasNext())
		{
			// adding to the first item..
			progress = i.next().addProgress( progress );

			// if nothing remains return
			if ( progress == null )
				return null;

			// if something remains continue..
			i = task.iterator();
		}

		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound out)
	{

	}

	@Override
	public void removeChild(CraftingTask craftingTask)
	{
		// TODO Auto-generated method stub
	}

	public ICraftingHost getHost()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends ICraftingParent> getSubJobs()
	{
		return Collections.singleton( top );
	}

}
