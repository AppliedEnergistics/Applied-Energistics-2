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

package appeng.fluids.parts;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv;
import alexiil.mc.lib.attributes.fluid.filter.ExactFluidFilter;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

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
import appeng.fluids.helper.GroupedFluidInvCache;
import appeng.fluids.util.AEFluidStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.ITickingMonitor;

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
    private final GroupedFluidInv fluidHandler;
    private final IGridProxyable proxyable;
    private final GroupedFluidInvCache cache;

    FluidHandlerAdapter(GroupedFluidInv fluidHandler, IGridProxyable proxy) {
        this.fluidHandler = fluidHandler;
        this.proxyable = proxy;
        this.cache = new GroupedFluidInvCache(this.fluidHandler);
    }

    private Simulation getFluidAction(Actionable actionable) {
        return actionable == Actionable.MODULATE ? Simulation.ACTION : Simulation.SIMULATE;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {
        FluidVolume fluidStack = input.getFluidStack();

        // Insert
        FluidVolume excess = this.fluidHandler.attemptInsertion(fluidStack, getFluidAction(type));
        if (excess.amount().equals(fluidStack.amount())) {
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

        return AEFluidStack.fromFluidVolume(excess, RoundingMode.DOWN);
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, IActionSource src) {
        FluidFilter filter = new ExactFluidFilter(request.getFluid());

        // Drain the fluid from the tank
        FluidVolume gathered = this.fluidHandler.attemptExtraction(filter, request.getAmount(), getFluidAction(mode));
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
        return AEFluidStack.fromFluidVolume(gathered, RoundingMode.DOWN);
    }

    @Override
    public TickRateModulation onTick() {
        List<IAEFluidStack> changes = this.cache.detectChanges();
        if (!changes.isEmpty()) {
            this.postDifference(changes);
            return TickRateModulation.URGENT;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
        return this.cache.getAvailable(out);
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

}
