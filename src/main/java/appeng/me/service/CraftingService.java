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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.*;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.events.GridCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.GenericStack;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingStorageBlockEntity;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingLinkNexus;
import appeng.crafting.CraftingWatcher;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.InterestManager;
import appeng.me.service.helpers.CraftingServiceStorage;

public class CraftingService implements ICraftingService, IGridServiceProvider, ICraftingProviderHelper {

    private static final ExecutorService CRAFTING_POOL;

    static {
        final ThreadFactory factory = ar -> {
            final Thread crafting = new Thread(ar, "AE Crafting Calculator");
            crafting.setDaemon(true);
            return crafting;
        };

        CRAFTING_POOL = Executors.newCachedThreadPool(factory);

        GridHelper.addGridServiceEventHandler(GridCraftingPatternChange.class, ICraftingService.class,
                (service, event) -> {
                    ((CraftingService) service).updatePatterns();
                });
        GridHelper.addGridServiceEventHandler(GridCraftingCpuChange.class, ICraftingService.class,
                (service, event) -> {
                    ((CraftingService) service).updateList = true;
                });
    }

    private final Set<CraftingCPUCluster> craftingCPUClusters = new HashSet<>();
    private final Set<ICraftingProvider> craftingProviders = new HashSet<>();
    private final Map<IGridNode, ICraftingWatcher> craftingWatchers = new HashMap<>();
    private final IGrid grid;
    private final Map<IPatternDetails, SortedMultiset<CraftingMedium>> craftingMethods = new HashMap<>();
    private final Map<AEKey, ImmutableList<IPatternDetails>> craftableItems = new HashMap<>();
    /**
     * Used for looking up craftable alternatives using fuzzy search (i.e. ignore NBT).
     */
    private final KeyCounter<AEKey> craftableItemsList = new KeyCounter<>();
    private final Set<AEKey> emitableItems = new HashSet<>();
    private final Map<String, CraftingLinkNexus> craftingLinks = new HashMap<>();
    private final Multimap<AEKey, CraftingWatcher> interests = HashMultimap.create();
    private final InterestManager<CraftingWatcher> interestManager = new InterestManager<>((Multimap) this.interests);
    private final IStorageService storageGrid;
    private final IEnergyService energyGrid;
    private boolean updateList = false;

    public CraftingService(IGrid grid, IStorageService storageGrid, IEnergyService energyGrid) {
        this.grid = grid;
        this.storageGrid = storageGrid;
        this.energyGrid = energyGrid;

        this.storageGrid.addGlobalStorageProvider(new CraftingServiceStorage(this));
    }

    @Override
    public void onServerEndTick() {
        if (this.updateList) {
            this.updateList = false;
            this.updateCPUClusters();
        }

        this.craftingLinks.values().removeIf(nexus -> nexus.isDead(this.grid, this));

        for (final CraftingCPUCluster cpu : this.craftingCPUClusters) {
            cpu.craftingLogic.tickCraftingLogic(energyGrid, this);
        }
    }

    @Override
    public void removeNode(final IGridNode gridNode) {

        var craftingWatcher = this.craftingWatchers.remove(gridNode);
        if (craftingWatcher != null) {
            craftingWatcher.reset();
        }

        var requester = gridNode.getService(ICraftingRequester.class);
        if (requester != null) {
            for (final CraftingLinkNexus link : this.craftingLinks.values()) {
                if (link.isRequester(requester)) {
                    link.removeNode();
                }
            }
        }

        var provider = gridNode.getService(ICraftingProvider.class);
        if (provider != null) {
            this.craftingProviders.remove(provider);
            this.updatePatterns();
        }

        if (gridNode.getOwner() instanceof CraftingBlockEntity) {
            this.updateList = true;
        }
    }

    @Override
    public void addNode(IGridNode gridNode) {

        var watchingNode = gridNode.getService(ICraftingWatcherNode.class);
        if (watchingNode != null) {
            final CraftingWatcher watcher = new CraftingWatcher(this, watchingNode);
            this.craftingWatchers.put(gridNode, watcher);
            watchingNode.updateWatcher(watcher);
        }

        var craftingRequester = gridNode.getService(ICraftingRequester.class);
        if (craftingRequester != null) {
            for (final ICraftingLink link : craftingRequester.getRequestedJobs()) {
                if (link instanceof CraftingLink) {
                    this.addLink((CraftingLink) link);
                }
            }
        }

        var craftingProvider = gridNode.getService(ICraftingProvider.class);
        if (craftingProvider != null) {
            this.craftingProviders.add(craftingProvider);
            this.updatePatterns();
        }

        if (gridNode.getOwner() instanceof CraftingBlockEntity) {
            this.updateList = true;
        }
    }

    @Override
    public <T extends AEKey> Set<T> getCraftables(IStorageChannel<T> channel) {
        var result = new HashSet<T>();

        // add craftable items!
        for (var stack : this.craftableItems.keySet()) {
            if (stack.getChannel() == channel) {
                result.add(stack.cast(channel));
            }
        }

        for (var stack : this.emitableItems) {
            if (stack.getChannel() == channel) {
                result.add(stack.cast(channel));
            }
        }

        return result;
    }

    private void updatePatterns() {
        // erase list.
        this.craftingMethods.clear();
        this.craftableItems.clear();
        this.craftableItemsList.reset();
        this.emitableItems.clear();

        // re-create list..
        for (final ICraftingProvider provider : this.craftingProviders) {
            provider.provideCrafting(this);
        }

        final Map<AEKey, Set<IPatternDetails>> tmpCraft = new HashMap<>();
        // Sort by highest priority (that of the highest priority crafting medium).
        Comparator<IPatternDetails> detailsComparator = Comparator
                .comparing(details -> -this.craftingMethods.get(details).firstEntry().getElement().priority);
        // new craftables!
        for (var details : this.craftingMethods.keySet()) {
            var primaryOutputKey = details.getPrimaryOutput().what();
            tmpCraft.computeIfAbsent(primaryOutputKey, k -> new TreeSet<>(detailsComparator)).add(details);
        }

        // make them immutable
        for (var e : tmpCraft.entrySet()) {
            var output = e.getKey();
            this.craftableItems.put(output, ImmutableList.copyOf(e.getValue()));
            this.craftableItemsList.add(output, 1);
        }

        this.craftableItemsList.removeZeros();
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

    public void addLink(final CraftingLink link) {
        if (link.isStandalone()) {
            return;
        }

        CraftingLinkNexus nexus = this.craftingLinks.get(link.getCraftingID());
        if (nexus == null) {
            this.craftingLinks.put(link.getCraftingID(), nexus = new CraftingLinkNexus(link.getCraftingID()));
        }

        link.setNexus(nexus);
    }

    @Override
    public void addCraftingOption(ICraftingMedium medium, IPatternDetails api, int priority) {
        this.craftingMethods.computeIfAbsent(api, pattern -> TreeMultiset.create())
                .add(new CraftingMedium(medium, priority));
    }

    @Override
    public void setEmitable(AEKey someItem) {
        this.emitableItems.add(someItem);
    }

    public long insertIntoCpus(AEKey what, long amount, Actionable type) {
        long inserted = 0;
        for (var cpu : this.craftingCPUClusters) {
            inserted += cpu.craftingLogic.insert(what, amount, type);
        }

        return inserted;
    }

    @Override
    public ImmutableCollection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
        return this.craftableItems.getOrDefault(whatToCraft, ImmutableList.of());
    }

    @Nullable
    @Override
    public AEKey getFuzzyCraftable(AEKey whatToCraft, Predicate<AEKey> filter) {
        for (var fuzzy : craftableItemsList.findFuzzy(whatToCraft, FuzzyMode.IGNORE_ALL)) {
            if (filter.test(fuzzy.getKey())) {
                return fuzzy.getKey();
            }
        }
        return null;
    }

    @Override
    public Future<ICraftingPlan> beginCraftingCalculation(Level level, ICraftingSimulationRequester simRequester,
            AEKey what, long amount) {
        if (level == null || simRequester == null) {
            throw new IllegalArgumentException("Invalid Crafting Job Request");
        }

        final CraftingCalculation job = new CraftingCalculation(level, grid, simRequester,
                new GenericStack(what, amount));

        return CRAFTING_POOL.submit(job::run);
    }

    @Override
    public ICraftingLink submitJob(final ICraftingPlan job, final ICraftingRequester requestingMachine,
            final ICraftingCPU target, final boolean prioritizePower, final IActionSource src) {
        if (job.simulation()) {
            return null;
        }

        CraftingCPUCluster cpuCluster = null;

        if (target instanceof CraftingCPUCluster) {
            cpuCluster = (CraftingCPUCluster) target;
        }

        if (target == null) {
            final List<CraftingCPUCluster> validCpusClusters = new ArrayList<>();
            for (final CraftingCPUCluster cpu : this.craftingCPUClusters) {
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
        return this.emitableItems.contains(someItem);
    }

    @Override
    public boolean isRequesting(AEKey what) {
        return this.requesting(what) > 0;
    }

    @Override
    public long requesting(AEKey what) {
        long requested = 0;

        for (final CraftingCPUCluster cluster : this.craftingCPUClusters) {
            requested += cluster.craftingLogic.getWaitingFor(what);
        }

        return requested;
    }

    public Iterable<ICraftingMedium> getMediums(final IPatternDetails key) {
        var mediums = this.craftingMethods.get(key);

        if (mediums == null) {
            return Collections.emptyList();
        } else {
            return Iterables.transform(mediums, CraftingMedium::medium);
        }
    }

    public boolean hasCpu(final ICraftingCPU cpu) {
        return this.craftingCPUClusters.contains(cpu);
    }

    public InterestManager<CraftingWatcher> getInterestManager() {
        return this.interestManager;
    }

    private record CraftingMedium(ICraftingMedium medium, int priority) implements Comparable<CraftingMedium> {

        @Override
        public int compareTo(CraftingMedium o) {
            // Higher priority goes first.
            return o.priority - this.priority;
        }
    }
}
