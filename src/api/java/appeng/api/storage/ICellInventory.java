package appeng.api.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;

public interface ICellInventory extends IMEInventory<IAEItemStack>
{

	/**
	 * @return the item stack of this storage cell.
	 */
	ItemStack getItemStack();

	/**
	 * @return idle cost for this Storage Cell
	 */
	double getIdleDrain();

	/**
	 * @return fuzzy setting
	 */
	FuzzyMode getFuzzyMode();

	/**
	 * @return access configured list
	 */
	IInventory getConfigInventory();

	/**
	 * @return access installed upgrades.
	 */
	IInventory getUpgradesInventory();

	/**
	 * @return How many bytes are used for each type?
	 */
	int getBytesPerType();

	/**
	 * @return true if a new item type can be added.
	 */
	boolean canHoldNewItem();

	/**
	 * @return total byte storage.
	 */
	long getTotalBytes();

	/**
	 * @return how many bytes are free.
	 */
	long getFreeBytes();

	/**
	 * @return how many bytes are in use.
	 */
	long getUsedBytes();

	/**
	 * @return max number of types.
	 */
	long getTotalItemTypes();

	/**
	 * @return how many items are stored.
	 */
	long getStoredItemCount();

	/**
	 * @return how many items types are currently stored.
	 */
	long getStoredItemTypes();

	/**
	 * @return how many item types remain.
	 */
	long getRemainingItemTypes();

	/**
	 * @return how many more items can be stored.
	 */
	long getRemainingItemCount();

	/**
	 * @return how many items can be added without consuming another byte.
	 */
	int getUnusedItemCount();

	/**
	 * @return the status number for this drive.
	 */
	int getStatusForCell();

}
