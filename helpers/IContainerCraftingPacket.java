package appeng.helpers;

import net.minecraft.inventory.IInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;

public interface IContainerCraftingPacket
{

	/**
	 * @return gain access to network infrastructure.
	 */
	IGridNode getNetworkNode();

	/**
	 * @param string
	 * @return the inventory of the part/tile by name.
	 */
	IInventory getInventoryByName(String string);

	/**
	 * @return who are we?
	 */
	BaseActionSource getSource();

	/**
	 * @return consume items?
	 */
	boolean useRealItems();

}
