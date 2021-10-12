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

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.BaseInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class AppEngInternalInventory extends BaseInternalInventory {
    private boolean enableClientEvents = false;
    private InternalInventoryHost host;
    private final NonNullList<ItemStack> stacks;
    private final int[] maxStack;
    private IAEItemFilter filter;
    private boolean notifyingChanges = false;

    public AppEngInternalInventory(InternalInventoryHost host, int size, int maxStack, IAEItemFilter filter) {
        this.setHost(host);
        this.setFilter(filter);
        this.maxStack = new int[size];
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        Arrays.fill(this.maxStack, maxStack);
    }

    public AppEngInternalInventory(@Nullable InternalInventoryHost inventory, final int size, final int maxStack) {
        this(inventory, size, maxStack, null);
    }

    public AppEngInternalInventory(int size) {
        this(null, size, 64);
    }

    public AppEngInternalInventory(@Nullable InternalInventoryHost inventory, int size) {
        this(inventory, size, 64);
    }

    public void setFilter(IAEItemFilter filter) {
        this.filter = filter;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.maxStack[slot];
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return stacks.get(slotIndex);
    }

    @Override
    public void setItemDirect(int slot, @Nonnull ItemStack stack) {
        var previousStack = stacks.get(slot).copy();
        stacks.set(slot, stack);
        onContentsChanged(slot, previousStack);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        Preconditions.checkArgument(slot >= 0 && slot < size(), "slot out of range");

        if (this.filter != null && !this.filter.allowExtract(this, slot, amount)) {
            return ItemStack.EMPTY;
        }

        var stack = stacks.get(slot);

        // This inventory adheres to vanilla stack size limits
        int toExtract = Math.min(stack.getCount(), Math.min(amount, stack.getMaxStackSize()));
        if (toExtract <= 0) {
            return ItemStack.EMPTY;
        }

        if (stack.getCount() <= toExtract) {
            if (!simulate) {
                setItemDirect(slot, ItemStack.EMPTY);
                onContentsChanged(slot, stack);
                return stack;
            } else {
                return stack.copy();
            }
        } else {
            var result = stack.copy();

            if (!simulate) {
                var prev = stack.copy();
                stack.shrink(toExtract);
                onContentsChanged(slot, prev);
            }

            result.setCount(toExtract);
            return result;
        }
    }

    protected void onContentsChanged(int slot, ItemStack oldStack) {
        if (this.host != null && this.eventsEnabled() && !this.notifyingChanges) {
            this.notifyingChanges = true;
            ItemStack newStack = this.getStackInSlot(slot).copy();

            if (newStack.isEmpty() || oldStack.isEmpty() || ItemStack.isSame(newStack, oldStack)) {
                if (newStack.getCount() > oldStack.getCount()) {
                    newStack.shrink(oldStack.getCount());
                    oldStack = ItemStack.EMPTY;
                } else {
                    oldStack.shrink(newStack.getCount());
                    newStack = ItemStack.EMPTY;
                }
            }

            this.host.onChangeInventory(this, slot, oldStack, newStack);
            this.host.saveChanges();
            this.notifyingChanges = false;
        }
    }

    protected boolean eventsEnabled() {
        return this.host != null && !this.host.isRemote() || this.isEnableClientEvents();
    }

    public void setMaxStackSize(final int slot, final int size) {
        this.maxStack[slot] = size;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        if (this.maxStack[slot] == 0) {
            return false;
        }
        if (this.filter != null) {
            return this.filter.allowInsert(this, slot, stack);
        }
        return true;
    }

    public void writeToNBT(CompoundTag data, String name) {
        if (isEmpty()) {
            data.remove(name);
            return;
        }

        var items = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            var stack = stacks.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                items.add(stack.save(itemTag));
            }
        }
        data.put(name, items);
    }

    public void readFromNBT(CompoundTag data, String name) {
        if (data.contains(name, Tag.TAG_LIST)) {
            var tagList = data.getList(name, Tag.TAG_COMPOUND);
            for (var itemTag : tagList) {
                var itemCompound = (CompoundTag) itemTag;
                int slot = itemCompound.getInt("Slot");

                if (slot >= 0 && slot < stacks.size()) {
                    stacks.set(slot, ItemStack.of(itemCompound));
                }
            }
        }
    }

    private boolean isEnableClientEvents() {
        return this.enableClientEvents;
    }

    public void setEnableClientEvents(final boolean enableClientEvents) {
        this.enableClientEvents = enableClientEvents;
    }

    protected final void setHost(InternalInventoryHost host) {
        this.host = host;
    }

    @Override
    public int size() {
        return stacks.size();
    }
}
