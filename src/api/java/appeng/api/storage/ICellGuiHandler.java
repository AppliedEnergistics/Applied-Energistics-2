
package appeng.api.storage;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.data.IAEStack;


public interface ICellGuiHandler
{
	/**
	 * Return true if this handler can show GUI for this channel.
	 * 
	 * @param channel Storage channel
	 * @return True if handled, else false.
	 */
	<T extends IAEStack<T>> boolean isHandlerFor( IStorageChannel<T> channel );

	/**
	 * Return true to prioritize this handler for the provided {@link ItemStack}.
	 * 
	 * @param is Cell ItemStack
	 * @return True, if specialized else false.
	 */
	default boolean isSpecializedFor( ItemStack is )
	{
		return false;
	}

	/**
	 * Called when the storage cell is placed in an ME Chest and the user tries to open the terminal side, if your item
	 * is not available via ME Chests simply tell the user they can't use it, or something, other wise you should open
	 * your gui and display the cell to the user.
	 *
	 * @param player player opening chest gui
	 * @param chest to be opened chest
	 * @param cellHandler cell handler
	 * @param inv inventory handler
	 * @param is item
	 * @param chan storage channel
	 */
	<T extends IAEStack<T>> void openChestGui( EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler<T> inv, ItemStack is, IStorageChannel<T> chan );

}
