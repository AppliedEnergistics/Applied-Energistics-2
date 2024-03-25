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

package appeng.api.upgrades;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.util.inv.AppEngInternalInventory;

/**
 * Provides an upgrade inventory that stores the upgrades directly on an {@link ItemStack}, and derives which updates
 * are compatible from the item of that stack.
 */
final class ItemUpgradeInventory extends UpgradeInventory {
    private static final String TAG_UPGRADES = "upgrades";

    private final ItemStack stack;

    @Nullable
    private final ItemUpgradesChanged changeCallback;

    public ItemUpgradeInventory(ItemStack stack, int upgrades, @Nullable ItemUpgradesChanged changeCallback) {
        super(stack.getItem(), upgrades);
        this.stack = stack;
        this.changeCallback = changeCallback;
        this.readFromNBT(stack.getOrCreateTag(), TAG_UPGRADES);
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.writeToNBT(this.stack.getOrCreateTag(), TAG_UPGRADES);

        super.saveChangedInventory(inv);
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        super.onChangeInventory(inv, slot);

        if (changeCallback != null) {
            changeCallback.onUpgradesChanged(stack, this);
        }
    }
}
