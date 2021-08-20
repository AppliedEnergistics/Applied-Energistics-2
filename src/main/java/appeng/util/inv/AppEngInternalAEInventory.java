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

package appeng.util.inv;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class AppEngInternalAEInventory extends BaseInternalInventory {
    private final InternalInventoryHost te;
    private final IAEItemStack[] inv;
    private final int size;
    private int maxStack;
    private boolean dirtyFlag = false;

    public AppEngInternalAEInventory(final InternalInventoryHost te, final int s) {
        this.te = te;
        this.size = s;
        this.maxStack = 64;
        this.inv = new IAEItemStack[s];
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
        if (data.contains(name, Tag.TAG_COMPOUND)) {
            var c = data.getCompound(name);
            this.readFromNBT(c);
        }
    }

    private void readFromNBT(CompoundTag target) {
        for (int x = 0; x < this.size; x++) {
            try {
                String name = "#" + x;
                if (target.contains(name, Tag.TAG_COMPOUND)) {
                    var c = target.getCompound(name);
                    this.inv[x] = AEItemStack.fromNBT(c);
                }
            } catch (Exception e) {
                AELog.warn(e);
            }
        }
    }

    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
    }

    public int size() {
        return this.size;
    }

    public ItemStack getStackInSlot(final int var1) {
        if (this.inv[var1] == null) {
            return ItemStack.EMPTY;
        }

        return this.inv[var1].createItemStack();
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = this.getStackInSlot(slot);
        int limit = this.getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!Platform.canItemStacksStack(stack, existing)) {
                return stack;
            }

            limit -= existing.getCount();
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.inv[slot] = StorageChannels.items()
                        .createStack(reachedLimit ? Platform.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            this.fireOnChangeInventory(slot, ItemStack.EMPTY,
                    reachedLimit ? Platform.copyStackWithSize(stack, limit) : stack);
        }
        return reachedLimit ? Platform.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.inv[slot] != null) {
            final ItemStack split = this.getStackInSlot(slot);

            if (amount >= split.getCount()) {
                if (!simulate) {
                    this.inv[slot] = null;
                    this.fireOnChangeInventory(slot, split, ItemStack.EMPTY);
                }
                return split;
            } else {
                if (!simulate) {
                    split.grow(-amount);
                    this.fireOnChangeInventory(slot,
                            Platform.copyStackWithSize(split, amount), ItemStack.EMPTY);
                }
                return Platform.copyStackWithSize(split, amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
        ItemStack oldStack = this.getStackInSlot(slotIndex).copy();
        this.inv[slotIndex] = StorageChannels.items()
                .createStack(stack);

        if (this.te != null && !this.te.isRemote()) {
            ItemStack newStack = stack.copy();

            if (ItemStack.isSame(oldStack, newStack)) {
                if (newStack.getCount() > oldStack.getCount()) {
                    newStack.shrink(oldStack.getCount());
                    oldStack = ItemStack.EMPTY;
                } else {
                    oldStack.shrink(newStack.getCount());
                    newStack = ItemStack.EMPTY;
                }
            }
            this.fireOnChangeInventory(slotIndex, oldStack, newStack);
        }
    }

    private void fireOnChangeInventory(int slot, ItemStack removed, ItemStack inserted) {
        if (this.te != null && !this.te.isRemote() && !this.dirtyFlag) {
            this.dirtyFlag = true;
            this.te.onChangeInventory(this, slot, removed, inserted);
            this.te.saveChanges();
            this.dirtyFlag = false;
        }
    }

    public int getSlotLimit(int slot) {
        return Math.min(this.maxStack, 64);
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

}
