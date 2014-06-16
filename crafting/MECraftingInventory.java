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

	final MECraftingInventory par;

	final IMEInventory<IAEItemStack> target;
	final IItemList<IAEItemStack> localCache;

	final boolean logExtracted;
	final IItemList<IAEItemStack> extractedCache;

	final boolean logInjections;
	final IItemList<IAEItemStack> injectedCache;

	final boolean logMissing;
	final IItemList<IAEItemStack> missingCache;

	public MECraftingInventory(MECraftingInventory parrent) {
		this.target = parrent;
		this.logExtracted = parrent.logExtracted;
		this.logInjections = parrent.logInjections;
		this.logMissing = parrent.logMissing;

		missingCache = AEApi.instance().storage().createItemList();
		extractedCache = AEApi.instance().storage().createItemList();
		injectedCache = AEApi.instance().storage().createItemList();
		localCache = target.getAvailableItems( AEApi.instance().storage().createItemList() );

		par = parrent;
	}

	public MECraftingInventory(IMEInventory<IAEItemStack> target, boolean logExtracted, boolean logInjections, boolean logMissing) {
		this.target = target;
		this.logExtracted = logExtracted;
		this.logInjections = logInjections;
		this.logMissing = logMissing;
		missingCache = AEApi.instance().storage().createItemList();
		extractedCache = AEApi.instance().storage().createItemList();
		injectedCache = AEApi.instance().storage().createItemList();
		localCache = target.getAvailableItems( AEApi.instance().storage().createItemList() );
		par = null;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		if ( input == null )
			return null;

		// AELog.info( mode.toString() + "Inject: " + input.toString() );

		if ( mode == Actionable.MODULATE )
		{
			if ( logInjections )
				injectedCache.add( input );
			localCache.add( input );
		}

		return null;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		if ( request == null )
			return null;

		// AELog.info( mode.toString() + "Extract: " + request.toString() );

		IAEItemStack list = localCache.findPrecise( request );
		if ( list == null || list.getStackSize() == 0 )
			return null;

		if ( mode == Actionable.MODULATE && logExtracted )
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

	public void commit(BaseActionSource src)
	{
		if ( logInjections )
		{
			for (IAEItemStack injec : injectedCache)
				target.injectItems( injec, Actionable.MODULATE, src );
		}

		if ( logExtracted )
		{
			for (IAEItemStack extra : extractedCache)
				target.extractItems( extra, Actionable.MODULATE, src );
		}

		if ( logMissing && par != null )
		{
			for (IAEItemStack extra : missingCache)
				par.addMissing( extra );
		}
	}

	public void addMissing(IAEItemStack extra)
	{
		missingCache.add( extra );
	}

	public void ignore(IAEItemStack what)
	{

	}
}
