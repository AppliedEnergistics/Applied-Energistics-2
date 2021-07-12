/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.me;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridCacheProvider;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.GridEvent;
import appeng.core.Api;
import appeng.core.registries.GridCacheRegistry;
import appeng.core.worlddata.WorldData;
import appeng.hooks.ticking.TickHandler;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

public class Grid implements IGrid {
    private final SetMultimap<Class<?>, IGridNode> machines = MultimapBuilder.hashKeys().hashSetValues().build();
    private final Map<Class<?>, IGridCacheProvider> caches;
    private GridNode pivot;
    private int priority; // how import is this network?
    private GridStorage myStorage;

    /**
     * Creates a new grid, sends the necessary events, and registers it to the tickhandler or other objects.
     *
     * @param center the pivot point of the new grid
     */
    public static Grid create(GridNode center) {
        Grid grid = new Grid(center);

        TickHandler.instance().addNetwork(grid);
        center.setGrid(grid);

        return grid;
    }

    private Grid(final GridNode center) {
        this.pivot = Objects.requireNonNull(center);

        var cacheRegistry = (GridCacheRegistry) Api.instance().registries().gridCache();
        this.caches = cacheRegistry.createCacheInstance(this);
    }

    int getPriority() {
        return this.priority;
    }

    IGridStorage getMyStorage() {
        return this.myStorage;
    }

    Collection<IGridCacheProvider> getProviders() {
        return this.caches.values();
    }

    @Override
    public int size() {
        return this.machines.size();
    }

    void remove(final GridNode gridNode) {
        for (var c : this.caches.values()) {
            c.removeNode(gridNode);
        }

        var machineClass = gridNode.getNodeOwner().getClass();
        this.machines.remove(machineClass, gridNode);

        gridNode.setGridStorage(null);

        if (this.pivot == gridNode) {
            var nodesIt = machines.values().iterator();
            if (nodesIt.hasNext()) {
                this.pivot = (GridNode) nodesIt.next();
            } else {
                this.pivot = null;
                TickHandler.instance().removeNetwork(this);
                this.myStorage.remove();
            }
        }
    }

    void add(final GridNode gridNode) {

        // handle loading grid storages.
        if (gridNode.getGridStorage() != null) {
            final GridStorage gs = gridNode.getGridStorage();
            final IGrid grid = gs.getGrid();

            if (grid == null) {
                this.myStorage = gs;
                this.myStorage.setGrid(this);

                for (var gc : this.caches.values()) {
                    gc.onJoin(this.myStorage);
                }
            } else if (grid != this) {
                if (this.myStorage == null) {
                    this.myStorage = WorldData.instance().storageData().getNewGridStorage();
                    this.myStorage.setGrid(this);
                }

                final IGridStorage tmp = new GridStorage();
                if (!gs.hasDivided(this.myStorage)) {
                    gs.addDivided(this.myStorage);

                    for (var gc : ((Grid) grid).caches.values()) {
                        gc.onSplit(tmp);
                    }

                    for (var gc : this.caches.values()) {
                        gc.onJoin(tmp);
                    }
                }
            }
        } else if (this.myStorage == null) {
            this.myStorage = WorldData.instance().storageData().getNewGridStorage();
            this.myStorage.setGrid(this);
        }

        // update grid node...
        gridNode.setGridStorage(this.myStorage);

        // track node.
        this.machines.put(gridNode.getNodeOwner().getClass(), gridNode);

        for (var cache : this.caches.values()) {
            cache.addNode(gridNode);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends IGridCache> C getCache(final Class<C> iface) {
        var cache = this.caches.get(iface);
        if (cache == null) {
            throw new IllegalArgumentException("Cache " + iface + " is not registered");
        }
        return (C) cache;
    }

    @Override
    public <T extends GridEvent> T postEvent(final T ev) {
        GridEventBus.postEvent(this, ev);
        return ev;
    }

    public Iterable<Class<?>> getMachineClasses() {
        return this.machines.keySet();
    }

    @Override
    public Iterable<IGridNode> getMachineNodes(Class<?> machineClass) {
        return this.machines.get(machineClass);
    }

    @Override
    public <T> Set<T> getMachines(Class<T> machineClass) {
        Set<IGridNode> nodes = this.machines.get(machineClass);
        var resultBuilder = ImmutableSet.<T>builder();
        for (IGridNode node : nodes) {
            var logicalHost = node.getNodeOwner();
            if (machineClass.isInstance(logicalHost)) {
                resultBuilder.add(machineClass.cast(logicalHost));
            }
        }
        return resultBuilder.build();
    }

    @Override
    public <T> Set<T> getActiveMachines(Class<T> machineClass) {
        Set<IGridNode> nodes = this.machines.get(machineClass);
        var resultBuilder = ImmutableSet.<T>builder();
        for (IGridNode node : nodes) {
            var logicalHost = node.getNodeOwner();
            if (machineClass.isInstance(logicalHost) && node.isActive()) {
                resultBuilder.add(machineClass.cast(logicalHost));
            }
        }
        return resultBuilder.build();
    }

    @Override
    public Iterable<IGridNode> getNodes() {
        return this.machines.values();
    }

    @Override
    public boolean isEmpty() {
        return this.pivot == null;
    }

    @Override
    public IGridNode getPivot() {
        return this.pivot;
    }

    void setPivot(final GridNode pivot) {
        this.pivot = pivot;
    }

    public void update() {
        for (var gc : this.caches.values()) {
            // are there any nodes left?
            if (this.pivot != null) {
                gc.onUpdateTick();
            }
        }
    }

    void saveState() {
        for (var c : this.caches.values()) {
            c.populateGridStorage(this.myStorage);
        }
    }

    public void setImportantFlag(final int i, final boolean publicHasPower) {
        final int flag = 1 << i;
        this.priority = this.priority & ~flag | (publicHasPower ? flag : 0);
    }
}
