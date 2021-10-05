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
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStackList;
import appeng.me.helpers.BaseActionSource;
import appeng.me.storage.ITickingMonitor;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;

class CondenserItemInventory implements IMEMonitor<IAEItemStack>, ITickingMonitor {
    private final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();
    private final CondenserBlockEntity target;
    private boolean hasChanged = true;
    private final ItemList cachedList = new ItemList();
    private IActionSource actionSource = new BaseActionSource();
    private ItemList changeSet = new ItemList();

    CondenserItemInventory(final CondenserBlockEntity te) {
        this.target = te;
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack input, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.MODULATE && input != null) {
            this.target.addPower(input.getStackSize());
        }
        return null;
    }

    @Override
    public IAEItemStack extractItems(final IAEItemStack request, final Actionable mode, final IActionSource src) {
        AEItemStack ret = null;
        ItemStack slotItem = this.target.getOutputSlot().getStackInSlot(0);
        if (!slotItem.isEmpty() && request.isSameType(slotItem)) {
            int count = (int) Math.min(request.getStackSize(), Integer.MAX_VALUE);
            ret = AEItemStack
                    .fromItemStack(this.target.getOutputSlot().extractItem(0, count, mode == Actionable.SIMULATE));
        }
        return ret;
    }

    @Override
    public IAEStackList<IAEItemStack> getAvailableItems(final IAEStackList<IAEItemStack> out) {
        if (!this.target.getOutputSlot().getStackInSlot(0).isEmpty()) {
            out.add(AEItemStack.fromItemStack(this.target.getOutputSlot().getStackInSlot(0)));
        }
        return out;
    }

    @Override
    public IAEStackList<IAEItemStack> getStorageList() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.resetStatus();
            return this.getAvailableItems(this.cachedList);
        }
        return this.cachedList;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return StorageChannels.items();
    }

    @Override
    public boolean validForPass(final int pass) {
        return pass == 2;
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEItemStack> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEItemStack> l) {
        this.listeners.remove(l);
    }

    public void updateOutput(ItemStack added, ItemStack removed) {
        this.hasChanged = true;
        if (!added.isEmpty()) {
            this.changeSet.add(AEItemStack.fromItemStack(added));
        }
        var change = AEItemStack.fromItemStack(removed);
        if (change != null) {
            change.setStackSize(-removed.getCount());
            this.changeSet.add(change);
        }
    }

    @Override
    public TickRateModulation onTick() {
        final ItemList currentChanges = this.changeSet;

        if (currentChanges.isEmpty()) {
            return TickRateModulation.IDLE;
        }

        this.changeSet = new ItemList();
        final Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.listeners.entrySet().iterator();
        while (i.hasNext()) {
            final Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> l = i.next();
            final IMEMonitorHandlerReceiver<IAEItemStack> key = l.getKey();
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
