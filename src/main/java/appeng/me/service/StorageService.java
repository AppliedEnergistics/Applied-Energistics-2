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

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityService;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.GenericInterestManager;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.ItemWatcher;
import appeng.me.storage.NetworkInventoryHandler;

public class StorageService implements IStorageService, IGridServiceProvider {
    static {
        AEApi.grid().addGridServiceEventHandler(GridCellArrayUpdate.class, IStorageService.class,
                (service, evt) -> {
                    ((StorageService) service).cellUpdate();
                });
    }

    private final IGrid myGrid;
    private final HashSet<ICellProvider> activeCellProviders = new HashSet<>();
    private final HashSet<ICellProvider> inactiveCellProviders = new HashSet<>();
    private final SetMultimap<IAEStack, ItemWatcher> interests = HashMultimap.create();
    private final GenericInterestManager<ItemWatcher> interestManager = new GenericInterestManager<>(this.interests);
    private final HashMap<IGridNode, IStackWatcher> watchers = new HashMap<>();
    private Map<IStorageChannel<? extends IAEStack>, NetworkInventoryHandler<?>> storageNetworks;
    private Map<IStorageChannel<? extends IAEStack>, NetworkMonitor<?>> storageMonitors;

    public StorageService(final IGrid g) {
        this.myGrid = g;
        this.storageNetworks = new IdentityHashMap<>();
        this.storageMonitors = new IdentityHashMap<>();

        StorageChannels.getAll()
                .forEach(channel -> this.storageMonitors.put(channel, new NetworkMonitor<>(this, channel)));
    }

    @Override
    public void onServerEndTick() {
        this.storageMonitors.forEach((channel, monitor) -> monitor.onTick());
    }

    @Override
    public void removeNode(final IGridNode node) {
        var cellProvider = node.getService(ICellProvider.class);
        if (cellProvider != null) {
            final CellChangeTracker tracker = new CellChangeTracker();

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

        var watcher = node.getService(IStackWatcherHost.class);
        if (watcher != null) {
            final ItemWatcher iw = new ItemWatcher(this, watcher);
            this.watchers.put(node, iw);
            watcher.updateWatcher(iw);
        }
    }

    public <T extends IAEStack> IMEInventoryHandler<T> getInventoryHandler(IStorageChannel<T> channel) {
        return (IMEInventoryHandler<T>) this.storageNetworks.computeIfAbsent(channel, this::buildNetworkStorage);
    }

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
                    tracker.postChanges(channel, 1, h, actionSrc);
                }
            });
        }

        return tracker;
    }

    private CellChangeTracker removeCellProvider(final ICellProvider cc, final CellChangeTracker tracker) {
        if (this.activeCellProviders.contains(cc)) {
            this.activeCellProviders.remove(cc);
            this.inactiveCellProviders.add(cc);

            final IActionSource actionSrc = cc instanceof IActionHost ? new MachineSource((IActionHost) cc)
                    : new BaseActionSource();

            this.storageMonitors.forEach((channel, monitor) -> {
                for (var h : cc.getCellArray(channel)) {
                    tracker.postChanges(channel, -1, h, actionSrc);
                }
            });
        }

        return tracker;
    }

    public void cellUpdate() {
        this.storageNetworks.clear();

        final List<ICellProvider> ll = new ArrayList<ICellProvider>();
        ll.addAll(this.inactiveCellProviders);
        ll.addAll(this.activeCellProviders);

        final CellChangeTracker tracker = new CellChangeTracker();

        for (final ICellProvider cc : ll) {
            boolean active = true;

            if (cc instanceof IActionHost) {
                final IGridNode node = ((IActionHost) cc).getActionableNode();
                if (node != null && node.isActive()) {
                    active = true;
                } else {
                    active = false;
                }
            }

            if (active) {
                this.addCellProvider(cc, tracker);
            } else {
                this.removeCellProvider(cc, tracker);
            }
        }

        this.storageMonitors.forEach((channel, monitor) -> monitor.forceUpdate());

        tracker.applyChanges();
    }

    private <T extends IAEStack, C extends IStorageChannel<T>> void postChangesToNetwork(final C chan,
            final int upOrDown, final IItemList<T> availableItems, final IActionSource src) {
        this.storageMonitors.get(chan).postChange(upOrDown > 0, (Iterable) availableItems, src);
    }

    private <T extends IAEStack, C extends IStorageChannel<T>> NetworkInventoryHandler<T> buildNetworkStorage(
            final C chan) {
        var security = (SecurityService) this.getGrid().getService(ISecurityService.class);

        final NetworkInventoryHandler<T> storageNetwork = new NetworkInventoryHandler<T>(chan, security);

        for (final ICellProvider cc : this.activeCellProviders) {
            for (final IMEInventoryHandler<T> h : cc.getCellArray(chan)) {
                storageNetwork.addNewStorage(h);
            }
        }

        return storageNetwork;
    }

    @Override
    public <T extends IAEStack> void postAlterationOfStoredItems(IStorageChannel<T> chan,
            Iterable<? extends IAEStack> input,
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

    public GenericInterestManager<ItemWatcher> getInterestManager() {
        return this.interestManager;
    }

    IGrid getGrid() {
        return this.myGrid;
    }

    private class CellChangeTrackerRecord<T extends IAEStack> {

        final IStorageChannel<T> channel;
        final int up_or_down;
        final IItemList<T> list;
        final IActionSource src;

        public CellChangeTrackerRecord(final IStorageChannel<T> channel, final int i, final IMEInventoryHandler<T> h,
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

    private class CellChangeTracker<T extends IAEStack> {

        final List<CellChangeTrackerRecord<T>> data = new ArrayList<>();

        public void postChanges(final IStorageChannel<T> channel, final int i, final IMEInventoryHandler<T> h,
                final IActionSource actionSrc) {
            this.data.add(new CellChangeTrackerRecord<T>(channel, i, h, actionSrc));
        }

        public void applyChanges() {
            for (final CellChangeTrackerRecord<T> rec : this.data) {
                rec.applyChanges();
            }
        }
    }
}
