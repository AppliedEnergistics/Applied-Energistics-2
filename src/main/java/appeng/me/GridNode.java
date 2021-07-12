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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MutableClassToInstanceMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IConfigurableGridNode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IGridVisitor;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.GridPowerIdleChange;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.util.AEColor;
import appeng.core.worlddata.WorldData;
import appeng.me.pathfinding.IPathItem;

public class GridNode implements IConfigurableGridNode, IPathItem {
    private final ServerWorld world;
    /**
     * This is the logical host of the node, which could be any object. In many cases this will be a tile-entity or
     * part.
     */
    @Nonnull
    private final Object nodeOwner;
    @Nonnull
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
    @Nonnull
    private ItemStack visualRepresentation = ItemStack.EMPTY;
    @Nonnull
    private AEColor gridColor = AEColor.TRANSPARENT;
    private long lastSecurityKey = -1;
    private int owningPlayerId = -1;
    private GridStorage myStorage = null;
    private Grid myGrid;
    private Object visitorIterationNumber = null;
    // connection criteria
    private int usedChannels = 0;
    private int lastUsedChannels = 0;
    private final EnumSet<GridFlags> flags;
    protected final EnumSet<Direction> exposedOnSides = EnumSet.noneOf(Direction.class);
    private ClassToInstanceMap<IGridNodeService> services;

    public <T> GridNode(@Nonnull ServerWorld world,
            @Nonnull T nodeOwner,
            @Nonnull IGridNodeListener<T> listener,
            Set<GridFlags> flags) {
        this.world = world;
        this.nodeOwner = nodeOwner;
        this.listener = listener;
        this.flags = EnumSet.copyOf(flags);
        this.services = null;
    }

    Grid getMyGrid() {
        return this.myGrid;
    }

    public int usedChannels() {
        return this.lastUsedChannels;
    }

    @FunctionalInterface
    public interface ListenerCallback<T> {
        void call(IGridNodeListener<T> listener, T owner, IGridNode node);
    }

    // The unchecked cast is safe because the constructor is correctly typed and ties logicalHost and listener together
    @SuppressWarnings("unchecked")
    public <T> void callListener(ListenerCallback<T> callback) {
        var typedOwner = (T) this.nodeOwner;
        var typedListener = (IGridNodeListener<T>) listener;
        callback.call(typedListener, typedOwner, this);
    }

    /**
     * Notifies the grid node's listener about a potential change in the grid node's status.
     */
    public void notifyStatusChange(IGridNodeListener.ActiveChangeReason reason) {
        callListener((listener, owner, node) -> listener.onActiveChanged(owner, node, reason));
    }

    void addConnection(final IGridConnection gridConnection) {
        connections.add((GridConnection) gridConnection);
        if (gridConnection.isInWorld()) {
            callListener(IGridNodeListener::onInWorldConnectionChanged);
        }

        connections.sort(new ConnectionComparator(this));
    }

    void removeConnection(final IGridConnection gridConnection) {
        connections.remove((GridConnection) gridConnection);
        if (gridConnection.isInWorld()) {
            callListener(IGridNodeListener::onInWorldConnectionChanged);
        }
    }

    boolean hasConnection(final IGridNode otherSide) {
        for (final IGridConnection gc : this.connections) {
            if (gc.a() == otherSide || gc.b() == otherSide) {
                return true;
            }
        }
        return false;
    }

    void validateGrid() {
        final GridSplitDetector gsd = new GridSplitDetector(this.getInternalGrid().getPivot());
        this.beginVisit(gsd);
        if (!gsd.isPivotFound()) {
            final IGridVisitor gp = new GridPropagator(Grid.create(this));
            this.beginVisit(gp);
        }
    }

    public Grid getInternalGrid() {
        if (this.myGrid == null) {
            this.myGrid = Grid.create(this);
        }

        return this.myGrid;
    }

    @Override
    public void beginVisit(final IGridVisitor g) {
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

                for (final GridNode n : thisRun) {
                    n.visitorConnection(tracker, g, nextRun, nextConn);
                }
            }
        } else {
            while (!nextRun.isEmpty()) {
                final Iterable<GridNode> thisRun = nextRun;
                nextRun = new ArrayDeque<>();

                for (final GridNode n : thisRun) {
                    n.visitorNode(tracker, g, nextRun);
                }
            }
        }
    }

    @Override
    public void updateState() {
        if (ready) {
            this.findInWorldConnections();
            this.getInternalGrid();
        }
    }

    public void setExposedOnSides(Set<Direction> directions) {
        exposedOnSides.clear();
        exposedOnSides.addAll(directions);
        updateState();
    }

    @Override
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
    @Override
    public void setIdlePowerUsage(@Nonnegative double usagePerTick) {
        this.idlePowerUsage = usagePerTick;
        if (myGrid != null && ready) {
            myGrid.postEvent(new GridPowerIdleChange(this));
        }
    }

    /**
     * Sets an itemstack that will only be used to represent this grid node in user interfaces. Can be set to
     * {@link ItemStack#EMPTY} to hide the node from UIs.
     */
    @Override
    public void setVisualRepresentation(@Nonnull ItemStack visualRepresentation) {
        this.visualRepresentation = Objects.requireNonNull(visualRepresentation);
    }

    /**
     * Colors can be used to prevent adjacent grid nodes from connecting. {@link AEColor#TRANSPARENT} indicates that the
     * node will connect to nodes of any color.
     */
    @Override
    public void setGridColor(@Nonnull AEColor color) {
        this.gridColor = Objects.requireNonNull(color);
        this.updateState();
    }

    @Override
    public IGrid getGrid() {
        return this.myGrid;
    }

    void setGrid(final Grid grid) {
        if (this.myGrid == grid) {
            return;
        }

        if (this.myGrid != null) {
            this.myGrid.remove(this);

            if (this.myGrid.isEmpty()) {
                this.myGrid.saveState();

                for (var c : grid.getProviders()) {
                    c.onJoin(this.myGrid.getMyStorage());
                }
            }
        }

        this.myGrid = grid;
        this.myGrid.add(this);
        callListener(IGridNodeListener::onGridChanged);
    }

    @Override
    public void destroy() {
        while (!this.connections.isEmpty()) {
            // not part of this network for real anymore.
            if (this.connections.size() == 1) {
                this.setGridStorage(null);
            }

            final IGridConnection c = this.connections.listIterator().next();
            final GridNode otherSide = (GridNode) c.getOtherSide(this);
            otherSide.getInternalGrid().setPivot(otherSide);
            c.destroy();
        }

        if (this.myGrid != null) {
            this.myGrid.remove(this);
        }
    }

    @Override
    public void markReady() {
        Preconditions.checkState(!ready);
        ready = true;
        updateState();
    }

    @Override
    public boolean isReady() {
        return ready && myGrid != null;
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

    @Nonnull
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

    @Override
    public boolean hasGridBooted() {
        if (myGrid == null) {
            return false;
        }
        return !myGrid.getService(IPathingGrid.class).isNetworkBooting();
    }

    @Override
    public boolean isPowered() {
        if (myGrid == null) {
            return false;
        }
        return myGrid.getService(IEnergyGrid.class).isNetworkPowered();
    }

    @Override
    public void loadFromNBT(final String name, final CompoundNBT nodeData) {
        Preconditions.checkState(!ready, "Cannot load NBT when the node was marked as ready.");
        if (this.myGrid == null) {
            final CompoundNBT node = nodeData.getCompound(name);
            this.owningPlayerId = node.getInt("p");
            this.setLastSecurityKey(node.getLong("k"));

            final long storageID = node.getLong("g");
            final GridStorage gridStorage = WorldData.instance().storageData().getGridStorage(storageID);
            this.setGridStorage(gridStorage);
        } else {
            throw new IllegalStateException("Loading data after part of a grid, this is invalid.");
        }
    }

    @Override
    public void saveToNBT(final String name, final CompoundNBT nodeData) {
        if (this.myStorage != null) {
            final CompoundNBT node = new CompoundNBT();

            node.putInt("p", this.owningPlayerId);
            node.putLong("k", this.getLastSecurityKey());
            node.putLong("g", this.myStorage.getID());

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
    public boolean hasFlag(final GridFlags flag) {
        return flags.contains(flag);
    }

    @Override
    public double getIdlePowerUsage() {
        return idlePowerUsage;
    }

    @NotNull
    @Override
    public ItemStack getVisualRepresentation() {
        return visualRepresentation;
    }

    @NotNull
    @Override
    public AEColor getGridColor() {
        return gridColor;
    }

    @Override
    public boolean isExposedOnSide(@NotNull Direction side) {
        return exposedOnSides.contains(side);
    }

    @Override
    public int getOwningPlayerId() {
        return this.owningPlayerId;
    }

    private int getUsedChannels() {
        return this.usedChannels;
    }

    protected void findInWorldConnections() {
    }

    private void visitorConnection(final Object tracker, final IGridVisitor g, final Deque<GridNode> nextRun,
            final Deque<IGridConnection> nextConnections) {
        if (g.visitNode(this)) {
            for (final IGridConnection gc : this.getConnections()) {
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

    private void visitorNode(final Object tracker, final IGridVisitor g, final Deque<GridNode> nextRun) {
        if (g.visitNode(this)) {
            for (final IGridConnection gc : this.getConnections()) {
                final GridNode gn = (GridNode) gc.getOtherSide(this);

                if (tracker == gn.visitorIterationNumber) {
                    continue;
                }

                gn.visitorIterationNumber = tracker;

                nextRun.add(gn);
            }
        }
    }

    GridStorage getGridStorage() {
        return this.myStorage;
    }

    void setGridStorage(final GridStorage s) {
        this.myStorage = s;
        this.usedChannels = 0;
        this.lastUsedChannels = 0;
    }

    @Override
    public IPathItem getControllerRoute() {
        if (this.connections.isEmpty() || this.hasFlag(GridFlags.CANNOT_CARRY)) {
            return null;
        }

        return this.connections.get(0);
    }

    @Override
    public void setControllerRoute(final IPathItem fast, final boolean zeroOut) {
        if (zeroOut) {
            this.usedChannels = 0;
        }

        GridConnection connection = (GridConnection) fast;

        final int idx = this.connections.indexOf(connection);
        if (idx > 0) {
            this.connections.remove(connection);
            this.connections.add(0, connection);
        }
    }

    @Override
    public boolean canSupportMoreChannels() {
        return this.getUsedChannels() < this.getMaxChannels();
    }

    private int getMaxChannels() {
        if (flags.contains(GridFlags.CANNOT_CARRY)) {
            return 0;
        } else if (!flags.contains(GridFlags.DENSE_CAPACITY)) {
            return 8;
        } else {
            return 32;
        }
    }

    @Override
    public Iterable<IPathItem> getPossibleOptions() {
        return ImmutableList.copyOf(this.connections);
    }

    @Override
    public void incrementChannelCount(final int usedChannels) {
        this.usedChannels += usedChannels;
    }

    @Override
    public void finalizeChannels() {
        if (hasFlag(GridFlags.CANNOT_CARRY)) {
            return;
        }

        if (this.getLastUsedChannels() != this.getUsedChannels()) {
            this.lastUsedChannels = this.usedChannels;

            if (this.getInternalGrid() != null) {
                notifyStatusChange(IGridNodeListener.ActiveChangeReason.CHANNEL);
            }
        }
    }

    private int getLastUsedChannels() {
        return this.lastUsedChannels;
    }

    public long getLastSecurityKey() {
        return this.lastSecurityKey;
    }

    public void setLastSecurityKey(final long lastSecurityKey) {
        this.lastSecurityKey = lastSecurityKey;
    }

    public double getPreviousDraw() {
        return this.previousDraw;
    }

    public void setPreviousDraw(final double previousDraw) {
        this.previousDraw = previousDraw;
    }

    private static class ConnectionComparator implements Comparator<IGridConnection> {
        private final IGridNode gn;

        public ConnectionComparator(final IGridNode gn) {
            this.gn = gn;
        }

        @Override
        public int compare(final IGridConnection o1, final IGridConnection o2) {
            final boolean preferredA = o1.getOtherSide(this.gn).hasFlag(GridFlags.PREFERRED);
            final boolean preferredB = o2.getOtherSide(this.gn).hasFlag(GridFlags.PREFERRED);

            return preferredA == preferredB ? 0 : preferredA ? -1 : 1;
        }
    }

    @Nullable
    @Override
    public <T extends IGridNodeService> T getService(Class<T> serviceClass) {
        return services != null ? services.getInstance(serviceClass) : null;
    }

    @Override
    public <T extends IGridNodeService> void addService(Class<T> serviceClass, T service) {
        if (services == null) {
            services = MutableClassToInstanceMap.create();
        }
        services.putInstance(serviceClass, service);
    }

    @NotNull
    @Override
    public Object getNodeOwner() {
        return nodeOwner;
    }

    @NotNull
    @Override
    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "node hosted by " + getNodeOwner().getClass().getName();
    }

}
