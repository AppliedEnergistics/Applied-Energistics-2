/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.fluid.AEFluidStack;

/**
 * Wraps an Fluid Handler in such a way that it can be used as an IMEInventory for fluids.
 */
public abstract class FluidHandlerAdapter
        implements IMEInventory<IAEFluidStack>, IBaseMonitor<IAEFluidStack>, ITickingMonitor {
    private final Map<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();
    private IActionSource source;
    private final Storage<FluidVariant> fluidHandler;
    private final FluidHandlerAdapter.InventoryCache cache;

    public FluidHandlerAdapter(Storage<FluidVariant> fluidHandler, boolean extractOnlyMode) {
        this.fluidHandler = fluidHandler;
        this.cache = new FluidHandlerAdapter.InventoryCache(this.fluidHandler, extractOnlyMode);
    }

    /**
     * Called after successful inject or extract, use to schedule a cache rebuild (storage bus), or rebuild it directly
     * (interface).
     */
    protected abstract void onInjectOrExtract();

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {

        try (var tx = Platform.openOrJoinTx()) {
            var filled = this.fluidHandler.insert(input.getFluid(), input.getStackSize(), tx);

            if (filled == 0) {
                return input.copy();
            }

            if (type == Actionable.MODULATE) {
                tx.commit();
                this.onInjectOrExtract();
            }

            if (filled >= input.getStackSize()) {
                return null;
            }

            return IAEStack.copy(input, input.getStackSize() - filled);
        }

    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, IActionSource src) {

        try (var tx = Platform.openOrJoinTx()) {

            var drained = this.fluidHandler.extract(request.getFluid(), request.getStackSize(), tx);

            if (drained <= 0) {
                return null;
            }

            if (mode == Actionable.MODULATE) {
                tx.commit();
                this.onInjectOrExtract();
            }

            return IAEStack.copy(request, drained);
        }

    }

    @Override
    public TickRateModulation onTick() {
        List<IAEFluidStack> changes = this.cache.update();
        if (!changes.isEmpty()) {
            this.postDifference(changes);
            return TickRateModulation.URGENT;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
        return this.cache.getAvailableItems(out);
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return StorageChannels.fluids();
    }

    @Override
    public void setActionSource(IActionSource source) {
        this.source = source;
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEFluidStack> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEFluidStack> l) {
        this.listeners.remove(l);
    }

    private void postDifference(Iterable<IAEFluidStack> a) {
        final Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object>> i = this.listeners.entrySet()
                .iterator();
        while (i.hasNext()) {
            final Map.Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> l = i.next();
            final IMEMonitorHandlerReceiver<IAEFluidStack> key = l.getKey();
            if (key.isValid(l.getValue())) {
                key.postChange(this, a, this.source);
            } else {
                i.remove();
            }
        }
    }

    private static class InventoryCache {
        private IItemList<IAEFluidStack> frontBuffer = StorageChannels.fluids().createList();
        private IItemList<IAEFluidStack> backBuffer = StorageChannels.fluids().createList();
        private final Storage<FluidVariant> fluidHandler;
        private final boolean extractableOnly;

        public InventoryCache(Storage<FluidVariant> fluidHandler,
                boolean extractableOnly) {
            this.fluidHandler = fluidHandler;
            this.extractableOnly = extractableOnly;
        }

        public List<IAEFluidStack> update() {
            // Flip back & front buffer and start building a new list
            var tmp = backBuffer;
            backBuffer = frontBuffer;
            frontBuffer = tmp;
            frontBuffer.resetStatus();

            // Rebuild the front buffer
            try (var tx = Transaction.openOuter()) {
                for (var view : this.fluidHandler.iterable(tx)) {
                    if (view.isResourceBlank()) {
                        continue;
                    }

                    // Skip resources that cannot be extracted if that filter was enabled
                    if (extractableOnly) {
                        // Use an inner TX to prevent two tanks that can be extracted from only mutually exclusively
                        // from not being influenced by our extraction test here.
                        try (var innerTx = tx.openNested()) {
                            var extracted = view.extract(view.getResource(), FluidConstants.DROPLET, innerTx);
                            // If somehow extracting the minimal amount doesn't work, check if everything could be
                            // extracted
                            // because the tank might have a minimum (or fixed) allowed extraction amount.
                            if (extracted == 0) {
                                extracted = view.extract(view.getResource(), view.getAmount(), innerTx);
                            }
                            if (extracted == 0) {
                                // We weren't able to simulate extraction of any fluid, so skip this one
                                continue;
                            }
                        }
                    }

                    frontBuffer.addStorage(AEFluidStack.of(view.getResource(), view.getAmount()));
                }
            }

            // Diff the front-buffer against the backbuffer
            var changes = new ArrayList<IAEFluidStack>();
            for (var stack : frontBuffer) {
                var old = backBuffer.findPrecise(stack);
                if (old == null) {
                    changes.add(stack.copy()); // new entry
                } else if (old.getStackSize() != stack.getStackSize()) {
                    var change = stack.copy();
                    change.decStackSize(old.getStackSize());
                    changes.add(change); // changed amount
                }
            }
            // Account for removals
            for (var oldStack : backBuffer) {
                if (frontBuffer.findPrecise(oldStack) == null) {
                    changes.add(IAEStack.copy(oldStack, -oldStack.getStackSize()));
                }
            }

            return changes;
        }

        public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
            for (var stack : frontBuffer) {
                out.addStorage(stack);
            }
            return out;
        }
    }
}
