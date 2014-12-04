package appeng.api.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.FuzzyMode;

public interface ICellWorkbenchItem
{

	/**
	 * if this return false, the item will not be treated as a cell, and cannot be inserted into the work bench.
	 * 
	 * @param is item
	 * @return true if the item should be editable in the cell workbench.
	 */
	boolean isEditable(ItemStack is);

	/**
	 * used to edit the upgrade slots on your cell, should have a capacity of 0-24, you are also responsible for
	 * implementing the valid checks, and any storage/usage of them.
	 * 
	 * onInventoryChange will be called when saving is needed.
	 */
	IInventory getUpgradesInventory(ItemStack is);

	/**
	 * Used to extract, or mirror the contents of the work bench onto the cell.
	 * 
	 * - This should have exactly 63 slots, any more, or less might cause issues.
	 * 
	 * onInventoryChange will be called when saving is needed.
	 */
	IInventory getConfigInventory(ItemStack is);

	/**
	 * @return the current fuzzy status.
	 */
	FuzzyMode getFuzzyMode(ItemStack is);

	/**
	 * sets the setting on the cell.
	 */
	void setFuzzyMode(ItemStack is, FuzzyMode fzMode);

}
