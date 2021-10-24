package appeng.me.cells;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.me.storage.MEInventoryHandler;

class CreativeCellInventoryHandler<T extends IAEStack> extends MEInventoryHandler<T>
        implements ICellInventoryHandler<T> {
    public CreativeCellInventoryHandler(CreativeCellInventory<T> c, IStorageChannel<T> channel) {
        super(c, channel);
    }

    @Override
    public ICellInventory<T> getCellInv() {
        return null;
    }

    @Override
    public boolean isPreformatted() {
        return !this.getPartitionList().isEmpty();
    }

    @Override
    public boolean isFuzzy() {
        return false;
    }

    @Override
    public IncludeExclude getIncludeExcludeMode() {
        return this.getWhitelist();
    }
}
