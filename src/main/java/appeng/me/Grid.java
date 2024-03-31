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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.Nullable;

import net.minecraft.CrashReportCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import appeng.api.networking.GridServicesInternal;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridEvent;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.ITickManager;
import appeng.core.AELog;
import appeng.hooks.ticking.TickHandler;
import appeng.me.service.P2PService;
import appeng.parts.AEBasePart;
import appeng.util.IDebugExportable;
import appeng.util.JsonStreamUtil;

public class Grid implements IGrid {
    /**
     * We use this to copy the list of grid nodes we'll notify. Avoids a potential ConcurrentModificationException.
     */
    private static final List<IGridNode> ITERATION_BUFFER = new ArrayList<>();
    private static int nextSerial = 0;

    private final SetMultimap<Class<?>, IGridNode> machines = MultimapBuilder.hashKeys().hashSetValues().build();
    private final Map<Class<?>, IGridServiceProvider> services;
    // Becomes null after the last node has left the grid.
    @Nullable
    private GridNode pivot;
    private int priority; // how import is this network?
    private final int serialNumber = nextSerial++; // useful to keep track of grids in toString() for debugging purposes

    /**
     * Creates a new grid, sends the necessary events, and registers it to the tickhandler or other objects.
     *
     * @param center the pivot point of the new grid
     */
    public static Grid create(GridNode center) {
        Grid grid = new Grid(center);

        TickHandler.instance().addNetwork(grid);
        center.setGrid(grid);

        AELog.grid("Created grid %s with center %s", grid, center);

        return grid;
    }

    private Grid(GridNode center) {
        this.pivot = Objects.requireNonNull(center);
        this.services = GridServicesInternal.createServices(this);
    }

    int getPriority() {
        return this.priority;
    }

    Collection<IGridServiceProvider> getProviders() {
        return this.services.values();
    }

    @Override
    public int size() {
        return this.machines.size();
    }

    void remove(GridNode gridNode) {
        for (var c : this.services.values()) {
            c.removeNode(gridNode);
        }

        var machineClass = gridNode.getOwner().getClass();
        this.machines.remove(machineClass, gridNode);

        if (this.pivot == gridNode) {
            var nodesIt = machines.values().iterator();
            if (nodesIt.hasNext()) {
                this.pivot = (GridNode) nodesIt.next();
            } else {
                this.pivot = null;
                TickHandler.instance().removeNetwork(this);

                AELog.grid("Removed grid %s", this);
            }
        }
    }

    void add(GridNode gridNode, @Nullable CompoundTag savedData) {
        // track node.
        this.machines.put(gridNode.getOwner().getClass(), gridNode);

        for (var service : this.services.values()) {
            service.addNode(gridNode, savedData);
        }
    }

    void saveNodeData(GridNode gridNode, CompoundTag savedData) {
        for (var service : this.services.values()) {
            service.saveNodeData(gridNode, savedData);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends IGridService> C getService(Class<C> iface) {
        var service = this.services.get(iface);
        if (service == null) {
            throw new IllegalArgumentException("Service " + iface + " is not registered");
        }
        return (C) service;
    }

    @Override
    public <T extends GridEvent> T postEvent(T ev) {
        GridEventBus.postEvent(this, ev);
        return ev;
    }

    @Override
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
            var logicalHost = node.getOwner();
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
            var logicalHost = node.getOwner();
            if (machineClass.isInstance(logicalHost) && node.isActive()) {
                resultBuilder.add(machineClass.cast(logicalHost));
            }
        }
        return resultBuilder.build();
    }

    @Override
    public Collection<IGridNode> getNodes() {
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

    void setPivot(GridNode pivot) {
        this.pivot = pivot;
    }

    public void onServerStartTick() {
        if (this.pivot == null) {
            return;
        }

        for (var gc : this.services.values()) {
            gc.onServerStartTick();
        }
    }

    public void onLevelStartTick(Level level) {
        if (this.pivot == null) {
            return;
        }

        for (var gc : this.services.values()) {
            gc.onLevelStartTick(level);
        }
    }

    public void onLevelEndTick(Level level) {
        if (this.pivot == null) {
            return;
        }

        for (var gc : this.services.values()) {
            gc.onLevelEndTick(level);
        }
    }

    public void onServerEndTick() {
        if (this.pivot == null) {
            return;
        }

        for (var gc : this.services.values()) {
            gc.onServerEndTick();
        }
    }

    public void setImportantFlag(int i, boolean publicHasPower) {
        final int flag = 1 << i;
        this.priority = this.priority & ~flag | (publicHasPower ? flag : 0);
    }

    public void notifyAllNodes(IGridNodeListener.State state) {
        if (!ITERATION_BUFFER.isEmpty()) {
            throw new IllegalStateException("Recursively trying to notify all nodes is not allowed");
        }

        try {
            // We're copying the nodes to a temporary buffer here because changing the power state of a node
            // may actually cause adjacent nodes to suddenly boot (i.e. QNBs) and modify the grid while
            // we're iterating over it.
            ITERATION_BUFFER.addAll(getNodes());

            for (IGridNode node : ITERATION_BUFFER) {
                ((GridNode) node).notifyStatusChange(state);
            }
        } finally {
            ITERATION_BUFFER.clear();
        }
    }

    public void fillCrashReportCategory(CrashReportCategory category) {
        category.setDetail("Nodes", this.machines.size());
        category.setDetail("Serial number", this.serialNumber);
        if (AELog.isGridLogEnabled()) {
            category.setDetail("All GridNodes",
                    this.machines.values().stream().map(Object::toString).collect(Collectors.joining(";")));
        }
        if (this.pivot != null) {
            this.pivot.fillCrashReportCategory(category);
        }
    }

    @Override
    public String toString() {
        return "Grid #" + serialNumber;
    }

    private static String getServiceExportKey(Class<?> service) {
        if (service == IEnergyService.class) {
            return "energyService";
        } else if (service == ISpatialService.class) {
            return "spatialService";
        } else if (service == IPathingService.class) {
            return "pathingService";
        } else if (service == IStorageService.class) {
            return "storageService";
        } else if (service == ITickManager.class) {
            return "tickManager";
        } else if (service == P2PService.class) {
            return "p2pService";
        } else if (service == ICraftingService.class) {
            return "craftingService";
        } else {
            return service.getName();
        }
    }

    @Override
    public void export(JsonWriter jsonWriter) throws IOException {
        jsonWriter.beginObject();

        var properties = Map.of(
                "id", serialNumber,
                "disposed", pivot == null);
        JsonStreamUtil.writeProperties(properties, jsonWriter);

        // Assign unique IDs to all owners
        var machineIdMap = new Reference2IntOpenHashMap<>(machines.size());
        for (var node : machines.values()) {
            machineIdMap.put(node.getOwner(), machineIdMap.size());
            // Also assign unique IDs to part hosts
            if (node.getOwner() instanceof AEBasePart part) {
                machineIdMap.put(part.getBlockEntity(), machineIdMap.size());
            }
        }

        // Assign unique IDs to all involved machines and nodes
        var nodeIdMap = new Reference2IntOpenHashMap<IGridNode>(machines.size());
        for (var node : machines.values()) {
            nodeIdMap.put(node, nodeIdMap.size());
        }

        jsonWriter.name("machines");
        exportMachines(jsonWriter, machineIdMap, nodeIdMap);

        jsonWriter.name("nodes");
        exportNodes(jsonWriter, machineIdMap, nodeIdMap);

        jsonWriter.name("services");
        jsonWriter.beginObject();
        for (var entry : services.entrySet()) {
            jsonWriter.name(getServiceExportKey(entry.getKey()));
            jsonWriter.beginObject();
            entry.getValue().debugDump(jsonWriter);
            jsonWriter.endObject();
        }
        jsonWriter.endObject();

        jsonWriter.endObject();
    }

    private void exportMachines(JsonWriter jsonWriter, Reference2IntMap<Object> machineIds,
            Reference2IntMap<IGridNode> nodeIds) throws IOException {
        jsonWriter.beginArray();
        for (var entry : machineIds.reference2IntEntrySet()) {
            jsonWriter.beginObject();
            JsonStreamUtil.writeProperties(Map.of(
                    "id", entry.getIntValue()), jsonWriter);
            if (entry.getKey() instanceof IDebugExportable exportable) {
                exportable.debugExport(jsonWriter, machineIds, nodeIds);
            }
            jsonWriter.endObject();
        }
        jsonWriter.endArray();
    }

    private void exportNodes(JsonWriter jsonWriter, Reference2IntMap<Object> machineIds,
            Reference2IntMap<IGridNode> nodeIds) throws IOException {
        // Dump nodes
        jsonWriter.beginArray();
        for (var entry : nodeIds.reference2IntEntrySet()) {
            var node = entry.getKey();
            ((GridNode) node).debugExport(jsonWriter, machineIds, nodeIds);
        }
        jsonWriter.endArray();
    }

    public int getSerialNumber() {
        return serialNumber;
    }
}
