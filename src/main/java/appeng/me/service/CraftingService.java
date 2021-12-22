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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.*;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeyFilter;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingStorageBlockEntity;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingLinkNexus;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.InterestManager;
import appeng.me.helpers.StackWatcher;
import appeng.me.service.helpers.CraftingServiceStorage;
import appeng.me.service.helpers.NetworkCraftingProviders;

public class CraftingService implements ICraftingService, IGridServiceProvider {

    private static final ExecutorService CRAFTING_POOL;

    static {
        final ThreadFactory factory = ar -> {
            final Thread crafting = new Thread(ar, "AE Crafting Calculator");
            crafting.setDaemon(true);
            return crafting;
        };

        CRAFTING_POOL = Executors.newCachedThreadPool(factory);

        GridHelper.addGridServiceEventHandler(GridCraftingCpuChange.class, ICraftingService.class,
                (service, event) -> {
                    ((CraftingService) service).updateList = true;
                });
    }

    private final Set<CraftingCPUCluster> craftingCPUClusters = new HashSet<>();
    private final Map<IGridNode, StackWatcher<ICraftingWatcherNode>> craftingWatchers = new HashMap<>();
    private final IGrid grid;
    private final NetworkCraftingProviders craftingProviders = new NetworkCraftingProviders();
    private final Map<String, CraftingLinkNexus> craftingLinks = new HashMap<>();
    private final Multimap<AEKey, StackWatcher<ICraftingWatcherNode>> interests = HashMultimap.create();
    private final InterestManager<StackWatcher<ICraftingWatcherNode>> interestManager = new InterestManager<>(
            this.interests);
    private final IEnergyService energyGrid;
    private final Set<AEKey> currentlyCrafting = new HashSet<>();
    private boolean updateList = false;

    public CraftingService(IGrid grid, IStorageService storageGrid, IEnergyService energyGrid) {
        this.grid = grid;
        this.energyGrid = energyGrid;

        storageGrid.addGlobalStorageProvider(new CraftingServiceStorage(this));
    }

    @Override
    public void onServerEndTick() {
        if (this.updateList) {
            this.updateList = false;
            this.updateCPUClusters();
        }

        this.craftingLinks.values().removeIf(nexus -> nexus.isDead(this.grid, this));

        var previouslyCrafting = new HashSet<>(currentlyCrafting);
        this.currentlyCrafting.clear();
        for (CraftingCPUCluster cpu : this.craftingCPUClusters) {
            cpu.craftingLogic.tickCraftingLogic(energyGrid, this);

            cpu.craftingLogic.getAllWaitingFor(this.currentlyCrafting);
        }

        // Notify watchers about items no longer being crafted
        var changed = new HashSet<AEKey>();
        changed.addAll(Sets.difference(previouslyCrafting, currentlyCrafting));
        changed.addAll(Sets.difference(currentlyCrafting, previouslyCrafting));
        for (var what : changed) {
            for (var watcher : interestManager.get(what)) {
                watcher.getHost().onRequestChange(this, what);
            }
            for (var watcher : interestManager.getAllStacksWatchers()) {
                watcher.getHost().onRequestChange(this, what);
            }
        }
    }

    @Override
    public void removeNode(IGridNode gridNode) {

        var craftingWatcher = this.craftingWatchers.remove(gridNode);
        if (craftingWatcher != null) {
            craftingWatcher.reset();
        }

        var requester = gridNode.getService(ICraftingRequester.class);
        if (requester != null) {
            for (CraftingLinkNexus link : this.craftingLinks.values()) {
                if (link.isRequester(requester)) {
                    link.removeNode();
                }
            }
        }

        this.craftingProviders.removeProvider(gridNode);

        if (gridNode.getOwner() instanceof CraftingBlockEntity) {
            this.updateList = true;
        }
    }

    @Override
    public void addNode(IGridNode gridNode) {

        // The provider can already be added because it gets callback from the storage service,
        // in which it might already register itself before coming to this point.
        this.craftingProviders.removeProvider(gridNode);
        this.craftingProviders.addProvider(gridNode);

        var watchingNode = gridNode.getService(ICraftingWatcherNode.class);
        if (watchingNode != null) {
            var watcher = new StackWatcher<>(interestManager, watchingNode);
            this.craftingWatchers.put(gridNode, watcher);
            watchingNode.updateWatcher(watcher);
        }

        var craftingRequester = gridNode.getService(ICraftingRequester.class);
        if (craftingRequester != null) {
            for (ICraftingLink link : craftingRequester.getRequestedJobs()) {
                if (link instanceof CraftingLink) {
                    this.addLink((CraftingLink) link);
                }
            }
        }

        if (gridNode.getOwner() instanceof CraftingBlockEntity) {
            this.updateList = true;
        }
    }

    @Override
    public Set<AEKey> getCraftables(AEKeyFilter filter) {
        return craftingProviders.getCraftables(filter);
    }

    private void updateCPUClusters() {
        this.craftingCPUClusters.clear();

        for (var blockEntity : this.grid.getMachines(CraftingStorageBlockEntity.class)) {
            final CraftingCPUCluster cluster = blockEntity.getCluster();
            if (cluster != null) {
                this.craftingCPUClusters.add(cluster);

                ICraftingLink maybeLink = cluster.craftingLogic.getLastLink();
                if (maybeLink != null) {
                    this.addLink((CraftingLink) maybeLink);
                }
            }
        }

    }

    public void addLink(CraftingLink link) {
        if (link.isStandalone()) {
            return;
        }

        CraftingLinkNexus nexus = this.craftingLinks.get(link.getCraftingID());
        if (nexus == null) {
            this.craftingLinks.put(link.getCraftingID(), nexus = new CraftingLinkNexus(link.getCraftingID()));
        }

        link.setNexus(nexus);
    }

    public long insertIntoCpus(AEKey what, long amount, Actionable type) {
        long inserted = 0;
        for (var cpu : this.craftingCPUClusters) {
            inserted += cpu.craftingLogic.insert(what, amount, type);
        }

        return inserted;
    }

    @Override
    public Collection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
        return this.craftingProviders.getCraftingFor(whatToCraft);
    }

    @Override
    public void refreshNodeCraftingProvider(IGridNode node) {
        this.craftingProviders.removeProvider(node);
        this.craftingProviders.addProvider(node);
    }

    @Nullable
    @Override
    public AEKey getFuzzyCraftable(AEKey whatToCraft, AEKeyFilter filter) {
        return this.craftingProviders.getFuzzyCraftable(whatToCraft, filter);
    }

    @Override
    public Future<ICraftingPlan> beginCraftingCalculation(Level level, ICraftingSimulationRequester simRequester,
            AEKey what, long amount, CalculationStrategy strategy) {
        if (level == null || simRequester == null) {
            throw new IllegalArgumentException("Invalid Crafting Job Request");
        }

        final CraftingCalculation job = new CraftingCalculation(level, grid, simRequester,
                new GenericStack(what, amount), strategy);

        return CRAFTING_POOL.submit(job::run);
    }

    @Override
    public ICraftingLink submitJob(ICraftingPlan job, ICraftingRequester requestingMachine,
            ICraftingCPU target, boolean prioritizePower, IActionSource src) {
        if (job.simulation()) {
            return null;
        }

        CraftingCPUCluster cpuCluster = null;

        if (target instanceof CraftingCPUCluster) {
            cpuCluster = (CraftingCPUCluster) target;
        }

        if (target == null) {
            final List<CraftingCPUCluster> validCpusClusters = new ArrayList<>();
            for (CraftingCPUCluster cpu : this.craftingCPUClusters) {
                if (cpu.isActive() && !cpu.isBusy() && cpu.getAvailableStorage() >= job.bytes()) {
                    validCpusClusters.add(cpu);
                }
            }

            validCpusClusters.sort((firstCluster, nextCluster) -> {
                if (prioritizePower) {
                    final int comparison1 = Long.compare(nextCluster.getCoProcessors(), firstCluster.getCoProcessors());
                    if (comparison1 != 0) {
                        return comparison1;
                    }
                    return Long.compare(nextCluster.getAvailableStorage(), firstCluster.getAvailableStorage());
                }

                final int comparison2 = Long.compare(firstCluster.getCoProcessors(), nextCluster.getCoProcessors());
                if (comparison2 != 0) {
                    return comparison2;
                }
                return Long.compare(firstCluster.getAvailableStorage(), nextCluster.getAvailableStorage());
            });

            if (!validCpusClusters.isEmpty()) {
                cpuCluster = validCpusClusters.get(0);
            }
        }

        if (cpuCluster != null) {
            return cpuCluster.submitJob(this.grid, job, src, requestingMachine);
        }

        return null;
    }

    @Override
    public ImmutableSet<ICraftingCPU> getCpus() {
        var cpus = ImmutableSet.<ICraftingCPU>builder();
        for (CraftingCPUCluster cpu : this.craftingCPUClusters) {
            if (cpu.isActive() && !cpu.isDestroyed()) {
                cpus.add(cpu);
            }
        }
        return cpus.build();
    }

    @Override
    public boolean canEmitFor(AEKey someItem) {
        return this.craftingProviders.canEmitFor(someItem);
    }

    @Override
    public boolean isRequesting(AEKey what) {
        return currentlyCrafting.contains(what);
    }

    @Override
    public long getRequestedAmount(AEKey what) {
        long requested = 0;

        for (CraftingCPUCluster cluster : this.craftingCPUClusters) {
            requested += cluster.craftingLogic.getWaitingFor(what);
        }

        return requested;
    }

    @Override
    public boolean isRequestingAny() {
        return !currentlyCrafting.isEmpty();
    }

    public Iterable<ICraftingProvider> getProviders(IPatternDetails key) {
        return craftingProviders.getMediums(key);
    }

    public boolean hasCpu(ICraftingCPU cpu) {
        return this.craftingCPUClusters.contains(cpu);
    }
}
