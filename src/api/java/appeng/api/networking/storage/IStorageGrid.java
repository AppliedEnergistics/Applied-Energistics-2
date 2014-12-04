package appeng.api.networking.storage;

import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;

/**
 * Common base class for item / fluid storage caches.
 */
public interface IStorageGrid extends IGridCache, IStorageMonitorable
{

	/**
	 * Used to inform the network of alterations to the storage system that fall outside of the standard Network
	 * operations, Examples, ME Chest inputs from the world, or a Storage Bus detecting modifications made to the chest
	 * by an outside force.
	 * 
	 * Expects the input to have either a negative or a positive stack size to correspond to the injection, or
	 * extraction operation.
	 * 
	 * @param input injected items
	 */
	void postAlterationOfStoredItems(StorageChannel chan, Iterable<? extends IAEStack> input, BaseActionSource src);

	/**
	 * Used to add a cell provider to the storage system
	 * 
	 * THIS IT NOT FOR USE {@link IGridHost} THAT PROVIDE {@link ICellContainer} - those are automatically handled by
	 * the storage system.
	 * 
	 * @param cc to be added cell provider
	 */
	void registerCellProvider(ICellProvider cc);

	/**
	 * remove a provider added with addCellContainer
	 */
	void unregisterCellProvider(ICellProvider cc);

}
