package appeng.api.storage;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.security.BaseActionSource;

/**
 * A Registration Record for {@link IExternalStorageRegistry}
 */
public interface IExternalStorageHandler
{

	/**
	 * if this can handle the provided inventory, return true. ( Generally skipped by AE, and it just calls getInventory
	 * )
	 * 
	 * @param te to be handled tile entity
	 * @param mySrc source
	 * @return true, if it can get a handler via getInventory
	 */
	boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc);

	/**
	 * if this can handle the given inventory, return the a IMEInventory implementing class for it, if not return null
	 * 
	 * please note that if your inventory changes and requires polling, you must use an {@link IMEMonitor} instead of an
	 * {@link IMEInventory} failure to do so will result in invalid item counts and reporting of the inventory.
	 * 
	 * @param te to be handled tile entity
	 * @param d direction
	 * @param channel channel
	 * @param src source
	 * @return The Handler for the inventory
	 */
	IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src);

}