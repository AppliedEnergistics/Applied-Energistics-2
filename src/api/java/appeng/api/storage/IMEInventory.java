package appeng.api.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

/**
 * AE's Equivalent to IInventory, used to reading contents, and manipulating contents of ME Inventories.
 * 
 * Implementations should COMPLETELY ignore stack size limits from an external view point, Meaning that you can inject
 * Integer.MAX_VALUE items and it should work as defined, or be able to extract Integer.MAX_VALUE and have it work as
 * defined, Translations to MC's max stack size are external to the AE API.
 * 
 * If you want to request a stack of an item, you should should determine that prior to requesting the stack from the
 * inventory.
 */
public interface IMEInventory<StackType extends IAEStack>
{

	/**
	 * Store new items, or simulate the addition of new items into the ME Inventory.
	 * 
	 * @param input item to add.
	 * @param type action type
	 * @param src action source
	 * @return returns the number of items not added.
	 */
	public StackType injectItems(StackType input, Actionable type, BaseActionSource src);

	/**
	 * Extract the specified item from the ME Inventory
	 * 
	 * @param request
	 *            item to request ( with stack size. )
	 * @param mode
	 *            simulate, or perform action?
	 * @return returns the number of items extracted, null
	 */
	public StackType extractItems(StackType request, Actionable mode, BaseActionSource src);

	/**
	 * request a full report of all available items, storage.
	 * 
	 * @param out
	 *            the IItemList the results will be written too
	 * @return returns same list that was passed in, is passed out
	 */
	public IItemList<StackType> getAvailableItems(IItemList<StackType> out);

	/**
	 * @return the type of channel your handler should be part of
	 */
	public StorageChannel getChannel();

}
