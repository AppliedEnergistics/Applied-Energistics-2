package appeng.api.implementations.items;

import net.minecraft.item.ItemStack;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.data.IAEItemStack;

/**
 * Any item which implements this can be treated as an IMEInventory via
 * Util.getCell / Util.isCell It automatically handles the internals and NBT
 * data, which is both nice, and bad for you!
 * 
 * Good cause it means you don't have to do anything, bad because you have
 * little to no control over it.
 * 
 * The standard AE implementation only provides 1-63 Types
 * 
 */
public interface IStorageCell extends ICellWorkbenchItem
{

	/**
	 * If this returns something where N % 8 != 0 Then you will be shot on
	 * sight, or your car will explode, something like that least...
	 * 
	 * @param cellItem item
	 * @return number of bytes
	 */
	int getBytes(ItemStack cellItem);

	/**
	 * Determines the number of bytes used for any type included on the cell.
	 * 
	 * @param cellItem item
	 * @return number of bytes
	 */
	int BytePerType(ItemStack cellItem);

	/**
	 * Must be between 1 and 63, indicates how many types you want to store on
	 * the item.
	 * 
	 * @param cellItem item
	 * @return number of types
	 */
	int getTotalTypes(ItemStack cellItem);

	/**
	 * Allows you to fine tune which items are allowed on a given cell, if you
	 * don't care, just return false; As the handler for this type of cell is
	 * still the default cells, the normal AE black list is also applied.
	 * 
	 * @param cellItem item
	 * @param requestedAddition requested addition
	 * @return true to preventAdditionOfItem
	 */
	boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition);

	/**
	 * Allows you to specify if this storage cell can be stored inside other
	 * storage cells, only set this for special items like the matter cannon
	 * that are not general purpose storage.
	 * 
	 * @return true if the storage cell can be stored inside other storage
	 *         cells, this is generally false, except for certain situations
	 *         such as the matter cannon.
	 */
	boolean storableInStorageCell();

	/**
	 * Allows an item to selectively enable or disable its status as a storage
	 * cell.
	 * 
	 * @param i item
	 * @return if the ItemStack should behavior as a storage cell.
	 */
	boolean isStorageCell(ItemStack i);

	/**
	 * @return drain in ae/t this storage cell will use.
	 */
	double getIdleDrain();
}
