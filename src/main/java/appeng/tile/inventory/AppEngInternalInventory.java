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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;

public class AppEngInternalInventory extends ItemStackHandler implements Iterable<net.minecraft.world.item.ItemStack> {
    private boolean enableClientEvents = false;
    private IAEAppEngInventory te;
    private final int[] maxStack;
    private net.minecraft.world.item.ItemStack previousStack = net.minecraft.world.item.ItemStack.EMPTY;
    private IAEItemFilter filter;
    private boolean dirtyFlag = false;

    public AppEngInternalInventory(final IAEAppEngInventory inventory, final int size, final int maxStack,
            IAEItemFilter filter) {
        super(size);
        this.setTileEntity(inventory);
        this.setFilter(filter);
        this.maxStack = new int[size];
        Arrays.fill(this.maxStack, maxStack);
    }

    public AppEngInternalInventory(final IAEAppEngInventory inventory, final int size, final int maxStack) {
        this(inventory, size, maxStack, null);
    }

    public AppEngInternalInventory(final IAEAppEngInventory inventory, final int size) {
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
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        this.previousStack = this.getStackInSlot(slot).copy();
        super.setStackInSlot(slot, stack);
    }

    @Override
    @Nonnull
    public net.minecraft.world.item.ItemStack insertItem(int slot, @Nonnull net.minecraft.world.item.ItemStack stack, boolean simulate) {
        if (this.filter != null && !this.filter.allowInsert(this, slot, stack)) {
            return stack;
        }

        if (!simulate) {
            this.previousStack = this.getStackInSlot(slot).copy();
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.filter != null && !this.filter.allowExtract(this, slot, amount)) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }

        if (!simulate) {
            this.previousStack = this.getStackInSlot(slot).copy();
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.te != null && this.eventsEnabled() && !this.dirtyFlag) {
            this.dirtyFlag = true;
            net.minecraft.world.item.ItemStack newStack = this.getStackInSlot(slot).copy();
            net.minecraft.world.item.ItemStack oldStack = this.previousStack;
            InvOperation op = InvOperation.SET;

            if (newStack.isEmpty() || oldStack.isEmpty() || net.minecraft.world.item.ItemStack.isSame(newStack, oldStack)) {
                if (newStack.getCount() > oldStack.getCount()) {
                    newStack.shrink(oldStack.getCount());
                    oldStack = net.minecraft.world.item.ItemStack.EMPTY;
                    op = InvOperation.INSERT;
                } else {
                    oldStack.shrink(newStack.getCount());
                    newStack = net.minecraft.world.item.ItemStack.EMPTY;
                    op = InvOperation.EXTRACT;
                }
            }

            this.getTileEntity().onChangeInventory(this, slot, op, oldStack, newStack);
            this.getTileEntity().saveChanges();
            this.previousStack = ItemStack.EMPTY;
            this.dirtyFlag = false;
        }
        super.onContentsChanged(slot);
    }

    protected boolean eventsEnabled() {
        return this.te != null && !this.te.isRemote() || this.isEnableClientEvents();
    }

    public void setMaxStackSize(final int slot, final int size) {
        this.maxStack[slot] = size;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (this.maxStack[slot] == 0) {
            return false;
        }
        if (this.filter != null) {
            return this.filter.allowInsert(this, slot, stack);
        }
        return true;
    }

    public void writeToNBT(final CompoundTag data, final String name) {
        data.put(name, this.serializeNBT());
    }

    public void readFromNBT(final CompoundTag data, final String name) {
        final CompoundTag c = data.getCompound(name);
        if (c != null) {
            this.readFromNBT(c);
        }
    }

    public void readFromNBT(final CompoundTag data) {
        this.deserializeNBT(data);
    }

    @Override
    public Iterator<net.minecraft.world.item.ItemStack> iterator() {
        return Collections.unmodifiableList(super.stacks).iterator();
    }

    private boolean isEnableClientEvents() {
        return this.enableClientEvents;
    }

    public void setEnableClientEvents(final boolean enableClientEvents) {
        this.enableClientEvents = enableClientEvents;
    }

    private IAEAppEngInventory getTileEntity() {
        return this.te;
    }

    public void setTileEntity(final IAEAppEngInventory te) {
        this.te = te;
    }
}
