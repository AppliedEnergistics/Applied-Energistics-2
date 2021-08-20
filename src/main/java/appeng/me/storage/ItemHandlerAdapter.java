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

package appeng.me.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Ints;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.me.storage.ITickingMonitor;
import appeng.util.item.AEItemStack;

/**
 * Wraps an Item Handler in such a way that it can be used as an IMEInventory for items.
 */
public abstract class ItemHandlerAdapter implements IMEInventory<IAEItemStack>, IBaseMonitor<IAEItemStack>, ITickingMonitor {
    private final Map<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();
    private IActionSource mySource;
    private final IItemHandler itemHandler;
    private final InventoryCache cache;

    public ItemHandlerAdapter(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
        this.cache = new InventoryCache(this.itemHandler);
    }

    /**
     * Called after successful inject or extract, use to schedule a cache rebuild (storage bus),
     * or rebuild it directly (interface).
     */
    protected abstract void onInjectOrExtract();

    @Override
    public IAEItemStack injectItems(IAEItemStack iox, Actionable type, IActionSource src) {
        ItemStack orgInput = iox.createItemStack();
        ItemStack remaining = orgInput;

        int slotCount = this.itemHandler.getSlots();
        boolean simulate = type == Actionable.SIMULATE;

        // This uses a brute force approach and tries to jam it in every slot the inventory exposes.
        for (int i = 0; i < slotCount && !remaining.isEmpty(); i++) {
            remaining = this.itemHandler.insertItem(i, remaining, simulate);
        }

        // At this point, we still have some items left...
        if (remaining == orgInput) {
            // The stack remained unmodified, target inventory is full
            return iox;
        }

        if (type == Actionable.MODULATE) {
            onInjectOrExtract();
        }

        return AEItemStack.fromItemStack(remaining);
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, IActionSource src) {
        int remainingSize = Ints.saturatedCast(request.getStackSize());

        // Use this to gather the requested items
        ItemStack gathered = ItemStack.EMPTY;

        final boolean simulate = mode == Actionable.SIMULATE;

        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            ItemStack stackInInventorySlot = this.itemHandler.getStackInSlot(i);

            if (!request.isSameType(stackInInventorySlot)) {
                continue;
            }

            ItemStack extracted;
            int stackSizeCurrentSlot = stackInInventorySlot.getCount();
            int remainingCurrentSlot = Math.min(remainingSize, stackSizeCurrentSlot);

            // We have to loop here because according to the docs, the handler shouldn't return a stack with
            // size > maxSize, even if we request more. So even if it returns a valid stack, it might have more stuff.
            do {
                extracted = this.itemHandler.extractItem(i, remainingCurrentSlot, simulate);
                if (!extracted.isEmpty()) {
                    // In order to guard against broken IItemHandler implementations, we'll try to guess if the returned
                    // stack (especially in simulate mode) is the same that was returned by getStackInSlot. This is
                    // obviously not a precise science, but it would catch the previous Forge bug:
                    // https://github.com/MinecraftForge/MinecraftForge/pull/6580
                    if (extracted == stackInInventorySlot) {
                        extracted = extracted.copy();
                    }

                    if (extracted.getCount() > remainingCurrentSlot) {
                        // Something broke. It should never return more than we requested...
                        // We're going to silently eat the remainder
                        AELog.warn(
                                "Mod that provided item handler %s is broken. Returned %s items while only requesting %d.",
                                this.itemHandler.getClass().getName(), extracted.toString(), remainingCurrentSlot);
                        extracted.setCount(remainingCurrentSlot);
                    }

                    // Heuristic for simulation: looping in case of simulations is pointless, since the state of the
                    // underlying inventory does not change after a simulated extraction. To still support inventories
                    // that report stacks that are larger than maxStackSize, we use this heuristic
                    if (simulate && extracted.getCount() == extracted.getMaxStackSize()
                            && remainingCurrentSlot > extracted.getMaxStackSize()) {
                        extracted.setCount(remainingCurrentSlot);
                    }

                    // We're just gonna use the first stack we get our hands on as the template for the rest.
                    if (gathered.isEmpty()) {
                        gathered = extracted;
                    } else {
                        gathered.grow(extracted.getCount());
                    }
                    remainingCurrentSlot -= extracted.getCount();
                }
            } while (!simulate && !extracted.isEmpty() && remainingCurrentSlot > 0);

            remainingSize -= stackSizeCurrentSlot - remainingCurrentSlot;

            // Done?
            if (remainingSize <= 0) {
                break;
            }
        }

        if (!gathered.isEmpty()) {
            if (mode == Actionable.MODULATE) {
                onInjectOrExtract();
            }

            return AEItemStack.fromItemStack(gathered);
        }

        return null;
    }

    @Override
    public TickRateModulation onTick() {
        List<IAEItemStack> changes = this.cache.update();
        if (!changes.isEmpty()) {
            this.postDifference(changes);
            return TickRateModulation.URGENT;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public void setActionSource(final IActionSource mySource) {
        this.mySource = mySource;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
        return this.cache.getAvailableItems(out);
    }

    @Override
    public IItemStorageChannel getChannel() {
        return StorageChannels.items();
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEItemStack> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEItemStack> l) {
        this.listeners.remove(l);
    }

    private void postDifference(Iterable<IAEItemStack> a) {
        final Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.listeners.entrySet()
                .iterator();
        while (i.hasNext()) {
            final Map.Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> l = i.next();
            final IMEMonitorHandlerReceiver<IAEItemStack> key = l.getKey();
            if (key.isValid(l.getValue())) {
                key.postChange(this, a, this.mySource);
            } else {
                i.remove();
            }
        }
    }

    private static class InventoryCache {
        private IAEItemStack[] cachedAeStacks = new IAEItemStack[0];
        private final IItemHandler itemHandler;

        public InventoryCache(IItemHandler itemHandler) {
            this.itemHandler = itemHandler;
        }

        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
            Arrays.stream(this.cachedAeStacks).forEach(out::add);
            return out;
        }

        public List<IAEItemStack> update() {
            final List<IAEItemStack> changes = new ArrayList<>();
            final int slots = this.itemHandler.getSlots();

            // Make room for new slots
            if (slots > this.cachedAeStacks.length) {
                this.cachedAeStacks = Arrays.copyOf(this.cachedAeStacks, slots);
            }

            for (int slot = 0; slot < slots; slot++) {
                // Save the old stuff
                final IAEItemStack oldAeIS = this.cachedAeStacks[slot];
                final ItemStack newIS = this.itemHandler.getStackInSlot(slot);

                this.handlePossibleSlotChanges(slot, oldAeIS, newIS, changes);
            }

            // Handle cases where the number of slots actually is lower now than before
            if (slots < this.cachedAeStacks.length) {
                for (int slot = slots; slot < this.cachedAeStacks.length; slot++) {
                    final IAEItemStack aeStack = this.cachedAeStacks[slot];

                    if (aeStack != null) {
                        final IAEItemStack a = aeStack.copy();
                        a.setStackSize(-a.getStackSize());
                        changes.add(a);
                    }
                }

                // Reduce the cache size
                this.cachedAeStacks = Arrays.copyOf(this.cachedAeStacks, slots);
            }

            return changes;
        }

        private void handlePossibleSlotChanges(int slot, IAEItemStack oldAeIS, ItemStack newIS,
                List<IAEItemStack> changes) {
            if (oldAeIS != null && oldAeIS.isSameType(newIS)) {
                this.handleStackSizeChanged(slot, oldAeIS, newIS, changes);
            } else {
                this.handleItemChanged(slot, oldAeIS, newIS, changes);
            }
        }

        private void handleStackSizeChanged(int slot, IAEItemStack oldAeIS, ItemStack newIS,
                List<IAEItemStack> changes) {
            // Still the same item, but amount might have changed
            final long diff = newIS.getCount() - oldAeIS.getStackSize();

            if (diff != 0) {
                final IAEItemStack stack = oldAeIS.copy();
                stack.setStackSize(newIS.getCount());

                this.cachedAeStacks[slot] = stack;

                final IAEItemStack a = stack.copy();
                a.setStackSize(diff);
                changes.add(a);
            }
        }

        private void handleItemChanged(int slot, IAEItemStack oldAeIS, ItemStack newIS, List<IAEItemStack> changes) {
            // Completely different item
            this.cachedAeStacks[slot] = AEItemStack.fromItemStack(newIS);

            // If we had a stack previously in this slot, notify the network about its disappearance
            if (oldAeIS != null) {
                oldAeIS.setStackSize(-oldAeIS.getStackSize());
                changes.add(oldAeIS);
            }

            // Notify the network about the new stack. Note that this is null if newIS was null
            if (this.cachedAeStacks[slot] != null) {
                changes.add(this.cachedAeStacks[slot]);
            }
        }
    }

}
