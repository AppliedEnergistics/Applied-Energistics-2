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

package appeng.api.upgrades;

import java.util.Collections;
import java.util.Iterator;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyHandler;

final class EmptyUpgradeInventory implements IUpgradeInventory {
    public static final EmptyUpgradeInventory INSTANCE = new EmptyUpgradeInventory();

    @Override
    public ItemLike getUpgradableItem() {
        return Items.AIR;
    }

    @Override
    public boolean isInstalled(ItemLike upgradeCard) {
        return false;
    }

    @Override
    public int getInstalledUpgrades(ItemLike u) {
        return 0;
    }

    @Override
    public int getMaxInstalled(ItemLike u) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public IItemHandler toItemHandler() {
        return EmptyHandler.INSTANCE;
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
    public void setItemDirect(int slotIndex, ItemStack stack) {
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public void readFromNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
    }

    @Override
    public void writeToNBT(CompoundTag data, String subtag, HolderLookup.Provider registries) {
    }
}
