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

package appeng.me.service;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Queues;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.me.helpers.InterestManager;
import appeng.me.storage.NetworkStorage;
import appeng.me.storage.StackWatcher;

/**
 * Wraps a {@link NetworkStorage} and adds change detection.
 */
public class NetworkInventoryMonitor<T extends AEKey> implements IMEMonitor<T> {
    private static final Deque<NetworkInventoryMonitor<?>> GLOBAL_DEPTH = Queues.newArrayDeque();

    private final InterestManager<StackWatcher> interestManager;
    private final IStorageChannel<T> channel;
    private final KeyCounter<T> cachedList;
    private final Map<IMEMonitorListener<T>, Object> listeners;
    @Nullable
    private IMEInventory<T> networkInventory;

    private boolean hasChangedLastTick = false;
    private boolean hasChanged = false;
    @Nonnegative
    private int recursionDepth = 0;

    public NetworkInventoryMonitor(InterestManager<StackWatcher> interestManager, IStorageChannel<T> channel) {
        this.interestManager = interestManager;
        this.channel = channel;
        this.cachedList = new KeyCounter<>();
        this.listeners = new HashMap<>();
    }

    public void setNetworkInventory(IMEInventory<T> networkInventory) {
        this.networkInventory = Objects.requireNonNull(networkInventory);
        forceUpdate();
    }

    @Override
    public void addListener(IMEMonitorListener<T> l, Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public long extract(T what, long amount, Actionable mode, IActionSource source) {
        if (networkInventory == null) {
            return 0;
        }

        if (mode == Actionable.SIMULATE) {
            return networkInventory.extract(what, amount, mode, source);
        }

        this.recursionDepth++;
        var extracted = networkInventory.extract(what, amount, mode, source);
        this.recursionDepth--;

        if (this.recursionDepth == 0) {
            this.monitorDifference(what, -extracted, source);
        }

        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter<T> out) {
        if (networkInventory != null) {
            networkInventory.getAvailableStacks(out);
        }
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return channel;
    }

    @Nonnull
    @Override
    public KeyCounter<T> getCachedAvailableStacks() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.reset();
            this.getAvailableStacks(this.cachedList);
            this.cachedList.removeZeros();
        }

        return this.cachedList;
    }

    @Override
    public long insert(T what, long amount, Actionable mode, IActionSource source) {
        if (networkInventory == null) {
            return 0;
        }

        if (mode == Actionable.SIMULATE) {
            return networkInventory.insert(what, amount, mode, source);
        }

        this.recursionDepth++;
        var inserted = networkInventory.insert(what, amount, mode, source);
        this.recursionDepth--;

        if (this.recursionDepth == 0) {
            monitorDifference(what, inserted, source);
        }

        return inserted;
    }

    @Override
    public void removeListener(IMEMonitorListener<T> l) {
        this.listeners.remove(l);
    }

    private Iterator<Entry<IMEMonitorListener<T>, Object>> getListeners() {
        return this.listeners.entrySet().iterator();
    }

    private void monitorDifference(T what, long difference, IActionSource source) {
        if (difference != 0) {
            this.postChangesToListeners(Collections.singleton(what), source);
        }
    }

    private void postChangesToListeners(Iterable<T> changes, final IActionSource src) {
        this.postChange(changes, src);
    }

    protected void postChange(Iterable<T> changes, IActionSource src) {
        if (this.recursionDepth > 0 || GLOBAL_DEPTH.contains(this)) {
            return;
        }

        GLOBAL_DEPTH.push(this);
        this.recursionDepth++;

        this.hasChangedLastTick = true;

        this.notifyListenersOfChange(changes, src);

        for (var changedItem : changes) {
            if (interestManager.containsKey(changedItem)) {
                var list = interestManager.get(changedItem);

                if (!list.isEmpty()) {
                    var amount = this.getCachedAvailableStacks().get(changedItem);

                    interestManager.enableTransactions();

                    for (var iw : list) {
                        iw.getHost().onStackChange(changedItem, amount);
                    }

                    interestManager.disableTransactions();
                }
            }
        }

        this.recursionDepth--;

        if (GLOBAL_DEPTH.pop() != this) {
            throw new IllegalStateException("Invalid Access to Networked Storage API detected.");
        }
    }

    private void notifyListenersOfChange(Iterable<T> diff, IActionSource src) {
        this.hasChanged = true;
        var i = this.getListeners();

        while (i.hasNext()) {
            final Entry<IMEMonitorListener<T>, Object> o = i.next();
            final IMEMonitorListener<T> receiver = o.getKey();
            if (receiver.isValid(o.getValue())) {
                receiver.postChange(this, diff, src);
            } else {
                i.remove();
            }
        }
    }

    private void forceUpdate() {
        this.hasChanged = true;

        var i = this.getListeners();
        while (i.hasNext()) {
            var o = i.next();
            var receiver = o.getKey();

            if (receiver.isValid(o.getValue())) {
                receiver.onListUpdate();
            } else {
                i.remove();
            }
        }
    }

    public boolean hasChangedLastTick() {
        return hasChangedLastTick;
    }

    public void clearHasChangedLastTick() {
        this.hasChangedLastTick = false;
    }
}
