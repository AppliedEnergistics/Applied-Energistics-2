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

package appeng.parts.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.ITickingMonitor;
import appeng.util.fluid.AEFluidStack;

/**
 * Wraps an Fluid Handler in such a way that it can be used as an IMEInventory
 * for fluids.
 *
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class FluidHandlerAdapter implements IMEInventory<IAEFluidStack>, IBaseMonitor<IAEFluidStack>, ITickingMonitor {
    private final Map<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();
    private IActionSource source;
    private final IFluidHandler fluidHandler;
    private final IGridProxyable proxyable;
    private final FluidHandlerAdapter.InventoryCache cache;

    FluidHandlerAdapter(IFluidHandler fluidHandler, IGridProxyable proxy) {
        this.fluidHandler = fluidHandler;
        this.proxyable = proxy;
        this.cache = new FluidHandlerAdapter.InventoryCache(this.fluidHandler);
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {
        FluidStack fluidStack = input.getFluidStack();

        // Insert
        int wasFillled = this.fluidHandler.fill(fluidStack, type.getFluidAction());
        int remaining = fluidStack.getAmount() - wasFillled;
        if (fluidStack.getAmount() == remaining) {
            // The stack was unmodified, target tank is full
            return input;
        }

        if (type == Actionable.MODULATE) {
            try {
                this.proxyable.getProxy().getTick().alertDevice(this.proxyable.getProxy().getNode());
            } catch (GridAccessException ignore) {
                // meh
            }
        }

        fluidStack.setAmount(remaining);

        return AEFluidStack.fromFluidStack(fluidStack);
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, IActionSource src) {
        FluidStack requestedFluidStack = request.getFluidStack();

        // Drain the fluid from the tank
        FluidStack gathered = this.fluidHandler.drain(requestedFluidStack, mode.getFluidAction());
        if (gathered.isEmpty()) {
            // If nothing was pulled from the tank, return null
            return null;
        }

        if (mode == Actionable.MODULATE) {
            try {
                this.proxyable.getProxy().getTick().alertDevice(this.proxyable.getProxy().getNode());
            } catch (GridAccessException ignore) {
                // meh
            }
        }
        return AEFluidStack.fromFluidStack(gathered);
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
        return Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);
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
        private IAEFluidStack[] cachedAeStacks = new IAEFluidStack[0];
        private final IFluidHandler fluidHandler;

        public InventoryCache(IFluidHandler fluidHandler) {
            this.fluidHandler = fluidHandler;
        }

        public List<IAEFluidStack> update() {
            final List<IAEFluidStack> changes = new ArrayList<>();
            final int slots = fluidHandler.getTanks();

            // Make room for new slots
            if (slots > this.cachedAeStacks.length) {
                this.cachedAeStacks = Arrays.copyOf(this.cachedAeStacks, slots);
            }

            for (int slot = 0; slot < slots; slot++) {
                // Save the old stuff
                final IAEFluidStack oldAEFS = this.cachedAeStacks[slot];
                final FluidStack newFS = fluidHandler.getFluidInTank(slot);

                this.handlePossibleSlotChanges(slot, oldAEFS, newFS, changes);
            }

            // Handle cases where the number of slots actually is lower now than before
            if (slots < this.cachedAeStacks.length) {
                for (int slot = slots; slot < this.cachedAeStacks.length; slot++) {
                    final IAEFluidStack aeStack = this.cachedAeStacks[slot];

                    if (aeStack != null) {
                        final IAEFluidStack a = aeStack.copy();
                        a.setStackSize(-a.getStackSize());
                        changes.add(a);
                    }
                }

                this.cachedAeStacks = Arrays.copyOf(this.cachedAeStacks, slots);
            }
            return changes;
        }

        public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
            Arrays.stream(this.cachedAeStacks).forEach(out::add);
            return out;
        }

        private void handlePossibleSlotChanges(int slot, IAEFluidStack oldAeFS, FluidStack newFS,
                List<IAEFluidStack> changes) {
            if (oldAeFS != null && oldAeFS.getFluidStack().isFluidEqual(newFS)) {
                this.handleStackSizeChanged(slot, oldAeFS, newFS, changes);
            } else {
                this.handleFluidChanged(slot, oldAeFS, newFS, changes);
            }
        }

        private void handleStackSizeChanged(int slot, IAEFluidStack oldAeFS, FluidStack newFS,
                List<IAEFluidStack> changes) {
            // Still the same fluid, but amount might have changed
            final long diff = newFS.getAmount() - oldAeFS.getStackSize();

            if (diff != 0) {
                final IAEFluidStack stack = oldAeFS.copy();
                stack.setStackSize(newFS.getAmount());

                this.cachedAeStacks[slot] = stack;

                final IAEFluidStack a = stack.copy();
                a.setStackSize(diff);
                changes.add(a);
            }
        }

        private void handleFluidChanged(int slot, IAEFluidStack oldAeFS, FluidStack newFS,
                List<IAEFluidStack> changes) {
            // Completely different fluid
            this.cachedAeStacks[slot] = AEFluidStack.fromFluidStack(newFS);

            // If we had a stack previously in this slot, notify the network about its
            // disappearance
            if (oldAeFS != null) {
                oldAeFS.setStackSize(-oldAeFS.getStackSize());
                changes.add(oldAeFS);
            }

            // Notify the network about the new stack. Note that this is null if newFS was
            // null
            if (this.cachedAeStacks[slot] != null) {
                changes.add(this.cachedAeStacks[slot]);
            }
        }
    }
}
