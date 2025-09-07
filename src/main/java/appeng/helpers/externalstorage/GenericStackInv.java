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

package appeng.helpers.externalstorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import it.unimi.dsi.fastutil.objects.Reference2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeySlotFilter;
import appeng.api.storage.MEStorage;
import appeng.core.AELog;
import appeng.util.ConfigMenuInventory;

public class GenericStackInv implements MEStorage, GenericInternalInventory {
    protected final GenericStack[] stacks;
    private final Runnable listener;
    private boolean suppressOnChange;
    private boolean onChangeSuppressed;
    private final Reference2LongMap<AEKeyType> capacities = new Reference2LongArrayMap<>();
    private final Set<AEKeyType> supportedKeyTypes;
    @Nullable
    private AEKeySlotFilter filter;
    protected final Mode mode;
    private Component description = Component.empty();

    public enum Mode {
        /**
         * When in types mode, the config inventory will ignore all amounts and always return amount 1 for stacks in the
         * inventory.
         */
        CONFIG_TYPES,
        /**
         * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
         */
        CONFIG_STACKS,
        STORAGE
    }

    public GenericStackInv(@Nullable Runnable listener, int size) {
        this(listener, Mode.STORAGE, size);
    }

    public GenericStackInv(@Nullable Runnable listener, Mode mode, int size) {
        this(AEKeyTypes.getAll(), listener, mode, size);
    }

    public GenericStackInv(Set<AEKeyType> supportedKeyTypes, @Nullable Runnable listener, Mode mode, int size) {
        this.supportedKeyTypes = Set.copyOf(Objects.requireNonNull(supportedKeyTypes, "supportedKeyTypes"));
        this.stacks = new GenericStack[size];
        this.listener = listener;
        this.mode = mode;
    }

    protected void setFilter(@Nullable AEKeySlotFilter filter) {
        this.filter = filter;
    }

    @Nullable
    public AEKeySlotFilter getFilter() {
        return filter;
    }

    @Override
    public boolean isSupportedType(AEKeyType type) {
        return supportedKeyTypes.contains(type);
    }

    @Override
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

    @Override
    @Nullable
    public GenericStack getStack(int slot) {
        return stacks[slot];
    }

    @Override
    @Nullable
    public AEKey getKey(int slot) {
        return stacks[slot] != null ? stacks[slot].what() : null;
    }

    @Override
    public long getAmount(int slot) {
        return stacks[slot] != null ? stacks[slot].amount() : 0;
    }

    @Override
    public void setStack(int slot, @Nullable GenericStack stack) {
        // Clamp to capacity
        if (stack != null && getMaxAmount(stack.what()) < stack.amount()) {
            stack = new GenericStack(stack.what(), getMaxAmount(stack.what()));
        }
        if (!Objects.equals(stacks[slot], stack)) {
            stacks[slot] = stack;
            onChange();
        }
    }

    @Override
    public long insert(int slot, AEKey what, long amount, Actionable mode) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument(amount >= 0, "amount >= 0");

        if (!canInsert() || !isAllowedIn(slot, what)) {
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

    public boolean isAllowedIn(int slot, AEKey what) {
        return isSupportedType(what) && (filter == null || filter.isAllowed(slot, what));
    }

    @Override
    public long extract(int slot, AEKey what, long amount, Actionable mode) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument(amount >= 0, "amount >= 0");

        var currentWhat = getKey(slot);
        if (!canExtract() || currentWhat == null || !currentWhat.equals(what)) {
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
                // Ensure setStack didn't screw us over
                var reallyExtracted = Math.max(0, currentAmount - getAmount(slot));
                if (reallyExtracted != canExtract) {
                    AELog.warn(
                            "GenericStackInv simulation/modulation extraction mismatch: canExtract=%d, reallyExtracted=%d",
                            canExtract, reallyExtracted);
                    canExtract = reallyExtracted;
                }
            }
        }
        return canExtract;
    }

    @Override
    public long getCapacity(AEKeyType space) {
        return capacities.getOrDefault(space, Long.MAX_VALUE);
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    public void setCapacity(AEKeyType space, long capacity) {
        this.capacities.put(space, capacity);
    }

    public void useRegisteredCapacities() {
        for (var entry : GenericSlotCapacities.getMap().entrySet()) {
            setCapacity(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public long getMaxAmount(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            return Math.min(itemKey.getMaxStackSize(), getCapacity(key.getType()));
        }
        return getCapacity(key.getType());
    }

    @Override
    public final void onChange() {
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

    public void writeToTag(ValueOutput.ValueOutputList output) {
        // Count how many trailing nulls we have and don't write them in the first place
        var lastIndex = stacks.length;
        for (; lastIndex > 0 && stacks[lastIndex - 1] == null; lastIndex--) {
        }

        for (int i = 0; i < lastIndex; i++) {
            var stack = stacks[i];
            GenericStack.writeTag(output.addChild(), stack);
        }
    }

    public void writeToChildTag(ValueOutput output, String name) {
        boolean isEmpty = true;
        for (var stack : stacks) {
            if (stack != null) {
                isEmpty = false;
                break;
            }
        }

        if (!isEmpty) {
            writeToTag(output.childrenList(name));
        }
    }

    public void readFromTag(ValueInput.ValueInputList input) {
        boolean changed = false;
        var index = 0;
        for (var inputElement : input) {
            if (index >= size()) {
                break;
            }

            var stack = GenericStack.readTag(inputElement);
            if (!Objects.equals(stack, stacks[index])) {
                stacks[index] = stack;
                changed = true;
            }
            index++;
        }
        // Ensure any of the remaining slots are cleared
        while (index < size()) {
            if (stacks[index] != null) {
                stacks[index] = null;
                changed = true;
            }
            index++;
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

    public void readFromChildTag(ValueInput input, String name) {
        var content = input.childrenListOrEmpty(name);
        if (!content.isEmpty()) {
            readFromTag(content);
        } else {
            clear();
        }
    }

    public void readFromList(List<@Nullable GenericStack> stacks) {
        for (var i = 0; i < size(); i++) {
            if (i < stacks.size()) {
                setStack(i, stacks.get(i));
            } else {
                setStack(i, null);
            }
        }
    }

    public List<@Nullable GenericStack> toList() {
        var result = new ArrayList<GenericStack>(size());
        for (int i = 0; i < size(); i++) {
            result.add(getStack(i));
        }
        return result;
    }

    /**
     * Begin a section where change notifications are suppressed until {@link #endBatch()} is called. If a change after
     * calling this method would cause a notification to occur, a <strong>single</strong> change notification will occur
     * upon calling {@link #endBatch()} instead.
     */
    @Override
    public void beginBatch() {
        Preconditions.checkState(!suppressOnChange, "beginBatch was called without endBatch");
        suppressOnChange = true;
    }

    /**
     * Ends a batch that was begun by calling {@link #beginBatch()} and triggers a pending change notification.
     */
    @Override
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
    @Override
    public void endBatchSuppressed() {
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

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument(amount >= 0, "amount >= 0");
        if (!isSupportedType(what)) {
            return 0;
        }

        // For type configs it makes no sense to try and spread the insert across multiple slots, so we use specific
        // logic here to just shove it into the first potential slot
        if (this.mode == Mode.CONFIG_TYPES) {
            int freeSlot = -1;
            for (int i = 0; i < stacks.length; i++) {
                var key = getKey(i);
                if (key == what) {
                    return 0;
                } else if (key == null && freeSlot == -1) {
                    freeSlot = i;
                }
            }
            if (freeSlot != -1 && mode == Actionable.MODULATE) {
                setStack(freeSlot, new GenericStack(what, 0));
            }
            return 0;
        }

        var inserted = 0L;
        for (int i = 0; i < stacks.length && inserted < amount; i++) {
            inserted += insert(i, what, amount - inserted, mode);
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        Objects.requireNonNull(what, "what");
        Preconditions.checkArgument(amount >= 0, "amount >= 0");

        var extracted = 0L;
        for (int i = 0; i < stacks.length && extracted < amount; i++) {
            extracted += extract(i, what, amount - extracted, mode);
        }
        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var stack : stacks) {
            if (stack != null) {
                out.add(stack.what(), stack.amount());
            }
        }
    }

    @Override
    public Component getDescription() {
        return description;
    }

    /**
     * Changes how this generic stack inventory is reported to outside sources when used as an {@link MEStorage}.
     */
    public void setDescription(Component description) {
        this.description = description;
    }
}
