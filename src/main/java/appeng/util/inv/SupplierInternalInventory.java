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

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import appeng.api.inventories.InternalInventory;

/**
 * Wraps another {@link IItemHandler} in such a way that the underlying item handler is queried from a supplier, which
 * allows it to be changed at any time.
 */
public class SupplierInternalInventory<T extends InternalInventory> implements InternalInventory {
    private final Supplier<T> delegate;

    public SupplierInternalInventory(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    protected final T getDelegate() {
        return this.delegate.get();
    }

    @Override
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    @Override
    public IItemHandler toItemHandler() {
        return getDelegate().toItemHandler();
    }

    @Override
    public Container toContainer() {
        return getDelegate().toContainer();
    }

    @Override
    public int size() {
        return getDelegate().size();
    }

    @Override
    public int getSlotLimit(int slot) {
        return getDelegate().getSlotLimit(slot);
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return getDelegate().getStackInSlot(slotIndex);
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        getDelegate().setItemDirect(slotIndex, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return getDelegate().isItemValid(slot, stack);
    }

    @Override
    public InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        return getDelegate().getSubInventory(fromSlotInclusive, toSlotExclusive);
    }

    @Override
    public InternalInventory getSlotInv(int slotIndex) {
        return getDelegate().getSlotInv(slotIndex);
    }

    @Override
    public int getRedstoneSignal() {
        return getDelegate().getRedstoneSignal();
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return getDelegate().iterator();
    }

    @Override
    public ItemStack addItems(ItemStack stack) {
        return getDelegate().addItems(stack);
    }

    @Override
    public ItemStack addItems(ItemStack stack, boolean simulate) {
        return getDelegate().addItems(stack, simulate);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return getDelegate().insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return getDelegate().extractItem(slot, amount, simulate);
    }

    @Override
    public void sendChangeNotification(int slot) {
        getDelegate().sendChangeNotification(slot);
    }
}
