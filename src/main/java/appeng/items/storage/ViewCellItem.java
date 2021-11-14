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

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
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

    private final IStorageChannel<?> channel;

    public ViewCellItem(Properties properties, IStorageChannel<?> channel) {
        super(properties);
        this.channel = channel;
    }

    public static <T extends AEKey> IPartitionList<T> createFilter(IStorageChannel<T> channel,
            Collection<ItemStack> list) {
        IPartitionList<T> myPartitionList = null;

        final MergedPriorityList<T> myMergedList = new MergedPriorityList<>();

        for (final ItemStack currentViewCell : list) {
            if (currentViewCell == null) {
                continue;
            }

            if (currentViewCell.getItem() instanceof ViewCellItem) {
                var priorityList = new KeyCounter<T>();

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
                    var what = channel.tryCast(config.getKey(i));
                    if (what != null) {
                        priorityList.add(what, 1);
                    }
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
    public ConfigInventory<?> getConfigInventory(final ItemStack is) {
        return CellConfig.create(channel, is);
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

    @Nonnull
    @Override
    public IStorageChannel<?> getChannel() {
        return StorageChannels.items();
    }
}
