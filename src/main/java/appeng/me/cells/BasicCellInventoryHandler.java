/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.me.cells;

import appeng.api.config.IncludeExclude;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

/**
 * Adapts a {@link BasicCellInventory} such that its upgrades and filters are applied.
 */
public class BasicCellInventoryHandler<T extends IAEStack> extends MEInventoryHandler<T>
        implements ICellInventoryHandler<T> {

    private final BasicCellInventory<T> cellInventory;

    BasicCellInventoryHandler(BasicCellInventory<T> cellInventory, IStorageChannel<T> channel) {
        super(cellInventory, channel);
        this.cellInventory = cellInventory;

        var priorityList = channel.createList();

        var upgrades = cellInventory.getUpgradesInventory();
        var config = cellInventory.getConfigInventory();
        var fzMode = cellInventory.getFuzzyMode();

        boolean hasInverter = false;
        boolean hasFuzzy = false;

        for (var upgrade : upgrades) {
            var u = IUpgradeModule.getTypeFromStack(upgrade);
            if (u != null) {
                switch (u) {
                    case FUZZY -> hasFuzzy = true;
                    case INVERTER -> hasInverter = true;
                    default -> {
                    }
                }
            }
        }

        for (var stack : config) {
            T configItem = channel.createStack(stack);
            if (configItem != null) {
                // The config inventories stack size is meaningless, but stacks of size 0
                // are ignored in the item list.
                configItem.setStackSize(1);
                priorityList.add(configItem);
            }
        }

        this.setWhitelist(hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);

        if (!priorityList.isEmpty()) {
            if (hasFuzzy) {
                this.setPartitionList(new FuzzyPriorityList<>(priorityList, fzMode));
            } else {
                this.setPartitionList(new PrecisePriorityList<>(priorityList));
            }
        }
    }

    public boolean isPreformatted() {
        return !this.getPartitionList().isEmpty();
    }

    public boolean isFuzzy() {
        return this.getPartitionList() instanceof FuzzyPriorityList;
    }

    public IncludeExclude getIncludeExcludeMode() {
        return this.getWhitelist();
    }

    /**
     * @return The number of bytes currently in use on the underlying storage cell.
     */
    public long getUsedBytes() {
        return cellInventory.getUsedBytes();
    }

    /**
     * @return The number of different types currently stored on the underlying storage cell.
     */
    public long getStoredItemTypes() {
        return cellInventory.getStoredItemTypes();
    }

    public void persist() {
        cellInventory.persist();
    }

    public CellState getStatus() {
        return cellInventory.getStatusForCell();
    }

    public double getIdleDrain() {
        return cellInventory.getIdleDrain();
    }

}
