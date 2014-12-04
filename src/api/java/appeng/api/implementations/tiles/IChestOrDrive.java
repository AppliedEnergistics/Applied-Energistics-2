package appeng.api.implementations.tiles;

import appeng.api.networking.IGridHost;
import appeng.api.storage.ICellContainer;
import appeng.api.util.IOrientable;

public interface IChestOrDrive extends ICellContainer, IGridHost, IOrientable
{

	/**
	 * @return how many slots are available. Chest has 1, Drive has 10.
	 */
	int getCellCount();

	/**
	 * 0 - cell is missing.
	 * 
	 * 1 - green,
	 * 
	 * 2 - orange,
	 * 
	 * 3 - red
	 * 
	 * @param slot slot index
	 * @return status of the slot, one of the above indices.
	 */
	int getCellStatus(int slot);

	/**
	 * @return if the device is online you should check this before providing any other information.
	 */
	boolean isPowered();

	/**
	 * @param slot slot index
	 * @return is the cell currently blinking to show activity.
	 */
	boolean isCellBlinking(int slot);

}
