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

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.events.GridCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingLinkNexus;
import appeng.crafting.CraftingWatcher;
import appeng.helpers.CraftingPatternDetails;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.GenericInterestManager;
import appeng.tile.crafting.CraftingStorageTileEntity;
import appeng.tile.crafting.CraftingTileEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class CraftingGridService
        implements ICraftingGrid, IGridServiceProvider, ICraftingProviderHelper, ICellProvider, IMEInventoryHandler<IAEItemStack> {

    private static final ExecutorService CRAFTING_POOL;
    private static final Comparator<ICraftingPatternDetails> COMPARATOR = (firstDetail,
            nextDetail) -> nextDetail.getPriority() - firstDetail.getPriority();

    static {
        final ThreadFactory factory = ar -> {
            final Thread crafting = new Thread(ar, "AE Crafting Calculator");
            crafting.setDaemon(true);
            return crafting;
        };

        CRAFTING_POOL = Executors.newCachedThreadPool(factory);

        Api.instance().grid().addGridServiceEventHandler(GridCraftingPatternChange.class, ICraftingGrid.class, (service, event) -> {
            ((CraftingGridService) service).updatePatterns();
        });
        Api.instance().grid().addGridServiceEventHandler(GridCraftingCpuChange.class, ICraftingGrid.class, (service, event) -> {
            ((CraftingGridService) service).updateList = true;
        });
    }

    private final Set<CraftingCPUCluster> craftingCPUClusters = new HashSet<>();
    private final Set<ICraftingProvider> craftingProviders = new HashSet<>();
    private final Map<IGridNode, ICraftingWatcher> craftingWatchers = new HashMap<>();
    private final IGrid grid;
    private final Map<ICraftingPatternDetails, List<ICraftingMedium>> craftingMethods = new HashMap<>();
    private final Map<IAEItemStack, ImmutableList<ICraftingPatternDetails>> craftableItems = new HashMap<>();
    private final Set<IAEItemStack> emitableItems = new HashSet<>();
    private final Map<String, CraftingLinkNexus> craftingLinks = new HashMap<>();
    private final Multimap<IAEStack, CraftingWatcher> interests = HashMultimap.create();
    private final GenericInterestManager<CraftingWatcher> interestManager = new GenericInterestManager<>(
            this.interests);
    private final IStorageGrid storageGrid;
    private final IEnergyGrid energyGrid;
    private boolean updateList = false;

    public CraftingGridService(IGrid grid, IStorageGrid storageGrid, IEnergyGrid energyGrid) {
        this.grid = grid;
        this.storageGrid = storageGrid;
        this.energyGrid = energyGrid;

        this.storageGrid.registerCellProvider(this);
    }

    @Override
    public void onUpdateTick() {
        if (this.updateList) {
            this.updateList = false;
            this.updateCPUClusters();
        }

        this.craftingLinks.values().removeIf(nexus -> nexus.isDead(this.grid, this));

        for (final CraftingCPUCluster cpu : this.craftingCPUClusters) {
            cpu.updateCraftingLogic(this.grid, this.energyGrid, this);
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

        if (gridNode.getNodeOwner() instanceof CraftingTileEntity) {
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

        if (gridNode.getNodeOwner() instanceof CraftingTileEntity) {
            this.updateList = true;
        }
    }

    private void updatePatterns() {
        final Map<IAEItemStack, ImmutableList<ICraftingPatternDetails>> oldItems = this.craftableItems;

        // erase list.
        this.craftingMethods.clear();
        this.craftableItems.clear();
        this.emitableItems.clear();

        // update the stuff that was in the list...
        this.storageGrid.postAlterationOfStoredItems(
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class), oldItems.keySet(),
                new BaseActionSource());

        // re-create list..
        for (final ICraftingProvider provider : this.craftingProviders) {
            provider.provideCrafting(this);
        }

        final Map<IAEItemStack, Set<ICraftingPatternDetails>> tmpCraft = new HashMap<>();

        // new craftables!
        for (final ICraftingPatternDetails details : this.craftingMethods.keySet()) {
            for (IAEItemStack out : details.getOutputs()) {
                out = out.copy();
                out.reset();
                out.setCraftable(true);

                var methods = tmpCraft.computeIfAbsent(out, k -> new TreeSet<>(COMPARATOR));

                methods.add(details);
            }
        }

        // make them immutable
        for (final Entry<IAEItemStack, Set<ICraftingPatternDetails>> e : tmpCraft.entrySet()) {
            this.craftableItems.put(e.getKey(), ImmutableList.copyOf(e.getValue()));
        }

        this.storageGrid.postAlterationOfStoredItems(
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class), this.craftableItems.keySet(),
                new BaseActionSource());
    }

    private void updateCPUClusters() {
        this.craftingCPUClusters.clear();

        for (var tile : this.grid.getMachines(CraftingStorageTileEntity.class)) {
            final CraftingCPUCluster cluster = tile.getCluster();
            if (cluster != null) {
                this.craftingCPUClusters.add(cluster);

                if (cluster.getLastCraftingLink() != null) {
                    this.addLink((CraftingLink) cluster.getLastCraftingLink());
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
    public void addCraftingOption(final ICraftingMedium medium, final ICraftingPatternDetails api) {
        Preconditions.checkArgument(api.getClass() == CraftingPatternDetails.class,
                "Only supports internal ICraftingPatternDetails for now");
        List<ICraftingMedium> details = this.craftingMethods.get(api);
        if (details == null) {
            details = new ArrayList<>();
            details.add(medium);
            this.craftingMethods.put(api, details);
        } else {
            details.add(medium);
        }
    }

    @Override
    public void setEmitable(final IAEItemStack someItem) {
        this.emitableItems.add(someItem.copy());
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel<?> channel) {
        final List<IMEInventoryHandler> list = new ArrayList<>(1);

        if (channel == Api.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
            list.add(this);
        }

        return list;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.WRITE;
    }

    @Override
    public boolean isPrioritized(final IAEItemStack input) {
        return true;
    }

    @Override
    public boolean canAccept(final IAEItemStack input) {
        for (final CraftingCPUCluster cpu : this.craftingCPUClusters) {
            if (cpu.canAccept(input)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(final int i) {
        return i == 1;
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack input, final Actionable type, final IActionSource src) {
        for (final CraftingCPUCluster cpu : this.craftingCPUClusters) {
            input = cpu.injectItems(input, type, src);
        }

        return input;
    }

    @Override
    public IAEItemStack extractItems(final IAEItemStack request, final Actionable mode, final IActionSource src) {
        return null;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(final IItemList<IAEItemStack> out) {
        // add craftable items!
        for (final IAEItemStack stack : this.craftableItems.keySet()) {
            out.addCrafting(stack);
        }

        for (final IAEItemStack st : this.emitableItems) {
            out.addCrafting(st);
        }

        return out;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public ImmutableCollection<ICraftingPatternDetails> getCraftingFor(final IAEItemStack whatToCraft,
            final ICraftingPatternDetails details, final int slotIndex, final World world) {
        final ImmutableList<ICraftingPatternDetails> res = this.craftableItems.get(whatToCraft);

        if (res == null) {
            if (details != null && details.isCraftable()) {
                for (final IAEItemStack ais : this.craftableItems.keySet()) {
                    // TODO: check if OK
                    // TODO: this is slightly hacky, but fine as long as we only deal with
                    // itemstacks
                    if (ais.getItem() == whatToCraft.getItem()
                            && (!ais.getItem().isDamageable() || ais.getItemDamage() == whatToCraft.getItemDamage())
                            && details.isValidItemForSlot(slotIndex, ais.asItemStackRepresentation(), world)) {
                        return this.craftableItems.get(ais);
                    }
                }
            }

            return ImmutableSet.of();
        }

        return res;
    }

    @Override
    public Future<ICraftingJob> beginCraftingJob(final World world, final IGrid grid, final IActionSource actionSrc,
            final IAEItemStack slotItem, final ICraftingCallback cb) {
        if (world == null || grid == null || actionSrc == null || slotItem == null) {
            throw new IllegalArgumentException("Invalid Crafting Job Request");
        }

        final CraftingJob job = new CraftingJob(world, grid, actionSrc, slotItem, cb);

        return CRAFTING_POOL.submit(job, job);
    }

    @Override
    public ICraftingLink submitJob(final ICraftingJob job, final ICraftingRequester requestingMachine,
            final ICraftingCPU target, final boolean prioritizePower, final IActionSource src) {
        if (job.isSimulation()) {
            return null;
        }

        CraftingCPUCluster cpuCluster = null;

        if (target instanceof CraftingCPUCluster) {
            cpuCluster = (CraftingCPUCluster) target;
        }

        if (target == null) {
            final List<CraftingCPUCluster> validCpusClusters = new ArrayList<>();
            for (final CraftingCPUCluster cpu : this.craftingCPUClusters) {
                if (cpu.isActive() && !cpu.isBusy() && cpu.getAvailableStorage() >= job.getByteTotal()) {
                    validCpusClusters.add(cpu);
                }
            }

            Collections.sort(validCpusClusters, (firstCluster, nextCluster) -> {
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
        return ImmutableSet.copyOf(new ActiveCpuIterator(this.craftingCPUClusters));
    }

    @Override
    public boolean canEmitFor(final IAEItemStack someItem) {
        return this.emitableItems.contains(someItem);
    }

    @Override
    public boolean isRequesting(final IAEItemStack what) {
        return this.requesting(what) > 0;
    }

    @Override
    public long requesting(IAEItemStack what) {
        long requested = 0;

        for (final CraftingCPUCluster cluster : this.craftingCPUClusters) {
            final IAEItemStack stack = cluster.making(what);
            requested += stack != null ? stack.getStackSize() : 0;
        }

        return requested;
    }

    public List<ICraftingMedium> getMediums(final ICraftingPatternDetails key) {
        List<ICraftingMedium> mediums = this.craftingMethods.get(key);

        if (mediums == null) {
            mediums = ImmutableList.of();
        }

        return mediums;
    }

    public boolean hasCpu(final ICraftingCPU cpu) {
        return this.craftingCPUClusters.contains(cpu);
    }

    public GenericInterestManager<CraftingWatcher> getInterestManager() {
        return this.interestManager;
    }

    private static class ActiveCpuIterator implements Iterator<ICraftingCPU> {

        private final Iterator<CraftingCPUCluster> iterator;
        private CraftingCPUCluster cpuCluster;

        public ActiveCpuIterator(final Collection<CraftingCPUCluster> o) {
            this.iterator = o.iterator();
            this.cpuCluster = null;
        }

        @Override
        public boolean hasNext() {
            this.findNext();

            return this.cpuCluster != null;
        }

        private void findNext() {
            while (this.iterator.hasNext() && this.cpuCluster == null) {
                this.cpuCluster = this.iterator.next();
                if (!this.cpuCluster.isActive() || this.cpuCluster.isDestroyed()) {
                    this.cpuCluster = null;
                }
            }
        }

        @Override
        public ICraftingCPU next() {
            final ICraftingCPU o = this.cpuCluster;
            this.cpuCluster = null;

            return o;
        }

        @Override
        public void remove() {
            // no..
        }
    }
}
