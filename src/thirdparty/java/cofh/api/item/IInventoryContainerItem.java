package cofh.api.item;

import net.minecraft.item.ItemStack;

/**
 * Implement this interface on Item classes that are themselves inventories.
 *
 * @author King Lemming
 */
public interface IInventoryContainerItem {

	/**
	 * Get the size of this inventory of this container item.
	 */
	int getSizeInventory(ItemStack container);

}
