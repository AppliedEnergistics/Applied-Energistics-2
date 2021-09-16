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

package appeng.me.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-23
 * @since rv6 2018-01-23
 */
public class BasicCellInventoryHandler<T extends IAEStack> extends MEInventoryHandler<T>
        implements ICellInventoryHandler<T> {
    public BasicCellInventoryHandler(final IMEInventory c, final IStorageChannel<T> channel) {
        super(c, channel);

        var ci = this.getCellInv();
        if (ci != null) {
            final IItemList<T> priorityList = channel.createList();

            var upgrades = ci.getUpgradesInventory();
            var config = ci.getConfigInventory();
            final FuzzyMode fzMode = ci.getFuzzyMode();

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

            for (ItemStack stack : config) {
                T configItem = channel.createStack(stack);
                if (configItem != null) {
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
    }

    @Override
    public ICellInventory getCellInv() {
        Object o = this.getInternal();

        if (o instanceof MEPassThrough) {
            o = ((MEPassThrough) o).getInternal();
        }

        return (ICellInventory) (o instanceof ICellInventory ? o : null);
    }

    @Override
    public boolean isPreformatted() {
        return !this.getPartitionList().isEmpty();
    }

    @Override
    public boolean isFuzzy() {
        return this.getPartitionList() instanceof FuzzyPriorityList;
    }

    @Override
    public IncludeExclude getIncludeExcludeMode() {
        return this.getWhitelist();
    }

    CompoundTag openNbtData() {
        return this.getCellInv().getItemStack().getOrCreateTag();
    }
}
