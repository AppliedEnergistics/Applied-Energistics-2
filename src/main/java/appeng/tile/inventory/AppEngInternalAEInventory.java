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

package appeng.tile.inventory;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import alexiil.mc.lib.attributes.ListenerRemovalToken;
import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.InvMarkDirtyListener;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import appeng.util.iterators.InvIterator;

public class AppEngInternalAEInventory implements FixedItemInv, Iterable<ItemStack> {
    private final IAEAppEngInventory te;
    private final IAEItemStack[] inv;
    private final int size;
    private int maxStack;
    private boolean dirtyFlag = false;

    public AppEngInternalAEInventory(final IAEAppEngInventory te, final int s) {
        this.te = te;
        this.size = s;
        this.maxStack = 64;
        this.inv = new IAEItemStack[s];
    }

    public void setMaxStackSize(final int s) {
        this.maxStack = s;
    }

    public IAEItemStack getAEStackInSlot(final int var1) {
        return this.inv[var1];
    }

    public void writeToNBT(final CompoundTag data, final String name) {
        final CompoundTag c = new CompoundTag();
        this.writeToNBT(c);
        data.put(name, c);
    }

    private void writeToNBT(final CompoundTag target) {
        for (int x = 0; x < this.size; x++) {
            try {
                final CompoundTag c = new CompoundTag();

                if (this.inv[x] != null) {
                    this.inv[x].writeToNBT(c);
                }

                target.put("#" + x, c);
            } catch (final Exception ignored) {
            }
        }
    }

    public void readFromNBT(final CompoundTag data, final String name) {
        final CompoundTag c = data.getCompound(name);
        if (c != null) {
            this.readFromNBT(c);
        }
    }

    private void readFromNBT(final CompoundTag target) {
        for (int x = 0; x < this.size; x++) {
            try {
                final CompoundTag c = target.getCompound("#" + x);

                if (c != null) {
                    this.inv[x] = AEItemStack.fromNBT(c);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(this.getMaxAmount(slot, stack), stack.getMaxCount());
    }

    @Override
    public ItemStack getInvStack(int slot) {
        if (this.inv[slot] == null) {
            return ItemStack.EMPTY;
        }

        return this.inv[slot].createItemStack();
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        if (simulation == Simulation.SIMULATE) {
            return true;
        }

        ItemStack oldStack = this.getInvStack(slot).copy();
        this.inv[slot] = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(to);

        ItemStack newStack = to.copy();
        InvOperation op = InvOperation.SET;

        if (ItemStack.areItemsEqual(oldStack, newStack)) {
            if (newStack.getCount() > oldStack.getCount()) {
                newStack.decrement(oldStack.getCount());
                oldStack = ItemStack.EMPTY;
                op = InvOperation.INSERT;
            } else {
                oldStack.decrement(newStack.getCount());
                newStack = ItemStack.EMPTY;
                op = InvOperation.EXTRACT;
            }
        }
        this.fireOnChangeInventory(slot, op, oldStack, newStack);
        return true;
    }

    @Override
    public int getSlotCount() {
        return this.size;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public int getChangeValue() {
        return 0;
    }

    @Nullable
    @Override
    public ListenerToken addListener(InvMarkDirtyListener listener, ListenerRemovalToken removalToken) {
        return null;
    }

    private void fireOnChangeInventory(int slot, InvOperation op, ItemStack removed, ItemStack inserted) {
        if (this.te != null && Platform.isServer() && !this.dirtyFlag) {
            this.dirtyFlag = true;
            this.te.onChangeInventory(this, slot, op, removed, inserted);
            this.te.saveChanges();
            this.dirtyFlag = false;
        }
    }

    @Override
    public int getMaxAmount(int slot, ItemStack is) {
        return Math.min(this.maxStack, 64);
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return new InvIterator(this);
    }

}
