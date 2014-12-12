package appeng.api.implementations.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Implemented on a {@link Item}
 */
public interface IStorageComponent
{

	/**
	 * This isn't necessarily the same as if you make a storage cell out of it,
	 * but all of AE's default cells do it that way, its currently only used for
	 * the condenser.
	 * 
	 * @param is item
	 * @return number of bytes
	 */
	int getBytes(ItemStack is);

	/**
	 * Just true or false for the item stack.
	 * 
	 * @param is item
	 * @return true if item is a storage component
	 */
	boolean isStorageComponent(ItemStack is);

}
