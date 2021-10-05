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

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.inventories.InternalInventory;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStackList;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.IPartitionList;
import appeng.util.prioritylist.MergedPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

public class ViewCellItem extends AEBaseItem implements ICellWorkbenchItem {

    public ViewCellItem(Item.Properties properties) {
        super(properties);
    }

    public static IPartitionList<IAEItemStack> createFilter(Collection<ItemStack> list) {
        IPartitionList<IAEItemStack> myPartitionList = null;

        final MergedPriorityList<IAEItemStack> myMergedList = new MergedPriorityList<>();

        for (final ItemStack currentViewCell : list) {
            if (currentViewCell == null) {
                continue;
            }

            if (currentViewCell.getItem() instanceof ViewCellItem) {
                final IAEStackList<IAEItemStack> priorityList = StorageChannels.items().createList();

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

                for (var stack : config) {
                    priorityList.add(AEItemStack.fromItemStack(stack));
                }

                if (!priorityList.isEmpty()) {
                    if (hasFuzzy) {
                        myMergedList.addNewList(new FuzzyPriorityList<>(priorityList, fzMode), !hasInverter);
                    } else {
                        myMergedList.addNewList(new PrecisePriorityList<>(priorityList), !hasInverter);
                    }

                    myPartitionList = myMergedList;
                }
            }
        }

        return myPartitionList;
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public UpgradeInventory getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public InternalInventory getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
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
