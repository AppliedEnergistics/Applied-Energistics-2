package appeng.api.storage;

import appeng.api.networking.security.IActionHost;

/**
 * Represents a IGridhost that contributes to storage, such as a ME Chest, or ME Drive.
 */
public interface ICellContainer extends IActionHost, ICellProvider, ISaveProvider
{

	/**
	 * tell the Cell container that this slot should blink, the slot number is relative to the
	 * 
	 * @param slot slot index
	 */
	void blinkCell(int slot);

}
