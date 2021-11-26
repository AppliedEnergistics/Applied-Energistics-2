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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Queues;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.MEStorage;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.me.helpers.InterestManager;
import appeng.me.storage.NetworkStorage;
import appeng.me.storage.StackWatcher;

/**
 * Wraps a {@link NetworkStorage} and adds change detection.
 */
public class NetworkInventoryMonitor implements MEMonitorStorage {
    private static final Deque<NetworkInventoryMonitor> GLOBAL_DEPTH = Queues.newArrayDeque();

    private final InterestManager<StackWatcher> interestManager;
    private final KeyCounter cachedList;
    private final Map<IMEMonitorListener, Object> listeners;
    @Nullable
    private MEStorage storage;

    private boolean hasChangedLastTick = false;
    private boolean hasChanged = false;
    @Nonnegative
    private int recursionDepth = 0;

    public NetworkInventoryMonitor(NetworkStorage storage, InterestManager<StackWatcher> interestManager) {
        this.interestManager = interestManager;
        this.cachedList = new KeyCounter();
        this.listeners = new HashMap<>();
        this.storage = storage;
    }

    @Override
    public void addListener(IMEMonitorListener l, Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (storage == null) {
            return 0;
        }

        if (mode == Actionable.SIMULATE) {
            return storage.extract(what, amount, mode, source);
        }

        this.recursionDepth++;
        var extracted = storage.extract(what, amount, mode, source);
        this.recursionDepth--;

        if (this.recursionDepth == 0) {
            this.monitorDifference(what, -extracted, source);
        }

        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (storage != null) {
            storage.getAvailableStacks(out);
        }
    }

    @Nonnull
    @Override
    public KeyCounter getCachedAvailableStacks() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.reset();
            this.getAvailableStacks(this.cachedList);
            this.cachedList.removeZeros();
        }

        return this.cachedList;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (storage == null) {
            return 0;
        }

        if (mode == Actionable.SIMULATE) {
            return storage.insert(what, amount, mode, source);
        }

        this.recursionDepth++;
        var inserted = storage.insert(what, amount, mode, source);
        this.recursionDepth--;

        if (this.recursionDepth == 0) {
            monitorDifference(what, inserted, source);
        }

        return inserted;
    }

    @Override
    public void removeListener(IMEMonitorListener l) {
        this.listeners.remove(l);
    }

    private Iterator<Entry<IMEMonitorListener, Object>> getListeners() {
        return this.listeners.entrySet().iterator();
    }

    private void monitorDifference(AEKey what, long difference, IActionSource source) {
        if (difference != 0) {
            this.postChangesToListeners(Collections.singleton(what), source);
        }
    }

    private void postChangesToListeners(Iterable<AEKey> changes, final IActionSource src) {
        this.postChange(changes, src);
    }

    protected void postChange(Iterable<AEKey> changes, IActionSource src) {
        if (this.recursionDepth > 0 || GLOBAL_DEPTH.contains(this)) {
            return;
        }

        GLOBAL_DEPTH.push(this);
        this.recursionDepth++;

        this.hasChangedLastTick = true;

        this.notifyListenersOfChange(changes, src);

        for (AEKey changedItem : changes) {
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

    private void notifyListenersOfChange(Iterable<AEKey> diff, IActionSource src) {
        this.hasChanged = true;
        var i = this.getListeners();

        while (i.hasNext()) {
            final Entry<IMEMonitorListener, Object> o = i.next();
            final IMEMonitorListener receiver = o.getKey();
            if (receiver.isValid(o.getValue())) {
                receiver.postChange(this, diff, src);
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
