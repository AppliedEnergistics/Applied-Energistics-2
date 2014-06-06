package appeng.crafting;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class MECraftingInventory implements IMEInventory<IAEItemStack>
{

	final IMEInventory<IAEItemStack> target;
	final IItemList<IAEItemStack> localCache;
	final IItemList<IAEItemStack> extractedCache;

	public MECraftingInventory(IMEInventory<IAEItemStack> target) {
		this.target = target;
		extractedCache = AEApi.instance().storage().createItemList();
		localCache = target.getAvailableItems( AEApi.instance().storage().createItemList() );
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		if ( input == null )
			return null;

		if ( mode == Actionable.SIMULATE )
			localCache.add( input );

		return null;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		if ( request == null )
			return null;

		IAEItemStack list = localCache.findPrecise( request );
		if ( list == null || list.getStackSize() == 0 )
			return null;

		if ( mode == Actionable.MODULATE )
			extractedCache.add( request );

		if ( list.getStackSize() >= request.getStackSize() )
		{
			if ( mode == Actionable.MODULATE )
				list.decStackSize( request.getStackSize() );

			return request;
		}

		IAEItemStack ret = request.copy();
		ret.setStackSize( list.getStackSize() );
		if ( mode == Actionable.MODULATE )
			list.reset();

		return ret;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out)
	{
		for (IAEItemStack is : localCache)
			out.add( is );

		return out;
	}

	public IItemList<IAEItemStack> getItemList()
	{
		return localCache;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	public void moveItemsToStorage(IItemList<IAEItemStack> storage)
	{
		// TODO Auto-generated method stub

	}

}
