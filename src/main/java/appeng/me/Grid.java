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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridNodeHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.util.IReadOnlyCollection;
import appeng.core.Api;
import appeng.core.worlddata.WorldData;
import appeng.hooks.ticking.TickHandler;
import appeng.util.ReadOnlyCollection;

public class Grid implements IGrid {
    private final NetworkEventBus eventBus = new NetworkEventBus();
    private final Map<Class<? extends IGridNodeHost>, MachineSet> machines = new HashMap<>();
    private final Map<Class<? extends IGridCache>, GridCacheWrapper> caches;
    private GridNode pivot;
    private int priority; // how import is this network?
    private GridStorage myStorage;

    /**
     * Creates a new grid, sends the necessary events, and registers it to the tickhandler or other objects.
     *
     * @param center the pivot point of the new grid
     * @return
     */
    public static Grid create(GridNode center) {
        Grid grid = new Grid(center);

        grid.postEvent(new MENetworkPostCacheConstruction());

        TickHandler.instance().addNetwork(grid);
        center.setGrid(grid);

        return grid;
    }

    private Grid(final GridNode center) {
        this.pivot = Objects.requireNonNull(center);

        final Map<Class<? extends IGridCache>, IGridCache> myCaches = Api.instance().registries().gridCache()
                .createCacheInstance(this);
        this.caches = new HashMap<>(myCaches.size());
        for (final Entry<Class<? extends IGridCache>, IGridCache> c : myCaches.entrySet()) {
            final Class<? extends IGridCache> key = c.getKey();
            final IGridCache value = c.getValue();
            final Class<? extends IGridCache> valueClass = value.getClass();

            this.eventBus.readClass(key, valueClass);
            this.caches.put(key, new GridCacheWrapper(value));
        }
    }

    int getPriority() {
        return this.priority;
    }

    IGridStorage getMyStorage() {
        return this.myStorage;
    }

    Map<Class<? extends IGridCache>, GridCacheWrapper> getCaches() {
        return this.caches;
    }

    public Iterable<Class<? extends IGridNodeHost>> getMachineClasses() {
        return this.machines.keySet();
    }

    int size() {
        int out = 0;
        for (final Collection<?> x : this.machines.values()) {
            out += x.size();
        }
        return out;
    }

    void remove(final GridNode gridNode) {
        for (final IGridCache c : this.caches.values()) {
            final IGridNodeHost machine = gridNode.getHost();
            c.removeNode(gridNode, machine);
        }

        final Class<? extends IGridNodeHost> machineClass = gridNode.getMachineClass();
        final Set<IGridNode> nodes = this.machines.get(machineClass);
        if (nodes != null) {
            nodes.remove(gridNode);
        }

        gridNode.setGridStorage(null);

        if (this.pivot == gridNode) {
            final Iterator<IGridNode> n = this.getNodes().iterator();
            if (n.hasNext()) {
                this.pivot = (GridNode) n.next();
            } else {
                this.pivot = null;
                TickHandler.instance().removeNetwork(this);
                this.myStorage.remove();
            }
        }
    }

    void add(final GridNode gridNode) {
        final Class<? extends IGridNodeHost> mClass = gridNode.getMachineClass();

        MachineSet nodes = this.machines.get(mClass);
        if (nodes == null) {
            nodes = new MachineSet(mClass);
            this.machines.put(mClass, nodes);
            this.eventBus.readClass(mClass, mClass);
        }

        // handle loading grid storages.
        if (gridNode.getGridStorage() != null) {
            final GridStorage gs = gridNode.getGridStorage();
            final IGrid grid = gs.getGrid();

            if (grid == null) {
                this.myStorage = gs;
                this.myStorage.setGrid(this);

                for (final IGridCache gc : this.caches.values()) {
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

                    for (final IGridCache gc : ((Grid) grid).caches.values()) {
                        gc.onSplit(tmp);
                    }

                    for (final IGridCache gc : this.caches.values()) {
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
        nodes.add(gridNode);

        for (final IGridCache cache : this.caches.values()) {
            final IGridNodeHost machine = gridNode.getHost();
            cache.addNode(gridNode, machine);
        }
    }

    @Override
    public <C extends IGridCache> C getCache(final Class<C> iface) {
        var wrapper = this.caches.get(iface);
        if (wrapper == null) {
            throw new IllegalArgumentException("Cache " + iface + " is not registered");
        }
        return iface.cast(wrapper.getCache());
    }

    @Override
    public MENetworkEvent postEvent(final MENetworkEvent ev) {
        return this.eventBus.postEvent(this, ev);
    }

    @Override
    public MENetworkEvent postEventTo(final IGridNode node, final MENetworkEvent ev) {
        return this.eventBus.postEventTo(this, (GridNode) node, ev);
    }

    @Override
    public IReadOnlyCollection<Class<? extends IGridNodeHost>> getMachinesClasses() {
        final Set<Class<? extends IGridNodeHost>> machineKeys = this.machines.keySet();

        return new ReadOnlyCollection<>(machineKeys);
    }

    @Override
    public IMachineSet getMachines(final Class<? extends IGridNodeHost> c) {
        final MachineSet s = this.machines.get(c);
        if (s == null) {
            return new MachineSet(c);
        }
        return s;
    }

    @Override
    public IReadOnlyCollection<IGridNode> getNodes() {
        return new GridNodeCollection(this.machines);
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
        for (final IGridCache gc : this.caches.values()) {
            // are there any nodes left?
            if (this.pivot != null) {
                gc.onUpdateTick();
            }
        }
    }

    void saveState() {
        for (final IGridCache c : this.caches.values()) {
            c.populateGridStorage(this.myStorage);
        }
    }

    public void setImportantFlag(final int i, final boolean publicHasPower) {
        final int flag = 1 << i;
        this.priority = this.priority & ~flag | (publicHasPower ? flag : 0);
    }
}
