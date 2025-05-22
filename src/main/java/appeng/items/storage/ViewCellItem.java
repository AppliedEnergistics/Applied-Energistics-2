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

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
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
                var config = vc.getConfigInventory(currentViewCell);
                var fzMode = vc.getFuzzyMode(currentViewCell);

                for (int i = 0; i < config.size(); i++) {
                    var what = config.getKey(i);
                    if (what != null && filter.matches(what)) {
                        priorityList.add(what, 1);
                    }
                }

                if (!priorityList.isEmpty()) {
                    var upgrades = vc.getUpgrades(currentViewCell);
                    var hasInverter = upgrades.isInstalled(AEItems.INVERTER_CARD);
                    if (upgrades.isInstalled(AEItems.FUZZY_CARD)) {
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
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }
}
