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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IGridVisitor;
import appeng.api.networking.events.GridPowerIdleChange;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AEColor;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.AELog;
import appeng.me.pathfinding.IPathItem;
import appeng.util.IDebugExportable;
import appeng.util.JsonStreamUtil;

public class GridNode implements IGridNode, IPathItem, IDebugExportable {
    private static final Logger LOG = LoggerFactory.getLogger(GridNode.class);

    private final ServerLevel level;
    /**
     * This is the logical host of the node, which could be any object. In many cases this will be a block entity or
     * part.
     */

    private final Object owner;

    protected final IGridNodeListener<?> listener;

    /**
     * Indicates whether this node will be available for connections or will attempt to make connections.
     */
    private boolean ready;
    protected final List<GridConnection> connections = new ArrayList<>();
    // old power draw, used to diff
    private double previousDraw = 0.0;
    // idle power usage per tick in AE
    private double idlePowerUsage = 1.0;
    @Nullable
    private AEItemKey visualRepresentation = null;

    private AEColor gridColor = AEColor.TRANSPARENT;
    private int owningPlayerId = -1;
    private Grid myGrid;
    private Object visitorIterationNumber = null;
    /**
     * Will be modified during pathing and should not be exposed outside of that purpose.
     */
    int usedChannels = 0;
    /**
     * Finalized version of {@link #usedChannels} once pathing is done.
     */
    private int lastUsedChannels = 0;
    /**
     * The nearest ancestor of this node which restricts the number of maximum available channels for its subtree. It is
     * {@code null} if the next node is a controller.
     * <p>
     * Used to quickly walk the path to the controller when checking channel assignability, based on the observation
     * that the max channel count increases as we get to the controller, and that we only need to check the highest node
     * of each max channel count.
     * <p>
     * For example, on the following path:
     * {@code controller - dense cable 1 - dense cable 2 - dense cable 3 - cable 1 - cable 2 - cable 3 - device}, we
     * need to check that {@code dense cable 1} can accept the additional channel. If this is true then dense cables
     * {@code 2} and {@code 3} can always accept it. Same for regular cables, so it is enough to check that
     * {@code dense cable 1} and {@code cable 1} can accept it, massively speeding up the assignment for large trees.
     */
    @Nullable
    private GridNode highestSimilarAncestor = null;
    private int subtreeMaxChannels;
    private boolean subtreeAllowsCompressedChannels;

    private final EnumSet<GridFlags> flags;
    private ClassToInstanceMap<IGridNodeService> services;

    /**
     * Loaded from NBT and used when the node joins the grid.
     */
    @Nullable
    private CompoundTag savedData;

    public <T> GridNode(ServerLevel level,
            T owner,
            IGridNodeListener<T> listener,
            Set<GridFlags> flags) {
        this.level = level;
        this.owner = owner;
        this.listener = listener;
        this.flags = EnumSet.noneOf(GridFlags.class);
        this.flags.addAll(flags);
        this.services = null;
    }

    Grid getMyGrid() {
        return this.myGrid;
    }

    @FunctionalInterface
    public interface ListenerCallback<T> {
        void call(IGridNodeListener<T> listener, T owner, IGridNode node);
    }

    // The unchecked cast is safe because the constructor is correctly typed and ties logicalHost and listener together
    @SuppressWarnings("unchecked")
    public <T> void callListener(ListenerCallback<T> callback) {
        var typedOwner = (T) this.owner;
        var typedListener = (IGridNodeListener<T>) listener;
        callback.call(typedListener, typedOwner, this);
    }

    /**
     * Notifies the grid node's listener about a potential change in the grid node's status.
     */
    public void notifyStatusChange(IGridNodeListener.State reason) {
        callListener((listener, owner, node) -> listener.onStateChanged(owner, node, reason));
    }

    void addConnection(IGridConnection gridConnection) {
        connections.add((GridConnection) gridConnection);
        if (gridConnection.isInWorld()) {
            callListener(IGridNodeListener::onInWorldConnectionChanged);
        }
    }

    void removeConnection(IGridConnection gridConnection) {
        connections.remove((GridConnection) gridConnection);
        if (gridConnection.isInWorld()) {
            callListener(IGridNodeListener::onInWorldConnectionChanged);
        }
    }

    boolean hasConnection(IGridNode otherSide) {
        for (IGridConnection gc : this.connections) {
            if (gc.a() == otherSide || gc.b() == otherSide) {
                return true;
            }
        }
        return false;
    }

    void validateGrid() {
        if (!ready) {
            // We're in the process of being destroyed
            return;
        }

        var gsd = new GridSplitDetector(this.getInternalGrid().getPivot());
        this.beginVisit(gsd);
        if (!gsd.isPivotFound()) {
            var gp = new GridPropagator(Grid.create(this));
            this.beginVisit(gp);
        }
    }

    public Grid getInternalGrid() {
        if (this.myGrid == null) {
            Grid.create(this);
            // Note that the node can be moved immediately to a new grid when
            // it triggers adjacent connections due to block updates emitted while it joins the grid.
            // That means the return value of Grid.create is not necessarily the new grid,
            // but myGrid will already have been updated by the Grid calling setGrid on this node.
            return Objects.requireNonNull(this.myGrid);
        }

        return this.myGrid;
    }

    @Override
    public void beginVisit(IGridVisitor g) {
        final Object tracker = new Object();

        Deque<GridNode> nextRun = new ArrayDeque<>();
        nextRun.add(this);

        this.visitorIterationNumber = tracker;

        if (g instanceof IGridConnectionVisitor gcv) {
            final Deque<IGridConnection> nextConn = new ArrayDeque<>();

            while (!nextRun.isEmpty()) {
                while (!nextConn.isEmpty()) {
                    gcv.visitConnection(nextConn.poll());
                }

                final Iterable<GridNode> thisRun = nextRun;
                nextRun = new ArrayDeque<>();

                for (GridNode n : thisRun) {
                    n.visitorConnection(tracker, g, nextRun, nextConn);
                }
            }
        } else {
            while (!nextRun.isEmpty()) {
                var thisRun = nextRun;
                nextRun = new ArrayDeque<>();

                for (var n : thisRun) {
                    n.visitorNode(tracker, g, nextRun);
                }
            }
        }
    }

    protected final void updateState() {
        if (ready) {
            this.findInWorldConnections();
            this.getInternalGrid();
        }
    }

    /**
     * tell the node who was responsible for placing it, failure to do this may result in in-compatibility with the
     * security system. Called instead of loadFromNBT when initially placed, once set never required again, the value is
     * saved with the Node NBT.
     *
     * @param ownerPlayerId ME player id of the owner. See {@link IPlayerRegistry}.
     */
    public void setOwningPlayerId(int ownerPlayerId) {
        if (ownerPlayerId >= 0 && this.owningPlayerId != ownerPlayerId) {
            this.owningPlayerId = ownerPlayerId;
            if (ready) {
                callListener(IGridNodeListener::onOwnerChanged);
            }
        }
    }

    /**
     * @param usagePerTick The power in AE/t that will be drained by this node.
     */
    public void setIdlePowerUsage(double usagePerTick) {
        this.idlePowerUsage = usagePerTick;
        if (myGrid != null && ready) {
            myGrid.postEvent(new GridPowerIdleChange(this));
        }
    }

    /**
     * Sets an itemstack that will only be used to represent this grid node in user interfaces. Can be set to
     * <code>null</code> to hide the node from UIs.
     */
    public void setVisualRepresentation(@Nullable AEItemKey visualRepresentation) {
        this.visualRepresentation = visualRepresentation;
    }

    /**
     * Colors can be used to prevent adjacent grid nodes from connecting. {@link AEColor#TRANSPARENT} indicates that the
     * node will connect to nodes of any color.
     */
    public void setGridColor(AEColor color) {
        this.gridColor = Objects.requireNonNull(color);
        this.updateState();
    }

    @Override
    public IGrid getGrid() {
        if (this.myGrid == null) {
            throw new IllegalStateException("A node is being used after it has been destroyed.");
        }
        return this.myGrid;
    }

    void setGrid(Grid grid) {
        if (this.myGrid == grid) {
            return;
        }

        // Save any data from the old grid to move it over to the new grid
        if (this.myGrid != null) {
            this.savedData = new CompoundTag();
            this.myGrid.saveNodeData(this, savedData);
            this.myGrid.remove(this);
        }

        boolean wasPowered = isPowered();
        this.myGrid = grid;
        this.myGrid.add(this, savedData);

        callListener(IGridNodeListener::onGridChanged);
        if (wasPowered != isPowered()) {
            notifyStatusChange(IGridNodeListener.State.POWER);
        }
    }

    public void destroy() {
        // Allows connection destroy logic to know that this node is
        // no longer available.
        this.ready = false;

        boolean movedPivot = false;

        // First pass: Remove the connection on the other side
        for (var connection : connections) {
            var otherSide = (GridNode) connection.getOtherSide(this);

            // Moving the pivot closer means we potentially have to search fewer nodes
            // when searching for a grid split. Especially if the grid hasn't really been split.
            // In grids with a controller, side A of the connection will be closer to the controller
            // By moving the pivot to side A, the controller will NOT receive a new grid, which
            // is potentially beneficial by assuming the controller has a lot more connected
            // nodes that are not disrupted by this node being destroyed.
            if (!movedPivot && connection.a() != this && myGrid != null) {
                myGrid.setPivot((GridNode) connection.a());
                movedPivot = true;
            }

            // Ensure the other side holds no reference to this node anymore
            otherSide.removeConnection(connection);
        }

        // Second pass: Re-validate the grids of the previously connected, adjacent nodes
        for (var connection : connections) {
            var otherSide = (GridNode) connection.getOtherSide(this);

            // If we were unable to move the pivot away from ourselves in the first pass
            // just move it to the first eligible node, but only if we're the pivot
            if (!movedPivot && myGrid != null && myGrid.getPivot() == this) {
                myGrid.setPivot(otherSide);
                movedPivot = true;
            }

            // Re-validating the grid will cause the actual grid split to occur if the previously adjacent nodes
            // were only connected by this node.
            otherSide.validateGrid();

            // Cause a repath later. This is not done immediately.
            otherSide.getInternalGrid().getPathingService().repath();
        }

        connections.clear();

        AELog.grid("Destroyed node %s in grid %s", this, this.myGrid);
        if (this.myGrid != null) {
            this.myGrid.remove(this);
            this.myGrid = null;
        }
    }

    void markReady() {
        Preconditions.checkState(!ready);
        ready = true;
        updateState();
    }

    @Override
    public EnumSet<Direction> getConnectedSides() {
        var result = EnumSet.noneOf(Direction.class);
        for (IGridConnection connection : this.connections) {
            if (connection.isInWorld()) {
                result.add(connection.getDirection(this));
            }
        }
        return result;
    }

    @Override
    public Map<Direction, IGridConnection> getInWorldConnections() {
        var result = new EnumMap<Direction, IGridConnection>(Direction.class);
        for (IGridConnection connection : this.connections) {
            var direction = connection.getDirection(this);
            if (direction != null) {
                result.put(direction, connection);
            }
        }
        return result;
    }

    @Override
    public List<IGridConnection> getConnections() {
        return ImmutableList.copyOf(this.connections);
    }

    public boolean hasNoConnections() {
        return this.connections.isEmpty();
    }

    @Override
    public boolean hasGridBooted() {
        if (myGrid == null) {
            return false;
        }
        return !myGrid.getPathingService().isNetworkBooting();
    }

    @Override
    public boolean isPowered() {
        if (myGrid == null) {
            return false;
        }
        return myGrid.getEnergyService().isNetworkPowered();
    }

    public void loadFromNBT(String name, CompoundTag nodeDataContainer) {
        this.owningPlayerId = -1;

        var oldNodeData = this.savedData;
        if (nodeDataContainer.get(name) instanceof CompoundTag newNodeData) {
            this.savedData = newNodeData;
            if (newNodeData.contains("p", Tag.TAG_INT)) {
                this.owningPlayerId = newNodeData.getInt("p");
            }
        } else {
            this.savedData = null;
        }

        // When we're already part of the grid, we kinda need to leave and rejoin if the data changed...
        if (ready && this.myGrid != null && !areTagsEqualIgnoringPlayerId(this.savedData, oldNodeData)) {
            AELog.debug("Resetting grid node %s to reload NBT", this);
            this.destroy();
            markReady();
        }
    }

    private boolean areTagsEqualIgnoringPlayerId(CompoundTag newData, CompoundTag oldData) {
        Set<String> newKeys = newData != null ? newData.getAllKeys() : Set.of();
        Set<String> oldKeys = oldData != null ? oldData.getAllKeys() : Set.of();
        for (var newKey : newKeys) {
            if ("p".equals(newKey)) {
                continue; // Ignore player ID
            }
            var newTag = newData.get(newKey);
            var oldTag = oldData != null ? oldData.get(newKey) : null;
            if (!Objects.equals(newTag, oldTag)) {
                return false;
            }
        }
        // Check for missing keys
        for (var oldKey : oldKeys) {
            if (!"p".equals(oldKey) && !newKeys.contains(oldKey)) {
                return false;
            }
        }
        return true;
    }

    public void saveToNBT(String name, CompoundTag nodeData) {
        if (this.myGrid != null) {

            var node = new CompoundTag();
            node.putInt("p", this.owningPlayerId);
            this.myGrid.saveNodeData(this, node);

            nodeData.put(name, node);
        } else {
            nodeData.remove(name);
        }
    }

    @Override
    public boolean meetsChannelRequirements() {
        return !flags.contains(GridFlags.REQUIRE_CHANNEL) || this.getUsedChannels() > 0;
    }

    @Override
    public boolean hasFlag(GridFlags flag) {
        return flags.contains(flag);
    }

    @Override
    public double getIdlePowerUsage() {
        return idlePowerUsage;
    }

    @Nullable
    @Override
    public AEItemKey getVisualRepresentation() {
        return visualRepresentation;
    }

    @Override
    public AEColor getGridColor() {
        return gridColor;
    }

    @Override
    public int getOwningPlayerId() {
        return this.owningPlayerId;
    }

    @Override
    public UUID getOwningPlayerProfileId() {
        if (owningPlayerId == -1) {
            return null;
        }
        IPlayerRegistry mapping = IPlayerRegistry.getMapping(level);
        return mapping != null ? mapping.getProfileId(owningPlayerId) : null;
    }

    protected void findInWorldConnections() {
    }

    private void visitorConnection(Object tracker, IGridVisitor g, Deque<GridNode> nextRun,
            Deque<IGridConnection> nextConnections) {
        if (g.visitNode(this)) {
            for (IGridConnection gc : this.getConnections()) {
                final GridNode gn = (GridNode) gc.getOtherSide(this);
                final GridConnection gcc = (GridConnection) gc;

                if (gcc.getVisitorIterationNumber() != tracker) {
                    gcc.setVisitorIterationNumber(tracker);
                    nextConnections.add(gc);
                }

                if (tracker == gn.visitorIterationNumber) {
                    continue;
                }

                gn.visitorIterationNumber = tracker;

                nextRun.add(gn);
            }
        }
    }

    private void visitorNode(Object tracker, IGridVisitor g, Deque<GridNode> nextRun) {
        if (g.visitNode(this)) {
            for (var gc : this.getConnections()) {
                var gn = (GridNode) gc.getOtherSide(this);

                if (tracker == gn.visitorIterationNumber) {
                    continue;
                }

                gn.visitorIterationNumber = tracker;

                nextRun.add(gn);
            }
        }
    }

    @Override
    public void setAdHocChannels(int channels) {
        this.usedChannels = channels;
    }

    @Override
    public IPathItem getControllerRoute() {
        if (this.connections.isEmpty()) {
            throw new IllegalStateException(
                    "Node %s has no connections, cannot have a controller route!".formatted(this));
        }

        return this.connections.getFirst();
    }

    public @Nullable GridNode getHighestSimilarAncestor() {
        return highestSimilarAncestor;
    }

    public boolean getSubtreeAllowsCompressedChannels() {
        return subtreeAllowsCompressedChannels;
    }

    @Override
    public void setControllerRoute(IPathItem fast) {
        this.usedChannels = 0;

        var nodeParent = (GridNode) fast.getControllerRoute();
        if (nodeParent.getOwner() instanceof ControllerBlockEntity) {
            this.highestSimilarAncestor = null;
            this.subtreeMaxChannels = getMaxChannels();
            this.subtreeAllowsCompressedChannels = !hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED);
        } else {
            if (nodeParent.highestSimilarAncestor == null) {
                // Parent is connected to a controller, it is the bottleneck.
                this.highestSimilarAncestor = nodeParent;
            } else if (nodeParent.subtreeMaxChannels == nodeParent.highestSimilarAncestor.subtreeMaxChannels) {
                // Parent is not restricting the number of channels, go as high as possible.
                this.highestSimilarAncestor = nodeParent.highestSimilarAncestor;
            } else {
                // Parent is restricting the number of channels, link to it directly.
                this.highestSimilarAncestor = nodeParent;
            }
            this.subtreeMaxChannels = Math.min(nodeParent.subtreeMaxChannels, getMaxChannels());
            this.subtreeAllowsCompressedChannels = nodeParent.subtreeAllowsCompressedChannels
                    && !hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED);
        }

        GridConnection connection = (GridConnection) fast;

        final int idx = this.connections.indexOf(connection);
        if (idx > 0) {
            this.connections.remove(connection);
            this.connections.add(0, connection);
        }
    }

    @Override
    public int getUsedChannels() {
        return this.lastUsedChannels;
    }

    @Override
    public int getMaxChannels() {
        if (flags.contains(GridFlags.CANNOT_CARRY)) {
            return 0;
        }

        var channelMode = myGrid.getPathingService().getChannelMode();
        if (channelMode == ChannelMode.INFINITE) {
            return Integer.MAX_VALUE;
        }

        if (!flags.contains(GridFlags.DENSE_CAPACITY)) {
            return 8 * channelMode.getCableCapacityFactor();
        } else {
            return 32 * channelMode.getCableCapacityFactor();
        }
    }

    @Override
    public Iterable<IPathItem> getPossibleOptions() {
        return ImmutableList.copyOf(this.connections);
    }

    public int propagateChannelsUpwards(boolean consumesChannel) {
        this.usedChannels = 0;
        for (var connection : connections) {
            if (connection.getControllerRoute() == this) {
                this.usedChannels += connection.usedChannels;
            }
        }
        if (consumesChannel) {
            this.usedChannels++;
        }

        if (this.usedChannels > getMaxChannels()) {
            LOG.error(
                    "Internal channel assignment error. Grid node {} has {} channels passing through it but it only supports up to {}. Please open an issue on the AE2 repository.",
                    this, this.usedChannels, getMaxChannels());
        }

        return this.usedChannels;
    }

    public void incrementChannelCount(int usedChannels) {
        this.usedChannels += usedChannels;
    }

    @Override
    public void finalizeChannels() {
        this.highestSimilarAncestor = null;

        if (hasFlag(GridFlags.CANNOT_CARRY)) {
            return;
        }

        if (this.lastUsedChannels != this.usedChannels) {
            this.lastUsedChannels = this.usedChannels;

            if (this.getInternalGrid() != null) {
                notifyStatusChange(IGridNodeListener.State.CHANNEL);
            }
        }
    }

    public double getPreviousDraw() {
        return this.previousDraw;
    }

    public void setPreviousDraw(double previousDraw) {
        this.previousDraw = previousDraw;
    }

    @Nullable
    @Override
    public <T extends IGridNodeService> T getService(Class<T> serviceClass) {
        return services != null ? services.getInstance(serviceClass) : null;
    }

    public <T extends IGridNodeService> void addService(Class<T> serviceClass, T service) {
        if (services == null) {
            services = MutableClassToInstanceMap.create();
        }
        services.putInstance(serviceClass, service);
    }

    @Override
    public Object getOwner() {
        return owner;
    }

    @Override
    public ServerLevel getLevel() {
        return level;
    }

    @Override
    public String toString() {
        if (getOwner() instanceof BlockEntity blockEntity) {
            var beType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
            return "node@" + Integer.toHexString(hashCode()) + " hosted by " + beType;
        } else {
            return "node@" + Integer.toHexString(hashCode()) + " hosted by " + getOwner().getClass().getName();
        }
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory category) {
        category.setDetail("Node", toString());
        if (getOwner() instanceof IPart part) {
            part.addEntityCrashInfo(category);
        } else if (getOwner() instanceof BlockEntity blockEntity) {
            blockEntity.fillCrashReportCategory(category);
            Level level = blockEntity.getLevel();
            if (level != null) {
                category.setDetail("Level", level.dimension());
            }
        }
    }

    @Override
    public final void debugExport(JsonWriter writer, HolderLookup.Provider registries,
            Reference2IntMap<Object> machineIds,
            Reference2IntMap<IGridNode> nodeIds) throws IOException {
        writer.beginObject();
        exportProperties(writer, machineIds, nodeIds);
        writer.endObject();
    }

    protected void exportProperties(JsonWriter writer, Reference2IntMap<Object> machineIds,
            Reference2IntMap<IGridNode> nodeIds)
            throws IOException {
        var id = nodeIds.getInt(this);
        var machineId = machineIds.getInt(owner);
        JsonStreamUtil.writeProperties(Map.of(
                "id", id, "owner", machineId), writer);

        writer.name("level");
        writer.value(level.dimension().location().toString());
    }
}
