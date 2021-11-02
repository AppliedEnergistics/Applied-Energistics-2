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

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.me.helpers.InterestManager;
import appeng.me.storage.NetworkInventory;
import appeng.me.storage.StackWatcher;

/**
 * Wraps a {@link NetworkInventory} and adds change detection.
 */
public class NetworkInventoryMonitor<T extends IAEStack> implements IMEMonitor<T> {
    private static final Deque<NetworkInventoryMonitor<?>> GLOBAL_DEPTH = Queues.newArrayDeque();

    private final InterestManager<StackWatcher> interestManager;
    private final IStorageChannel<T> channel;
    private final IAEStackList<T> cachedList;
    private final Map<IMEMonitorListener<T>, Object> listeners;
    private NetworkInventory<T> networkInventory;

    private boolean hasChangedLastTick = false;
    private boolean hasChanged = false;
    @Nonnegative
    private int localDepthSemaphore = 0;

    public NetworkInventoryMonitor(InterestManager<StackWatcher> interestManager,
            NetworkInventory<T> networkInventory,
            IStorageChannel<T> channel) {
        this.interestManager = interestManager;
        this.channel = channel;
        this.cachedList = channel.createList();
        this.listeners = new HashMap<>();
        this.networkInventory = Objects.requireNonNull(networkInventory);
    }

    public void setNetworkInventory(NetworkInventory<T> networkInventory) {
        this.networkInventory = Objects.requireNonNull(networkInventory);
        forceUpdate();
    }

    @Override
    public void addListener(final IMEMonitorListener<T> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public T extractItems(final T request, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.SIMULATE) {
            return networkInventory.extractItems(request, mode, src);
        }

        this.localDepthSemaphore++;
        final T leftover = networkInventory.extractItems(request, mode, src);
        this.localDepthSemaphore--;

        if (this.localDepthSemaphore == 0) {
            this.monitorDifference(IAEStack.copy(request), leftover, true, src);
        }

        return leftover;
    }

    @Override
    public IAEStackList<T> getAvailableStacks(final IAEStackList<T> out) {
        return networkInventory.getAvailableItems(out);
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return channel;
    }

    @Nonnull
    @Override
    public IAEStackList<T> getCachedAvailableStacks() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.resetStatus();
            return this.getAvailableStacks(this.cachedList);
        }

        return this.cachedList;
    }

    @Override
    public T injectItems(final T input, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.SIMULATE) {
            return networkInventory.injectItems(input, mode, src);
        }

        this.localDepthSemaphore++;
        final T leftover = networkInventory.injectItems(input, mode, src);
        this.localDepthSemaphore--;

        if (this.localDepthSemaphore == 0) {
            this.monitorDifference(IAEStack.copy(input), leftover, false, src);
        }

        return leftover;
    }

    @Override
    public void removeListener(final IMEMonitorListener<T> l) {
        this.listeners.remove(l);
    }

    private Iterator<Entry<IMEMonitorListener<T>, Object>> getListeners() {
        return this.listeners.entrySet().iterator();
    }

    private void monitorDifference(final T original, final T leftOvers, final boolean extraction,
            final IActionSource src) {
        final T diff = IAEStack.copy(original);

        if (extraction) {
            diff.setStackSize(leftOvers == null ? 0 : -leftOvers.getStackSize());
        } else if (leftOvers != null) {
            diff.decStackSize(leftOvers.getStackSize());
        }

        if (diff.getStackSize() != 0) {
            this.postChangesToListeners(ImmutableList.of(diff), src);
        }
    }

    private void notifyListenersOfChange(final Iterable<T> diff, final IActionSource src) {
        this.hasChanged = true;
        final Iterator<Entry<IMEMonitorListener<T>, Object>> i = this.getListeners();

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

    private void postChangesToListeners(final Iterable<T> changes, final IActionSource src) {
        this.postChange(true, changes, src);
    }

    protected void postChange(final boolean add, final Iterable<T> changes, final IActionSource src) {
        if (this.localDepthSemaphore > 0 || GLOBAL_DEPTH.contains(this)) {
            return;
        }

        GLOBAL_DEPTH.push(this);
        this.localDepthSemaphore++;

        this.hasChangedLastTick = true;

        this.notifyListenersOfChange(changes, src);

        for (final T changedItem : changes) {
            T difference = changedItem;

            if (!add && changedItem != null) {
                difference = IAEStack.copy(changedItem);
                difference.setStackSize(-changedItem.getStackSize());
            }

            if (interestManager.containsKey(changedItem)) {
                var list = interestManager.get(changedItem);

                if (!list.isEmpty()) {
                    IAEStack fullStack = this.getCachedAvailableStacks().findPrecise(changedItem);

                    if (fullStack == null) {
                        fullStack = IAEStack.copy(changedItem);
                        fullStack.setStackSize(0);
                    }

                    interestManager.enableTransactions();

                    for (var iw : list) {
                        iw.getHost().onStackChange(this.getCachedAvailableStacks(), fullStack, difference, src,
                                this.getChannel());
                    }

                    interestManager.disableTransactions();
                }
            }
        }

        final NetworkInventoryMonitor<?> last = GLOBAL_DEPTH.pop();
        this.localDepthSemaphore--;

        if (last != this) {
            throw new IllegalStateException("Invalid Access to Networked Storage API detected.");
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
