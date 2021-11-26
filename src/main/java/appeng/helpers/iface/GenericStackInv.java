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

package appeng.helpers.iface;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import appeng.api.config.Actionable;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.util.ConfigMenuInventory;

public class GenericStackInv {
    protected final GenericStack[] stacks;
    private final Runnable listener;
    private boolean suppressOnChange;
    private boolean onChangeSuppressed;
    private long capacity = Long.MAX_VALUE;
    @org.jetbrains.annotations.Nullable
    private AEKeyFilter filter;
    protected final Mode mode;

    public enum Mode {
        CONFIG_TYPES,
        CONFIG_STACKS,
        STORAGE
    }

    public GenericStackInv(@Nullable Runnable listener, int size) {
        this(listener, Mode.STORAGE, size);
    }

    public GenericStackInv(@Nullable Runnable listener, Mode mode, int size) {
        this.stacks = new GenericStack[size];
        this.listener = listener;
        this.mode = mode;
    }

    protected void setFilter(@Nullable AEKeyFilter filter) {
        this.filter = filter;
    }

    @Nullable
    public AEKeyFilter getFilter() {
        return filter;
    }

    public boolean isAllowed(AEKey what) {
        return filter == null || filter.matches(what);
    }

    public boolean isAllowed(@Nullable GenericStack stack) {
        return stack == null || isAllowed(stack.what());
    }

    public int size() {
        return stacks.length;
    }

    public boolean isEmpty() {
        for (var stack : stacks) {
            if (stack != null) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public GenericStack getStack(int slot) {
        return stacks[slot];
    }

    @Nullable
    public AEKey getKey(int slot) {
        return stacks[slot] != null ? stacks[slot].what() : null;
    }

    public long getAmount(int slot) {
        return stacks[slot] != null ? stacks[slot].amount() : 0;
    }

    public void setStack(int slot, @Nullable GenericStack stack) {
        if (!Objects.equals(stacks[slot], stack)) {
            stacks[slot] = stack;
            onChange();
        }
    }

    public long insert(int slot, AEKey what, long amount, Actionable mode) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument(amount >= 0, "amount >= 0");

        if (!isAllowed(what)) {
            return 0;
        }

        var currentWhat = getKey(slot);
        var currentAmount = getAmount(slot);
        if (currentWhat == null || currentWhat.equals(what)) {
            var newAmount = Math.min(currentAmount + amount, getMaxAmount(what));
            if (newAmount > currentAmount) {
                if (mode == Actionable.MODULATE) {
                    setStack(slot, new GenericStack(what, newAmount));
                    // Ensure setStack didn't screw us over
                    newAmount = getAmount(slot);
                }
                return newAmount - currentAmount;
            }
        }
        return 0;
    }

    public long extract(int slot, AEKey what, long amount, Actionable mode) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument(amount >= 0, "amount >= 0");

        var currentWhat = getKey(slot);
        if (currentWhat == null || !currentWhat.equals(what)) {
            // Can't extract from empty slot or mismatched type
            return 0;
        }

        var currentAmount = getAmount(slot);
        var canExtract = Math.min(currentAmount, amount);

        if (canExtract > 0) {
            if (mode == Actionable.MODULATE) {
                var newAmount = currentAmount - canExtract;
                if (newAmount <= 0) {
                    setStack(slot, null);
                } else {
                    setStack(slot, new GenericStack(what, newAmount));
                }
                // Ensure setStack didn't screw us over and reduce the extracted amount by what is left
                canExtract = Math.max(0, canExtract - getAmount(slot));
            }
        }
        return canExtract;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getMaxAmount(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            return Math.min(itemKey.getItem().getMaxStackSize(), capacity);
        }
        return capacity;
    }

    final void onChange() {
        if (!suppressOnChange) {
            notifyListener();
        } else {
            onChangeSuppressed = true;
        }
    }

    protected void notifyListener() {
        if (listener != null) {
            listener.run();
        }
    }

    public ListTag writeToTag() {
        ListTag tag = new ListTag();

        for (var stack : stacks) {
            tag.add(GenericStack.writeTag(stack));
        }

        // Strip out trailing nulls
        for (int i = tag.size() - 1; i >= 0; i--) {
            if (tag.getCompound(i).isEmpty()) {
                tag.remove(i);
            } else {
                break;
            }
        }

        return tag;
    }

    public void writeToChildTag(CompoundTag tag, String name) {
        boolean isEmpty = true;
        for (var stack : stacks) {
            if (stack != null) {
                isEmpty = false;
                break;
            }
        }

        if (!isEmpty) {
            tag.put(name, writeToTag());
        } else {
            tag.remove(name);
        }
    }

    public void readFromTag(ListTag tag) {
        boolean changed = false;
        for (int i = 0; i < Math.min(size(), tag.size()); ++i) {
            var stack = GenericStack.readTag(tag.getCompound(i));
            if (!Objects.equals(stack, stacks[i])) {
                stacks[i] = stack;
                changed = true;
            }
        }
        // Ensure any of the remaining slots are cleared
        for (int i = tag.size(); i < size(); i++) {
            if (stacks[i] != null) {
                stacks[i] = null;
                changed = true;
            }
        }

        if (changed) {
            onChange();
        }
    }

    /**
     * Convenience method to clear the inventory, by setting all slots to null. Triggers only a single change
     * notification at the end.
     */
    public void clear() {
        boolean changed = false;
        for (int i = 0; i < stacks.length; i++) {
            changed |= stacks[i] != null;
            stacks[i] = null;
        }
        if (changed) {
            onChange();
        }
    }

    public void readFromChildTag(CompoundTag tag, String name) {
        if (tag.contains(name, Tag.TAG_LIST)) {
            readFromTag(tag.getList(name, Tag.TAG_COMPOUND));
        } else {
            clear();
        }
    }

    /**
     * Begin a section where change notifications are supressed until {@link #endBatch()} is called. If a change after
     * calling this method would cause a notification to occur, a <strong>single</strong> change notification will occur
     * upon calling {@link #endBatch()} instead.
     */
    public void beginBatch() {
        Preconditions.checkState(!suppressOnChange, "beginBatch was called without endBatch");
        suppressOnChange = true;
    }

    /**
     * Ends a batch that was begun by calling {@link #beginBatch()} and triggers a pending change notification.
     */
    public void endBatch() {
        Preconditions.checkState(suppressOnChange, "endBatch was called without beginBatch");
        suppressOnChange = false;
        if (onChangeSuppressed) {
            onChangeSuppressed = false;
            onChange();
        }
    }

    /**
     * Ends a batch that was begun by calling {@link #beginBatch()} and drops the change notification.
     */
    void endBatchSuppressed() {
        Preconditions.checkState(suppressOnChange, "endBatch was called without beginBatch");
        suppressOnChange = false;
        onChangeSuppressed = false;
    }

    public Mode getMode() {
        return mode;
    }

    /**
     * Creates a wrapper around this config inventory for use with {@link appeng.menu.slot.FakeSlot} in menus.
     */
    public ConfigMenuInventory createMenuWrapper() {
        return new ConfigMenuInventory(this);
    }

}
