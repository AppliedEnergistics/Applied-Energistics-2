package appeng.crafting;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class CraftingMissingItemsException extends Exception {

	private static final long serialVersionUID = -7517510681369528425L;

	final public IItemList<IAEItemStack> missingItems;
	
	public CraftingMissingItemsException( IItemList<IAEItemStack> missing )
	{
		missingItems = missing;
	}
	
}
