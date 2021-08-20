/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.automation;

import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;

public final class EmptyUpgradeInventory implements IUpgradeInventory {
    public static final EmptyUpgradeInventory INSTANCE = new EmptyUpgradeInventory();

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        return 0;
    }

    @Override
    public int getMaxInstalled(Upgrades u) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Storage<ItemVariant> toStorage() {
        return Storage.empty();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
    }

    @Nonnull
    @Override
    public Iterator<ItemStack> iterator() {
        return Collections.emptyIterator();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }
}
