/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.util.inv;

import java.util.Iterator;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;

/**
 * Wraps another {@link InternalInventory} in such a way that the underlying inventory is queried from a supplier, which
 * allows it to be changed at any time.
 */
public class SupplierInternalInventory implements InternalInventory {
    private final Supplier<InternalInventory> delegate;

    public SupplierInternalInventory(Supplier<InternalInventory> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isEmpty() {
        return delegate.get().isEmpty();
    }

    @Override
    public Storage<ItemVariant> toStorage() {
        return delegate.get().toStorage();
    }

    @Override
    public Container toContainer() {
        return delegate.get().toContainer();
    }

    @Override
    public int size() {
        return delegate.get().size();
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.get().getSlotLimit(slot);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return delegate.get().getStackInSlot(slotIndex);
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
        delegate.get().setItemDirect(slotIndex, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return delegate.get().isItemValid(slot, stack);
    }

    @Override
    public InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        return delegate.get().getSubInventory(fromSlotInclusive, toSlotExclusive);
    }

    @Override
    public InternalInventory getSlotInv(int slotIndex) {
        return delegate.get().getSlotInv(slotIndex);
    }

    @Override
    public int getRedstoneSignal() {
        return delegate.get().getRedstoneSignal();
    }

    @Override
    public @Nonnull Iterator<ItemStack> iterator() {
        return delegate.get().iterator();
    }

    @Override
    public ItemStack addItems(ItemStack stack) {
        return delegate.get().addItems(stack);
    }

    @Override
    public ItemStack addItems(ItemStack stack, boolean simulate) {
        return delegate.get().addItems(stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return delegate.get().insertItem(slot, stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return delegate.get().extractItem(slot, amount, simulate);
    }
}
