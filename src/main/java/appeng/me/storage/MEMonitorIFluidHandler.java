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

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.ExactFluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import appeng.core.Api;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.util.AEFluidStack;

import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;
import appeng.core.Api;

public class MEMonitorIFluidHandler implements IMEMonitor<IAEFluidStack>, ITickingMonitor {
    private final GroupedFluidInv handler;
    private final IItemList<IAEFluidStack> list = Api.instance().storage().getStorageChannel(IFluidStorageChannel.class)
            .createList();
    private final HashMap<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();
    private final Map<FluidKey, CachedFluidStack> memory;
    private IActionSource mySource;
    private StorageFilter mode = StorageFilter.EXTRACTABLE_ONLY;

    public MEMonitorIFluidHandler(final GroupedFluidInv handler) {
        this.handler = handler;
        this.memory = new HashMap<>();
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEFluidStack> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEFluidStack> l) {
        this.listeners.remove(l);
    }

    private Simulation getFluidAction(Actionable actionable){
        return actionable == Actionable.MODULATE ? Simulation.ACTION : Simulation.SIMULATE;
    }

    @Override
    public IAEFluidStack injectItems(final IAEFluidStack input, final Actionable type, final IActionSource src) {
        FluidVolume toFill = input.getFluidStack();
        final FluidVolume excess = this.handler.attemptInsertion(toFill, getFluidAction(type));

        if (excess.equals(toFill)) {
            return input.copy();
        }

        if (type == Actionable.MODULATE) {
            this.onTick();
        }

        if (excess.isEmpty()) {
            return null;
        }

        return AEFluidStack.fromFluidVolume(excess, RoundingMode.DOWN);
    }

    @Override
    public IAEFluidStack extractItems(final IAEFluidStack request, final Actionable type, final IActionSource src) {
        FluidAmount amount = request.getAmount();
        ExactFluidFilter filter = new ExactFluidFilter(request.getFluid());
        final FluidVolume removed = this.handler.attemptExtraction(filter, amount, getFluidAction(type));

        if (removed.isEmpty()) {
            return null;
        }

        if (type == Actionable.MODULATE) {
            this.onTick();
        }

        return AEFluidStack.fromFluidVolume(removed, RoundingMode.DOWN);
    }

    @Override
    public IStorageChannel getChannel() {
        return Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    private static final FluidAmount MIN_EXTRACTION_AMOUNT = FluidAmount.of(1, 1000);

    @Override
    public TickRateModulation onTick() {
        final List<IAEFluidStack> changes = new ArrayList<>();

        this.list.resetStatus();
        boolean changed = false;

        Set<FluidKey> storedFluids = handler.getStoredFluids();
        for (FluidKey storedFluid : storedFluids) {
            CachedFluidStack old = this.memory.get(storedFluid);

            // FIXME FABRIC: This is doing a bunch of work that is likely unnecessary
            FluidAmount newAmount = getCurrentAmount(storedFluid);
            FluidAmount oldAmount = old == null ? FluidAmount.ZERO : old.volume.amount();

            if (!newAmount.equals(oldAmount)) {
                final CachedFluidStack cis = new CachedFluidStack(storedFluid.withAmount(newAmount));
                this.memory.put(storedFluid, cis);

                if (old != null && old.aeStack != null) {
                    old.aeStack.setStackSize(-old.aeStack.getStackSize());
                    changes.add(old.aeStack);
                }

                if (cis.aeStack != null) {
                    changes.add(cis.aeStack);
                    this.list.add(cis.aeStack);
                }

                changed = true;
            } else {
                final long newSize = newAmount.isZero() ? 0 : newAmount.asLong(1000, RoundingMode.DOWN);
                final long diff = newSize - (oldAmount.isZero() ? 0 : oldAmount.asLong(1000, RoundingMode.DOWN));

                IAEFluidStack stack = null;

                if (!newAmount.isZero()) {
                    stack = (old == null || old.aeStack == null ? AEFluidStack.fromFluidVolume(storedFluid.withAmount(newAmount), RoundingMode.DOWN) : old.aeStack.copy());
                }
                if (stack != null) {
                    stack.setStackSize(newSize);
                    this.list.add(stack);
                }

                if (diff != 0 && stack != null) {
                    final CachedFluidStack cis = new CachedFluidStack(storedFluid.withAmount(newAmount));
                    this.memory.put(storedFluid, cis);

                    final IAEFluidStack a = stack.copy();
                    a.setStackSize(diff);
                    changes.add(a);
                    changed = true;
                }
            }
        }

        // detect dropped items; should fix non IISided Inventory Changes.
        Set<FluidKey> toRemove = null;
        for (final Entry<FluidKey, CachedFluidStack> entry : memory.entrySet()) {
            if (storedFluids.contains(entry.getKey())) {
                continue; // Still stored
            }

            if (toRemove == null) {
                toRemove = new HashSet<>();
            }
            toRemove.add(entry.getKey());

            if (entry.getValue().aeStack != null) {
                final IAEFluidStack a = entry.getValue().aeStack.copy();
                a.setStackSize(-a.getStackSize());
                changes.add(a);
                changed = true;
            }
        }
        // Now clean up if any removed entries were found
        if (toRemove != null) {
            for (FluidKey fluidKey : toRemove) {
                memory.remove(fluidKey);
            }
        }

        if (!changes.isEmpty()) {
            this.postDifference(changes);
        }

        return changed ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    private FluidAmount getCurrentAmount(FluidKey storedFluid) {
        FluidAmount newAmount = this.handler.getAmount_F(storedFluid);
        if (!newAmount.isZero() && this.getMode() == StorageFilter.EXTRACTABLE_ONLY) {
            // We have to actually check if we could extract _anything_
            ExactFluidFilter filter = new ExactFluidFilter(storedFluid);
            if (this.handler.attemptExtraction(filter, MIN_EXTRACTION_AMOUNT, Simulation.SIMULATE).isEmpty()) {
                // Just to safeguard against tanks that prevent non-bucket-size extractions
                if (this.handler.attemptExtraction(filter, FluidAmount.BUCKET, Simulation.SIMULATE).isEmpty()) {
                    newAmount = FluidAmount.ZERO;
                }
            }
        }
        return newAmount;
    }

    private static boolean isDifferent(FluidVolume a, FluidVolume b) {
        if (a == b) {
            return false;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return true;
        }
        return !a.getFluidKey().equals(b.getFluidKey());
    }

    private void postDifference(final Iterable<IAEFluidStack> a) {
        if (a != null) {
            final Iterator<Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object>> i = this.listeners.entrySet()
                    .iterator();
            while (i.hasNext()) {
                final Entry<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> l = i.next();
                final IMEMonitorHandlerReceiver<IAEFluidStack> key = l.getKey();
                if (key.isValid(l.getValue())) {
                    key.postChange(this, a, this.getActionSource());
                } else {
                    i.remove();
                }
            }
        }
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(final IAEFluidStack input) {
        return false;
    }

    @Override
    public boolean canAccept(final IAEFluidStack input) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(final int i) {
        return true;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(final IItemList out) {
        for (final CachedFluidStack is : this.memory.values()) {
            out.addStorage(is.aeStack);
        }

        return out;
    }

    @Override
    public IItemList<IAEFluidStack> getStorageList() {
        return this.list;
    }

    private StorageFilter getMode() {
        return this.mode;
    }

    public void setMode(final StorageFilter mode) {
        this.mode = mode;
    }

    private IActionSource getActionSource() {
        return this.mySource;
    }

    @Override
    public void setActionSource(final IActionSource mySource) {
        this.mySource = mySource;
    }

    private static class CachedFluidStack {

        private final FluidVolume volume;
        private final IAEFluidStack aeStack;

        CachedFluidStack(FluidVolume volume) {
            this.aeStack = AEFluidStack.fromFluidVolume(volume, RoundingMode.DOWN);
            if (aeStack != null) {
                // This ensures that the amount is equal if it was rounded down
                this.volume = aeStack.getFluidStack();
            } else {
                this.volume = FluidVolumeUtil.EMPTY;
            }
        }
    }
}
