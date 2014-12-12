package appeng.api.implementations.guiobjects;

import net.minecraft.inventory.IInventory;
import appeng.api.networking.IGridHost;

/**
 * Obtained via {@link IGuiItem} getGuiObject
 */
public interface INetworkTool extends IInventory, IGuiItemObject
{

	IGridHost getGridHost(); // null for most purposes.

}
