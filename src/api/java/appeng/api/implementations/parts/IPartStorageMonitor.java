package appeng.api.implementations.parts;

import appeng.api.networking.IGridHost;
import appeng.api.parts.IPart;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.INetworkToolAgent;

/**
 * The Storage monitor is a {@link IPart} located on the sides of a IPartHost
 */
public interface IPartStorageMonitor extends IPartMonitor, IPart, IGridHost, INetworkToolAgent
{

	/**
	 * @return the item being displayed on the storage monitor, in AEStack Form, can be either a IAEItemStack or an
	 *         IAEFluidStack the quantity is important remember to use getStackSize() on the IAEStack, and not on the
	 *         FluidStack/ItemStack acquired from it.
	 */
	IAEStack getDisplayed();

	/**
	 * @return the current locked state of the Storage Monitor
	 */
	boolean isLocked();

}