package appeng.api.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.storage.data.IAEStack;

/**
 * Thin logic layer that can be swapped with different IMEInventory implementations, used to handle features related to
 * storage, that are Separate from the storage medium itself.
 * 
 * @param <StackType>
 */
public interface IMEInventoryHandler<StackType extends IAEStack> extends IMEInventory<StackType>
{

	/**
	 * determine if items can be injected/extracted.
	 * 
	 * @return the access
	 */
	public AccessRestriction getAccess();

	/**
	 * determine if a particular item is prioritized for this inventory handler, if it is, then it will be added to this
	 * inventory prior to any non-prioritized inventories.
	 * 
	 * @param input
	 *            - item that might be added
	 * @return if its prioritized
	 */
	public boolean isPrioritized(StackType input);

	/**
	 * determine if an item can be accepted and stored.
	 * 
	 * @param input
	 *            - item that might be added
	 * @return if the item can be added
	 */
	public boolean canAccept(StackType input);

	/**
	 * determine what the priority of the inventory is.
	 * 
	 * @return the priority, zero is default, positive and negative are supported.
	 */
	public int getPriority();

	/**
	 * pass back value for blinkCell.
	 * 
	 * @return the slot index for the cell that this represents in the storage unit, the method on the
	 *         {@link ICellContainer} will be called with this value, only trust the return value of this method if you
	 *         are the implementer of the {@link IMEInventoryHandler}.
	 */
	public int getSlot();

	/**
	 * AE Uses a two pass placement system, the first pass checks contents and tries to find a place where the item
	 * belongs, however in some cases you can save processor time, or require that the second, or first pass is simply
	 * ignored, this allows you to do that.
	 * 
	 * @param i
	 *            - pass number ( 1 or 2 )
	 * @return true, if this inventory is valid for this pass.
	 */
	public boolean validForPass(int i);
}
