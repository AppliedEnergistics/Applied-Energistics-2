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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.math.LongMath;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

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
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

/**
 * Wraps an Item Handler in such a way that it can be used as an IMEInventory for items.
 */
public abstract class ItemHandlerAdapter
        implements IMEInventory<IAEItemStack>, IBaseMonitor<IAEItemStack>, ITickingMonitor {
    private final Map<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();
    private IActionSource mySource;
    private final Storage<ItemVariant> storage;
    private final InventoryCache cache;

    public ItemHandlerAdapter(Storage<ItemVariant> storage) {
        this.storage = storage;
        this.cache = new InventoryCache(this.storage);
    }

    /**
     * Called after successful inject or extract, use to schedule a cache rebuild (storage bus), or rebuild it directly
     * (interface).
     */
    protected abstract void onInjectOrExtract();

    @Override
    public IAEItemStack injectItems(IAEItemStack iox, Actionable type, IActionSource src) {
        var inserted = 0L;
        try (var tx = Platform.openOrJoinTx()) {
            inserted = storage.insert(iox.getVariant(), iox.getStackSize(), tx);
            if (inserted <= 0) {
                return iox;
            }

            if (type == Actionable.MODULATE) {
                tx.commit();
            }
        }

        if (type == Actionable.MODULATE) {
            onInjectOrExtract();
        }

        var overflow = iox.copy();
        overflow.decStackSize(inserted);
        return overflow.getStackSize() > 0 ? overflow : null;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, IActionSource src) {
        long extracted;
        try (var tx = Platform.openOrJoinTx()) {
            extracted = storage.extract(request.getVariant(), request.getStackSize(), tx);

            if (mode == Actionable.MODULATE) {
                tx.commit();
            }
        }

        if (extracted <= 0) {
            return null;
        }

        if (mode == Actionable.MODULATE) {
            onInjectOrExtract();
        }

        var result = request.copy();
        result.setStackSize(extracted);
        return result;
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
        private List<IAEItemStack> frontBuffer = new ArrayList<>();
        private List<IAEItemStack> backBuffer = new ArrayList<>();
        private final Storage<ItemVariant> storage;
        private static final Object2LongMap<ItemVariant> changeAccumulator = new Object2LongOpenHashMap<>();

        public InventoryCache(Storage<ItemVariant> storage) {
            this.storage = storage;
        }

        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
            frontBuffer.forEach(out::add);
            return out;
        }

        public List<IAEItemStack> update() {
            // Build the backbuffer
            backBuffer.clear();

            try (var tx = Transaction.openOuter()) {
                for (var view : storage.iterable(tx)) {
                    var stack = AEItemStack.of(view.getResource(), view.getAmount());
                    if (stack != null) {
                        backBuffer.add(stack);
                    }
                }
            }

            var changes = buildChanges();

            // Swap front/back and reuse the list the next time we do change-detection
            frontBuffer.clear();
            var tmp = backBuffer;
            backBuffer = frontBuffer;
            frontBuffer = tmp;

            return changes;
        }

        private synchronized List<IAEItemStack> buildChanges() {
            changeAccumulator.clear();

            // Insert last known state as negative amounts
            for (var stack : frontBuffer) {
                changeAccumulator.mergeLong(
                        stack.getVariant(),
                        -stack.getStackSize(),
                        LongMath::saturatedAdd);
            }

            // Insert current known state as positive amounts
            for (var stack : backBuffer) {
                changeAccumulator.mergeLong(
                        stack.getVariant(),
                        stack.getStackSize(),
                        LongMath::saturatedAdd);
            }

            // Any non-zero entry will now be a change
            List<IAEItemStack> changes = null;
            for (var entry : changeAccumulator.object2LongEntrySet()) {
                if (entry.getLongValue() != 0) {
                    if (changes == null) {
                        changes = new ArrayList<>();
                    }
                    var changeStack = AEItemStack.of(entry.getKey(), entry.getLongValue());
                    if (changeStack != null) {
                        changes.add(changeStack);
                    }
                }
            }

            return changes != null ? changes : Collections.emptyList();
        }

    }

}
