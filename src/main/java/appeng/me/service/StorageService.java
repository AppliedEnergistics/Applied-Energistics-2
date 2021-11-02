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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.events.GridStorageEvent;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityService;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherNode;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.InterestManager;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.NetworkInventory;
import appeng.me.storage.StackWatcher;

public class StorageService implements IStorageService, IGridServiceProvider {
    static {
        GridHelper.addGridServiceEventHandler(GridCellArrayUpdate.class, IStorageService.class,
                (service, evt) -> {
                    ((StorageService) service).cellUpdate();
                });
    }

    private final IGrid grid;
    private final HashSet<ICellProvider> activeCellProviders = new HashSet<>();
    private final HashSet<ICellProvider> inactiveCellProviders = new HashSet<>();
    private final SetMultimap<IAEStack, StackWatcher> interests = HashMultimap.create();
    private final InterestManager<StackWatcher> interestManager = new InterestManager<>(this.interests);
    private final HashMap<IGridNode, IStackWatcher> watchers = new HashMap<>();
    private final Map<IStorageChannel<? extends IAEStack>, NetworkInventoryMonitor<?>> storageMonitors;
    private final SecurityService security;

    public StorageService(IGrid g, ISecurityService security) {
        this.grid = g;
        this.security = (SecurityService) security;
        this.storageMonitors = new IdentityHashMap<>(StorageChannels.getAll().size());
        for (var channel : StorageChannels.getAll()) {
            storageMonitors.put(channel, createInventory(channel));
        }
    }

    private <T extends IAEStack> NetworkInventoryMonitor<T> createInventory(IStorageChannel<T> channel) {
        return new NetworkInventoryMonitor<>(interestManager, buildNetworkStorage(channel), channel);
    }

    @Override
    public void onServerEndTick() {
        for (var monitor : this.storageMonitors.values()) {
            if (monitor.hasChangedLastTick()) {
                monitor.clearHasChangedLastTick();
                grid.postEvent(new GridStorageEvent(monitor));
            }
        }
    }

    @Override
    public void addNode(final IGridNode node) {
        var cellProvider = node.getService(ICellProvider.class);
        if (cellProvider != null) {
            this.inactiveCellProviders.add(cellProvider);

            this.getGrid().postEvent(new GridCellArrayUpdate());

            if (node.isActive()) {
                final CellChangeTracker tracker = new CellChangeTracker();

                this.addCellProvider(cellProvider, tracker);
                tracker.applyChanges();
            }
        }

        var watcher = node.getService(IStackWatcherNode.class);
        if (watcher != null) {
            final StackWatcher iw = new StackWatcher(this, watcher);
            this.watchers.put(node, iw);
            watcher.updateWatcher(iw);
        }
    }

    @Override
    public void removeNode(IGridNode node) {
        var cellProvider = node.getService(ICellProvider.class);
        if (cellProvider != null) {
            var tracker = new CellChangeTracker();

            this.removeCellProvider(cellProvider, tracker);
            this.inactiveCellProviders.remove(cellProvider);
            this.getGrid().postEvent(new GridCellArrayUpdate());

            tracker.applyChanges();
        }

        var watcher = this.watchers.remove(node);
        if (watcher != null) {
            watcher.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IAEStack> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        return (IMEMonitor<T>) this.storageMonitors.get(channel);
    }

    private CellChangeTracker addCellProvider(final ICellProvider cc, final CellChangeTracker tracker) {
        if (this.inactiveCellProviders.contains(cc)) {
            this.inactiveCellProviders.remove(cc);
            this.activeCellProviders.add(cc);

            final IActionSource actionSrc = cc instanceof IActionHost ? new MachineSource((IActionHost) cc)
                    : new BaseActionSource();

            this.storageMonitors.forEach((channel, monitor) -> {
                for (var h : cc.getCellArray(channel)) {
                    tracker.postAddHandler(h, actionSrc);
                }
            });
        }

        return tracker;
    }

    private CellChangeTracker removeCellProvider(ICellProvider cc, CellChangeTracker tracker) {
        if (this.activeCellProviders.contains(cc)) {
            this.activeCellProviders.remove(cc);
            this.inactiveCellProviders.add(cc);

            var actionSrc = cc instanceof IActionHost ? new MachineSource((IActionHost) cc)
                    : new BaseActionSource();

            this.storageMonitors.forEach((channel, monitor) -> {
                for (var h : cc.getCellArray(channel)) {
                    tracker.postRemoveHandler(h, actionSrc);
                }
            });
        }

        return tracker;
    }

    public void cellUpdate() {
        var ll = new ArrayList<ICellProvider>();
        ll.addAll(this.inactiveCellProviders);
        ll.addAll(this.activeCellProviders);

        var tracker = new CellChangeTracker();

        for (var cc : ll) {
            boolean active = true;

            if (cc instanceof IActionHost) {
                var node = ((IActionHost) cc).getActionableNode();
                if (node == null || !node.isActive()) {
                    active = false;
                }
            }

            if (active) {
                this.addCellProvider(cc, tracker);
            } else {
                this.removeCellProvider(cc, tracker);
            }
        }

        for (var monitor : this.storageMonitors.values()) {
            updateNetworkInventory(monitor);

        }

        tracker.applyChanges();
    }

    private <T extends IAEStack> void updateNetworkInventory(NetworkInventoryMonitor<T> monitor) {
        monitor.setNetworkInventory(buildNetworkStorage(monitor.getChannel()));
    }

    private <T extends IAEStack> void postChangesToNetwork(IStorageChannel<T> chan,
            int upOrDown,
            IAEStackList<T> availableItems,
            IActionSource src) {
        this.storageMonitors.get(chan).postChange(upOrDown > 0, (Iterable) availableItems, src);
    }

    private <T extends IAEStack> NetworkInventory<T> buildNetworkStorage(IStorageChannel<T> channel) {
        var storageNetwork = new NetworkInventory<T>(security);

        for (final ICellProvider cc : this.activeCellProviders) {
            for (final IMEInventoryHandler<T> h : cc.getCellArray(channel)) {
                storageNetwork.addNewStorage(h);
            }
        }

        return storageNetwork;
    }

    @Override
    public <T extends IAEStack> void postAlterationOfStoredItems(IStorageChannel<T> chan,
            Iterable<T> input,
            final IActionSource src) {
        this.storageMonitors.get(chan).postChange(true, (Iterable) input, src);
    }

    @Override
    public void registerAdditionalCellProvider(final ICellProvider provider) {
        this.inactiveCellProviders.add(provider);
        this.addCellProvider(provider, new CellChangeTracker()).applyChanges();
    }

    @Override
    public void unregisterAdditionalCellProvider(final ICellProvider provider) {
        this.removeCellProvider(provider, new CellChangeTracker()).applyChanges();
        this.inactiveCellProviders.remove(provider);
    }

    public InterestManager<StackWatcher> getInterestManager() {
        return this.interestManager;
    }

    IGrid getGrid() {
        return this.grid;
    }

    private class CellChangeTracker {
        final List<CellChangeTrackerRecord<?>> data = new ArrayList<>();

        public <T extends IAEStack> void postAddHandler(IMEInventoryHandler<T> h,
                IActionSource actionSrc) {
            this.data.add(new CellChangeTrackerRecord<>(h.getChannel(), 1, h, actionSrc));
        }

        public <T extends IAEStack> void postRemoveHandler(IMEInventoryHandler<T> h,
                IActionSource actionSrc) {
            this.data.add(new CellChangeTrackerRecord<>(h.getChannel(), -1, h, actionSrc));
        }

        public void applyChanges() {
            for (var rec : this.data) {
                rec.applyChanges();
            }
        }
    }

    private class CellChangeTrackerRecord<T extends IAEStack> {
        final IStorageChannel<T> channel;
        final int up_or_down;
        final IAEStackList<T> list;
        final IActionSource src;

        public CellChangeTrackerRecord(final IStorageChannel<T> channel,
                final int i,
                final IMEInventoryHandler<T> h,
                final IActionSource actionSrc) {
            this.channel = channel;
            this.up_or_down = i;
            this.src = actionSrc;

            this.list = h.getAvailableItems(channel.createList());
        }

        public void applyChanges() {
            StorageService.this.postChangesToNetwork(this.channel, this.up_or_down, this.list, this.src);
        }
    }
}
