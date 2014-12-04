package appeng.api.storage;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public enum StorageChannel
{
	/**
	 * AE2's Default Storage.
	 */
	ITEMS( IAEItemStack.class ),

	/**
	 * AE2's Fluid Based Storage ( mainly added to better support ExtraCells )
	 */
	FLUIDS( IAEFluidStack.class );
	
	public final Class<? extends IAEStack> type;

	private StorageChannel( Class<? extends IAEStack> t ) {
		type = t;
	}

	public IItemList createList() {
		if ( this == ITEMS )
			return AEApi.instance().storage().createItemList();
		else
			return AEApi.instance().storage().createFluidList();
	}
}
