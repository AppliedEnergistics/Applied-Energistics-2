package appeng.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;

public interface IContainerCraftingPacket
{

	/**
	 * @return gain access to network infrastructure.
	 */
	IGridNode getNetworkNode();

	/**
	 * @param string name of inventory
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

	/**
	 * @return array of view cells
	 */
	ItemStack[] getViewCells();

}
