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

package appeng.blockentity.misc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.KeyCounter;
import appeng.me.helpers.BaseActionSource;
import appeng.me.storage.ITickingMonitor;

class CondenserItemInventory implements IMEMonitor<AEItemKey>, ITickingMonitor {
    private final HashMap<IMEMonitorListener<AEItemKey>, Object> listeners = new HashMap<>();
    private final CondenserBlockEntity target;
    private boolean hasChanged = true;
    private final KeyCounter<AEItemKey> cachedList = new KeyCounter<>();
    private IActionSource actionSource = new BaseActionSource();
    private Set<AEItemKey> changeSet = new HashSet<>();

    CondenserItemInventory(final CondenserBlockEntity te) {
        this.target = te;
    }

    @Override
    public long insert(AEItemKey what, long amount, Actionable mode, IActionSource source) {
        IMEInventory.checkPreconditions(what, amount, mode, source);
        if (mode == Actionable.MODULATE) {
            this.target.addPower(amount);
        }
        return amount;
    }

    @Override
    public long extract(AEItemKey what, long amount, Actionable mode, IActionSource source) {
        IMEInventory.checkPreconditions(what, amount, mode, source);
        var slotItem = this.target.getOutputSlot().getStackInSlot(0);

        if (what.matches(slotItem)) {
            int count = (int) Math.min(amount, Integer.MAX_VALUE);
            return this.target.getOutputSlot().extractItem(0, count, mode == Actionable.SIMULATE)
                    .getCount();
        }

        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter<AEItemKey> out) {
        var stack = this.target.getOutputSlot().getStackInSlot(0);
        if (!stack.isEmpty()) {
            out.add(AEItemKey.of(stack), stack.getCount());
        }
    }

    @Override
    public KeyCounter<AEItemKey> getCachedAvailableStacks() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.clear();
            this.getAvailableStacks(this.cachedList);
        }
        return this.cachedList;
    }

    @Override
    public IStorageChannel<AEItemKey> getChannel() {
        return StorageChannels.items();
    }

    @Override
    public void addListener(IMEMonitorListener<AEItemKey> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(IMEMonitorListener<AEItemKey> l) {
        this.listeners.remove(l);
    }

    public void updateOutput(ItemStack added, ItemStack removed) {
        this.hasChanged = true;
        if (!added.isEmpty()) {
            this.changeSet.add(AEItemKey.of(added));
        }
        if (!removed.isEmpty()) {
            this.changeSet.add(AEItemKey.of(removed));
        }
    }

    @Override
    public TickRateModulation onTick() {
        var currentChanges = this.changeSet;

        if (currentChanges.isEmpty()) {
            return TickRateModulation.IDLE;
        }

        this.changeSet = new HashSet<>();
        var i = this.listeners.entrySet().iterator();
        while (i.hasNext()) {
            var l = i.next();
            var key = l.getKey();
            if (key.isValid(l.getValue())) {
                key.postChange(this, currentChanges, this.actionSource);
            } else {
                i.remove();
            }
        }

        return TickRateModulation.URGENT;
    }

    @Override
    public void setActionSource(IActionSource actionSource) {
        this.actionSource = actionSource;
    }
}
