package appeng.util.crafting.mock;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import com.google.common.base.Preconditions;

/**
 * Mocking a storage grid to test the crafting jobs. Only has an inventory.
 */
public class MockedStorageGrid implements IStorageGrid {
    private final MockedMEMonitor monitor = new MockedMEMonitor();

    public IItemList<IAEItemStack> getList() {
        return monitor.list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        Preconditions.checkArgument(channel instanceof IItemStorageChannel);
        return (IMEMonitor<T>) monitor;
    }

    // Everything below just throws an exception

    @Override
    public void postAlterationOfStoredItems(IStorageChannel<?> chan, Iterable<? extends IAEStack<?>> input, IActionSource src) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void registerCellProvider(ICellProvider cc) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void unregisterCellProvider(ICellProvider cc) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void onUpdateTick() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void removeNode(IGridNode gridNode, IGridHost machine) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void addNode(IGridNode gridNode, IGridHost machine) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void onSplit(IGridStorage destinationStorage) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void onJoin(IGridStorage sourceStorage) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void populateGridStorage(IGridStorage destinationStorage) {
        throw new UnsupportedOperationException("mock");
    }
}
