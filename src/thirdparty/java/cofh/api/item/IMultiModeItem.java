package cofh.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Implement this interface on Item classes which have multiple modes - what that means is completely up to you. This just provides a uniform way of dealing
 * with them.
 *
 * @author King Lemming
 */
public interface IMultiModeItem {

	/**
	 * Get the current mode of an item.
	 */
	int getMode(ItemStack stack);

	/**
	 * Attempt to set the empowered state of the item.
	 *
	 * @param stack ItemStack to set the mode on.
	 * @param mode  Desired mode.
	 * @return TRUE if the operation was successful, FALSE if it was not.
	 */
	boolean setMode(ItemStack stack, int mode);

	/**
	 * Increment the current mode of an item.
	 */
	boolean incrMode(ItemStack stack);

	/**
	 * Decrement the current mode of an item.
	 */
	boolean decrMode(ItemStack stack);

	/**
	 * Returns the number of possible modes.
	 */
	int getNumModes(ItemStack stack);

	/**
	 * Callback method for reacting to a state change. Useful in KeyBinding handlers.
	 *
	 * @param player Player holding the item, if applicable.
	 * @param stack  The item being held.
	 */
	void onModeChange(EntityPlayer player, ItemStack stack);

}
