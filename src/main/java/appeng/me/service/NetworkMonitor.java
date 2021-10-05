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

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;

import appeng.api.config.Actionable;
import appeng.api.networking.events.GridStorageEvent;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.ItemWatcher;

public class NetworkMonitor<T extends IAEStack> implements IMEMonitor<T> {
    @Nonnull
    private static final Deque<NetworkMonitor<?>> GLOBAL_DEPTH = Queues.newArrayDeque();

    @Nonnull
    private final StorageService service;
    @Nonnull
    private final IStorageChannel<T> myChannel;
    @Nonnull
    private final IItemList<T> cachedList;
    @Nonnull
    private final Map<IMEMonitorHandlerReceiver<T>, Object> listeners;

    private boolean sendEvent = false;
    private boolean hasChanged = false;
    @Nonnegative
    private int localDepthSemaphore = 0;

    public NetworkMonitor(final StorageService service, final IStorageChannel<T> chan) {
        this.service = service;
        this.myChannel = chan;
        this.cachedList = chan.createList();
        this.listeners = new HashMap<>();
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<T> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public T extractItems(final T request, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.SIMULATE) {
            return this.getHandler().extractItems(request, mode, src);
        }

        this.localDepthSemaphore++;
        final T leftover = this.getHandler().extractItems(request, mode, src);
        this.localDepthSemaphore--;

        if (this.localDepthSemaphore == 0) {
            this.monitorDifference(IAEStack.copy(request), leftover, true, src);
        }

        return leftover;
    }

    @Override
    public IItemList<T> getAvailableItems(final IItemList<T> out) {
        return this.getHandler().getAvailableItems(out);
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return myChannel;
    }

    @Nonnull
    @Override
    public IItemList<T> getStorageList() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.resetStatus();
            return this.getAvailableItems(this.cachedList);
        }

        return this.cachedList;
    }

    @Override
    public T injectItems(final T input, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.SIMULATE) {
            return this.getHandler().injectItems(input, mode, src);
        }

        this.localDepthSemaphore++;
        final T leftover = this.getHandler().injectItems(input, mode, src);
        this.localDepthSemaphore--;

        if (this.localDepthSemaphore == 0) {
            this.monitorDifference(IAEStack.copy(input), leftover, false, src);
        }

        return leftover;
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<T> l) {
        this.listeners.remove(l);
    }

    private IMEInventoryHandler<T> getHandler() {
        return this.service.getInventoryHandler(this.myChannel);
    }

    private Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> getListeners() {
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
        final Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.getListeners();

        while (i.hasNext()) {
            final Entry<IMEMonitorHandlerReceiver<T>, Object> o = i.next();
            final IMEMonitorHandlerReceiver<T> receiver = o.getKey();
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

        this.sendEvent = true;

        this.notifyListenersOfChange(changes, src);

        for (final T changedItem : changes) {
            T difference = changedItem;

            if (!add && changedItem != null) {
                difference = IAEStack.copy(changedItem);
                difference.setStackSize(-changedItem.getStackSize());
            }

            if (this.service.getInterestManager().containsKey(changedItem)) {
                final Collection<ItemWatcher> list = this.service.getInterestManager().get(changedItem);

                if (!list.isEmpty()) {
                    IAEStack fullStack = this.getStorageList().findPrecise(changedItem);

                    if (fullStack == null) {
                        fullStack = IAEStack.copy(changedItem);
                        fullStack.setStackSize(0);
                    }

                    this.service.getInterestManager().enableTransactions();

                    for (final ItemWatcher iw : list) {
                        iw.getHost().onStackChange(this.getStorageList(), fullStack, difference, src,
                                this.getChannel());
                    }

                    this.service.getInterestManager().disableTransactions();
                }
            }
        }

        final NetworkMonitor<?> last = GLOBAL_DEPTH.pop();
        this.localDepthSemaphore--;

        if (last != this) {
            throw new IllegalStateException("Invalid Access to Networked Storage API detected.");
        }
    }

    void forceUpdate() {
        this.hasChanged = true;

        final Iterator<Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.getListeners();
        while (i.hasNext()) {
            final Entry<IMEMonitorHandlerReceiver<T>, Object> o = i.next();
            final IMEMonitorHandlerReceiver<T> receiver = o.getKey();

            if (receiver.isValid(o.getValue())) {
                receiver.onListUpdate();
            } else {
                i.remove();
            }
        }
    }

    void onTick() {
        if (this.sendEvent) {
            this.sendEvent = false;
            this.service.getGrid().postEvent(new GridStorageEvent(this, this.myChannel));
        }
    }
}
