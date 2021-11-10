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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.GridStorageEvent;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityService;
import appeng.api.networking.storage.IStackWatcherNode;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.AEKey;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.InterestManager;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.NetworkStorage;
import appeng.me.storage.StackWatcher;

public class StorageService implements IStorageService, IGridServiceProvider {

    private final IGrid grid;
    /**
     * Tracks the storage service's state for each grid node that provides storage to the network.
     */
    private final Map<IGridNode, ProviderState> nodeProviders = new IdentityHashMap<>();
    /**
     * Tracks state for storage providers that are provided by other grid services (i.e. crafting).
     */
    private final List<ProviderState> globalProviders = new ArrayList<>();
    private final SetMultimap<AEKey, StackWatcher> interests = HashMultimap.create();
    private final InterestManager<StackWatcher> interestManager = new InterestManager<>(this.interests);
    private final Map<IStorageChannel<?>, NetworkStorage<?>> storage;
    private final Map<IStorageChannel<?>, NetworkInventoryMonitor<?>> storageMonitors;
    private final SecurityService security;
    /**
     * When mounting many storage providers at once, we use a batch operation to prevent repeated rescans and rebuilds
     * of the network inventory.
     */
    @Nullable
    private MountBatchChange currentBatch;
    /**
     * Tracks the stack watcher associated with a given grid node. Needed to clean up watchers when the node leaves the
     * grid.
     */
    private final Map<IGridNode, StackWatcher> watchers = new IdentityHashMap<>();

    public StorageService(IGrid g, ISecurityService security) {
        this.grid = g;
        this.security = (SecurityService) security;
        this.storage = new IdentityHashMap<>(StorageChannels.getAll().size());
        this.storageMonitors = new IdentityHashMap<>(StorageChannels.getAll().size());
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

    /**
     * When a node joins the grid, we automatically register provided {@link IStorageProvider} and
     * {@link IStackWatcherNode}.
     */
    @Override
    public void addNode(IGridNode node) {
        var storageProvider = node.getService(IStorageProvider.class);
        if (storageProvider != null) {
            // Determine the logical source for inventory changes by this storage provider
            var actionSource = node.getOwner() instanceof IActionHost actionHost ? new MachineSource(actionHost)
                    : new BaseActionSource();

            ProviderState state = new ProviderState(storageProvider, actionSource);
            this.nodeProviders.put(node, state);
            if (node.isActive()) {
                try (var tracker = new MountBatchChange()) {
                    state.mount();
                }
            }
        }

        var watcher = node.getService(IStackWatcherNode.class);
        if (watcher != null) {
            var iw = new StackWatcher(this, watcher);
            this.watchers.put(node, iw);
            watcher.updateWatcher(iw);
        }
    }

    /**
     * When a node leaves the grid, we automatically unregister the previously registered {@link IStorageProvider} or
     * {@link appeng.api.networking.storage.IStackWatcher}.
     */
    @Override
    public void removeNode(IGridNode node) {
        var watcher = this.watchers.remove(node);
        if (watcher != null) {
            watcher.reset();
        }

        var providerState = this.nodeProviders.remove(node);
        if (providerState != null) {
            try (var tracker = new MountBatchChange()) {
                providerState.unmount();
            }
        }
    }

    @Override
    public <T extends AEKey> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        return getNetworkMonitor(channel);
    }

    @SuppressWarnings("unchecked")
    private <T extends AEKey> NetworkInventoryMonitor<T> getNetworkMonitor(IStorageChannel<T> channel) {
        return (NetworkInventoryMonitor<T>) storageMonitors.computeIfAbsent(channel, c -> {
            var monitor = new NetworkInventoryMonitor<>(interestManager, channel);
            var existingStorage = storage.get(channel);
            if (existingStorage != null) {
                monitor.setNetworkInventory((IMEInventory<T>) existingStorage);
            }
            return monitor;
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends AEKey> NetworkStorage<T> getOrCreateNetworkStorage(IStorageChannel<T> channel) {
        return (NetworkStorage<T>) storage.computeIfAbsent(channel, c -> {
            var storage = new NetworkStorage<>(channel, security);
            var monitor = storageMonitors.get(c);
            if (monitor != null) {
                ((NetworkInventoryMonitor<T>) monitor).setNetworkInventory(storage);
            }
            return storage;
        });
    }

    private <T extends AEKey> void postChangesToNetwork(IStorageChannel<T> channel,
            Iterable<T> changedItems,
            IActionSource src) {
        if (currentBatch != null) {
            currentBatch.pending.add(new PendingChangeNotification<>(channel, changedItems, src));
        } else {
            getNetworkMonitor(channel).postChange(changedItems, src);
        }
    }

    @Override
    public <T extends AEKey> void postAlterationOfStoredItems(IStorageChannel<T> channel,
            Iterable<T> input,
            IActionSource src) {
        postChangesToNetwork(channel, input, src);
    }

    @Override
    public void addGlobalStorageProvider(IStorageProvider provider) {
        var state = new ProviderState(provider);
        this.globalProviders.add(state);
        try (var tracker = new MountBatchChange()) {
            state.mount();
        }
    }

    @Override
    public void removeGlobalStorageProvider(IStorageProvider provider) {
        try (var tracker = new MountBatchChange()) {
            var it = this.globalProviders.iterator();
            while (it.hasNext()) {
                var state = it.next();
                if (state.provider == provider) {
                    it.remove();
                    state.unmount();
                }
            }
        }
    }

    @Override
    public void refreshNodeStorageProvider(IGridNode node) {
        var state = nodeProviders.get(node);
        if (state == null) {
            throw new IllegalArgumentException("The given node is not part of this grid or has no storage provider.");
        }
        try (var batch = new MountBatchChange()) {
            state.update();
        }
    }

    @Override
    public void refreshGlobalStorageProvider(IStorageProvider provider) {
        for (var state : globalProviders) {
            if (state.provider == provider) {
                try (var batch = new MountBatchChange()) {
                    state.update();
                }
                return;
            }
        }

        throw new IllegalArgumentException("The given node is not part of this grid or has no storage provider.");
    }

    public InterestManager<StackWatcher> getInterestManager() {
        return this.interestManager;
    }

    /**
     * Batches mount and unmount operations together to only notify storage listeners at the very end.
     */
    private class MountBatchChange implements AutoCloseable {
        private final List<PendingChangeNotification<?>> pending = new ArrayList<>();

        public MountBatchChange() {
            Preconditions.checkState(currentBatch == null, "Cannot perform multiple batch updates simultaneously");
            currentBatch = this;
        }

        @Override
        public void close() {
            Preconditions.checkState(currentBatch == this, "Batch update got somehow canceled");
            currentBatch = null;

            for (var pendingOp : this.pending) {
                apply(pendingOp);
            }
        }

        private <T extends AEKey> void apply(PendingChangeNotification<T> rec) {
            postChangesToNetwork(rec.channel(), rec.list(), rec.src);
        }
    }

    private record PendingChangeNotification<T extends AEKey> (
            IStorageChannel<T> channel,
            // The list of stacks in the added or removed inventory
            Iterable<T> list,
            // The action source used to notify about the changes caused by this operation.
            IActionSource src) {
    }

    /**
     * A {@link IStorageProvider}-specific mount table facade which allows the provider to easily mount/remount its
     * storage.
     */
    private class ProviderState implements IStorageMounts {
        private final IStorageProvider provider;
        private final IActionSource actionSource;
        private final Set<IMEInventory<?>> inventories = new HashSet<>();
        private boolean mounted;

        public ProviderState(IStorageProvider provider, IActionSource actionSource) {
            this.provider = provider;
            this.actionSource = actionSource;
        }

        public ProviderState(IStorageProvider provider) {
            this(provider, new BaseActionSource());
        }

        /**
         * Performs the first mount operation on this storage provider, which does not assume any of the provider's
         * inventories are currently mounted and need to be removed first.
         */
        private void mount() {
            Preconditions.checkState(!mounted, "Can't mount a provider's inventories when it's already mounted");

            mounted = true;
            provider.mountInventories(this);
        }

        @Override
        public <T extends AEKey> void mount(IMEInventory<T> inventory, int priority) {
            Preconditions.checkState(mounted, "Cannot use StorageMounts after the storage has been unmounted.");

            if (!inventories.add(inventory)) {
                throw new IllegalStateException("Cannot mount the same inventory twice.");
            }

            // Mount this inventory into the network storage
            var networkStorage = getOrCreateNetworkStorage(inventory.getChannel());
            networkStorage.mount(priority, inventory);

            postChangesToNetwork(inventory.getChannel(), inventory.getAvailableStacks().keySet(), actionSource);
        }

        public void update() {
            unmount();
            mount();
        }

        public void unmount() {
            if (!mounted) {
                return;
            }
            mounted = false;

            for (var inventory : inventories) {
                unmount(inventory);
            }
            inventories.clear();
        }

        private <T extends AEKey> void unmount(IMEInventory<T> inventory) {
            getOrCreateNetworkStorage(inventory.getChannel()).unmount(inventory);
            postChangesToNetwork(inventory.getChannel(), inventory.getAvailableStacks().keySet(), actionSource);
        }
    }
}
