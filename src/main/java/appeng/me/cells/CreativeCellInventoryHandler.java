package appeng.me.cells;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.me.storage.MEInventoryHandler;

class CreativeCellInventoryHandler<T extends IAEStack> extends MEInventoryHandler<T>
        implements ICellInventoryHandler<T> {
    public CreativeCellInventoryHandler(CreativeCellInventory<T> c, IStorageChannel<T> channel) {
        super(c);
    }

    @Override
    public CellState getStatus() {
        return CellState.TYPES_FULL;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public void persist() {
    }
}
