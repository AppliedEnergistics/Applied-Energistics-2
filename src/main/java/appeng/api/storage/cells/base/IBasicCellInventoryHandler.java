package appeng.api.storage.cells.base;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;

/**
 * Specialized version of {@link ICellInventoryHandler} for basic cells. Obtain instances through
 * {@link IBasicCellItem#getHandler}.
 */
public interface IBasicCellInventoryHandler<T extends IAEStack> extends ICellInventoryHandler<T> {
    IBasicCellInfo<T> getInfo();

    @Override
    default CellState getStatus() {
        return getInfo().getStatusForCell();
    }

    @Override
    default double getIdleDrain() {
        return getInfo().getIdleDrain();
    }

    @Override
    default void persist() {
        getInfo().persist();
    }

    boolean isPreformatted();

    boolean isFuzzy();

    IncludeExclude getIncludeExcludeMode();
}
