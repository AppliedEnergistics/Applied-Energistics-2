/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.items.storage;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.IPartitionList;
import appeng.util.prioritylist.MergedPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

public class ViewCellItem extends AEBaseItem implements ICellWorkbenchItem {
    public ViewCellItem(Properties properties) {
        super(properties);
    }

    /**
     * Creates a filter from the given list of {@link ViewCellItem view cells}. Only item keys will be considered to be
     * part of the filter.
     */
    public static IPartitionList createItemFilter(Collection<ItemStack> list) {
        return createFilter(AEItemKey.filter(), list);
    }

    public static IPartitionList createFilter(AEKeyFilter filter,
            Collection<ItemStack> list) {
        IPartitionList myPartitionList = null;

        final MergedPriorityList myMergedList = new MergedPriorityList();

        for (var currentViewCell : list) {
            if (currentViewCell == null) {
                continue;
            }

            if (currentViewCell.getItem() instanceof ViewCellItem) {
                var priorityList = new KeyCounter();

                var vc = (ICellWorkbenchItem) currentViewCell.getItem();
                var upgrades = vc.getUpgradesInventory(currentViewCell);
                var config = vc.getConfigInventory(currentViewCell);
                var fzMode = vc.getFuzzyMode(currentViewCell);

                boolean hasInverter = false;
                boolean hasFuzzy = false;

                if (upgrades != null) {
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
                }

                for (int i = 0; i < config.size(); i++) {
                    var what = config.getKey(i);
                    if (what != null && filter.matches(what)) {
                        priorityList.add(what, 1);
                    }
                }

                if (!priorityList.isEmpty()) {
                    if (hasFuzzy) {
                        myMergedList.addNewList(new FuzzyPriorityList(priorityList, fzMode), !hasInverter);
                    } else {
                        myMergedList.addNewList(new PrecisePriorityList(priorityList), !hasInverter);
                    }

                    myPartitionList = myMergedList;
                }
            }
        }

        return myPartitionList;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public UpgradeInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }
}
