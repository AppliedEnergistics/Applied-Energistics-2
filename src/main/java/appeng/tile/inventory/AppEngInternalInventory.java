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


import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;


public class AppEngInternalInventory extends ItemStackHandler implements Iterable<ItemStack> {
    private boolean enableClientEvents = false;
    private IAEAppEngInventory te;
    private final int[] maxStack;
    private ItemStack previousStack = ItemStack.EMPTY;
    private IAEItemFilter filter;
    private boolean dirtyFlag = false;

    public AppEngInternalInventory(final IAEAppEngInventory inventory, final int size, final int maxStack, IAEItemFilter filter) {
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
        if (stack != this.getStackInSlot(slot)) {
            this.previousStack = this.getStackInSlot(slot).copy();
        }
        super.setStackInSlot(slot, stack);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
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
            return ItemStack.EMPTY;
        }

        if (!simulate) {
            this.previousStack = this.getStackInSlot(slot).copy();
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.getTileEntity() != null && this.eventsEnabled() && !this.dirtyFlag) {
            this.dirtyFlag = true;
            ItemStack newStack = this.getStackInSlot(slot);
            ItemStack oldStack = this.previousStack;
            InvOperation op = InvOperation.SET;

            if (newStack.isEmpty() || oldStack.isEmpty() || oldStack.getCount() != newStack.getCount() && ItemStack.areItemsEqual(newStack, oldStack)) {
                if (newStack.getCount() > oldStack.getCount()) {
                    newStack = newStack.copy();
                    newStack.shrink(oldStack.getCount());
                    oldStack = ItemStack.EMPTY;
                    op = InvOperation.INSERT;
                } else {
                    oldStack.shrink(newStack.getCount());
                    newStack = ItemStack.EMPTY;
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
        return Platform.isServer() || this.isEnableClientEvents();
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

    public void writeToNBT(final NBTTagCompound data, final String name) {
        data.setTag(name, this.serializeNBT());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack is = stacks.get(i);
            if (!is.isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                if (is.getCount() > is.getMaxStackSize()) {
                    itemTag.setInteger("stackSize", stacks.get(i).getCount());
                }
                stacks.get(i).writeToNBT(itemTag);
                nbtTagList.appendTag(itemTag);
            }
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", stacks.size());
        return nbt;
    }

    public void readFromNBT(final NBTTagCompound data, final String name) {
        final NBTTagCompound c = data.getCompoundTag(name);
        if (c != null) {
            this.readFromNBT(c);
        }
    }

    public void readFromNBT(final NBTTagCompound data) {
        this.deserializeNBT(data);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        setSize(nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.size());
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, new ItemStack(itemTags));
                if (itemTags.hasKey("stackSize")) {
                    int stackSize = itemTags.getInteger("stackSize");
                    stacks.get(slot).setCount(stackSize);
                }
            }
        }
        onLoad();
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return Collections.unmodifiableList(super.stacks).iterator();
    }

    private boolean isEnableClientEvents() {
        return this.enableClientEvents;
    }

    public void setEnableClientEvents(final boolean enableClientEvents) {
        this.enableClientEvents = enableClientEvents;
    }

    public IAEAppEngInventory getTileEntity() {
        return this.te;
    }

    public void setTileEntity(final IAEAppEngInventory te) {
        this.te = te;
    }
}
