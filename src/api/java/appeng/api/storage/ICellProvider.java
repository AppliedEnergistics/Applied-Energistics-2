package appeng.api.storage;

import java.util.List;

/**
 * Allows you to provide cells via non IGridHosts directly to the storage system, drives, and similar features should go
 * though {@link ICellContainer} and be automatically handled by the storage system.
 */
public interface ICellProvider
{

	/**
	 * Inventory of the tile for use with ME, should always return an valid list, never NULL.
	 * 
	 * You must return the correct Handler for the correct channel, if your handler returns a IAEItemStack handler, for
	 * a Fluid Channel stuffs going to explode, same with the reverse.
	 * 
	 * @return a valid list of handlers, NEVER NULL
	 */
	List<IMEInventoryHandler> getCellArray(StorageChannel channel);

	/**
	 * the storage's priority.
	 * 
	 * Positive and negative are supported
	 */
	int getPriority();

}
