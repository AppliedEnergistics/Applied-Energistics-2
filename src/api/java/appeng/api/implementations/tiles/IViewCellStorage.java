package appeng.api.implementations.tiles;

import net.minecraft.inventory.IInventory;

public interface IViewCellStorage
{

	/**
	 * should contains at least 5 slot, the first 5
	 * 
	 * @return inventory with at least 5 slot
	 */
	IInventory getViewCellStorage();

}
