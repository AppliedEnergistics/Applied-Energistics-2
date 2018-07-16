
package appeng.api.storage;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.data.IAEStack;


public interface ICellGuiHandler
{
	/**
	 * return true if the provided storage channel is handled by your cell gui handler.
	 *
	 * @param channel the storage channel
	 *
	 * @return return true, if this handler provides a GUI for this channel.
	 */
	<T extends IAEStack<T>> boolean isHandlerFor( IStorageChannel<T> channel );

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
