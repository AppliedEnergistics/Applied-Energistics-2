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
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IReadOnlyCollection;
import appeng.core.AELog;
import appeng.core.worlddata.WorldData;
import appeng.hooks.ticking.TickHandler;
import appeng.me.pathfinding.IPathItem;
import appeng.util.IWorldCallable;
import appeng.util.ReadOnlyCollection;

public class GridNode implements IGridNode, IPathItem {
    private static final MENetworkChannelsChanged EVENT = new MENetworkChannelsChanged();
    private static final int[] CHANNEL_COUNT = { 0, 8, 32 };

    private final List<IGridConnection> connections = new ArrayList<>();
    private final IGridBlock gridProxy;
    // old power draw, used to diff
    private double previousDraw = 0.0;
    private long lastSecurityKey = -1;
    private int playerID = -1;
    private GridStorage myStorage = null;
    private Grid myGrid;
    private Object visitorIterationNumber = null;
    // connection criteria
    private int compressedData = 0;
    private int usedChannels = 0;
    private int lastUsedChannels = 0;

    public GridNode(final IGridBlock what) {
        this.gridProxy = what;
    }

    IGridBlock getGridProxy() {
        return this.gridProxy;
    }

    Grid getMyGrid() {
        return this.myGrid;
    }

    public int usedChannels() {
        return this.lastUsedChannels;
    }

    Class<? extends IGridHost> getMachineClass() {
        return this.getMachine().getClass();
    }

    void addConnection(final IGridConnection gridConnection) {
        this.connections.add(gridConnection);
        if (gridConnection.hasDirection()) {
            this.gridProxy.onGridNotification(GridNotification.CONNECTIONS_CHANGED);
        }

        final IGridNode gn = this;

        Collections.sort(this.connections, new ConnectionComparator(gn));
    }

    void removeConnection(final IGridConnection gridConnection) {
        this.connections.remove(gridConnection);
        if (gridConnection.hasDirection()) {
            this.gridProxy.onGridNotification(GridNotification.CONNECTIONS_CHANGED);
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
            final IGridVisitor gp = new GridPropagator(new Grid(this));
            this.beginVisit(gp);
        }
    }

    public Grid getInternalGrid() {
        if (this.myGrid == null) {
            this.myGrid = new Grid(this);
        }

        return this.myGrid;
    }

    @Override
    public void beginVisit(final IGridVisitor g) {
        final Object tracker = new Object();

        Deque<GridNode> nextRun = new ArrayDeque<>();
        nextRun.add(this);

        this.visitorIterationNumber = tracker;

        if (g instanceof IGridConnectionVisitor) {
            final Deque<IGridConnection> nextConn = new ArrayDeque<>();
            final IGridConnectionVisitor gcv = (IGridConnectionVisitor) g;

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
        final EnumSet<GridFlags> set = this.gridProxy.getFlags();

        this.compressedData = set.contains(GridFlags.CANNOT_CARRY) ? 0
                : (set.contains(GridFlags.DENSE_CAPACITY) ? 2 : 1);

        this.compressedData |= (this.gridProxy.getGridColor().ordinal() << 3);

        for (final Direction dir : this.gridProxy.getConnectableSides()) {
            this.compressedData |= (1 << (dir.ordinal() + 8));
        }

        this.findConnections();
        this.getInternalGrid();
    }

    @Override
    public IGridHost getMachine() {
        return this.gridProxy.getMachine();
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

                for (final IGridCache c : grid.getCaches().values()) {
                    c.onJoin(this.myGrid.getMyStorage());
                }
            }
        }

        this.myGrid = grid;
        this.myGrid.add(this);
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
    public IWorld getWorld() {
        return this.gridProxy.getLocation().getWorld();
    }

    @Override
    public EnumSet<AEPartLocation> getConnectedSides() {
        final EnumSet<AEPartLocation> set = EnumSet.noneOf(AEPartLocation.class);
        for (final IGridConnection gc : this.connections) {
            set.add(gc.getDirection(this));
        }
        return set;
    }

    @Override
    public IReadOnlyCollection<IGridConnection> getConnections() {
        return new ReadOnlyCollection<>(this.connections);
    }

    @Override
    public IGridBlock getGridBlock() {
        return this.gridProxy;
    }

    @Override
    public boolean isActive() {
        final IGrid g = this.getGrid();
        if (g != null) {
            final IPathingGrid pg = g.getCache(IPathingGrid.class);
            final IEnergyGrid eg = g.getCache(IEnergyGrid.class);
            return eg.isNetworkPowered() && !pg.isNetworkBooting() && this.meetsChannelRequirements();
        }
        return false;
    }

    @Override
    public void loadFromNBT(final String name, final CompoundNBT nodeData) {
        if (this.myGrid == null) {
            final CompoundNBT node = nodeData.getCompound(name);
            this.playerID = node.getInt("p");
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

            node.putInt("p", this.playerID);
            node.putLong("k", this.getLastSecurityKey());
            node.putLong("g", this.myStorage.getID());

            nodeData.put(name, node);
        } else {
            nodeData.remove(name);
        }
    }

    @Override
    public boolean meetsChannelRequirements() {
        return (!this.gridProxy.getFlags().contains(GridFlags.REQUIRE_CHANNEL) || this.getUsedChannels() > 0);
    }

    @Override
    public boolean hasFlag(final GridFlags flag) {
        return this.gridProxy.getFlags().contains(flag);
    }

    @Override
    public int getPlayerID() {
        return this.playerID;
    }

    @Override
    public void setPlayerID(final int playerID) {
        if (playerID >= 0 && this.playerID != playerID) {
            this.playerID = playerID;
            gridProxy.onGridNotification(GridNotification.OWNER_CHANGED);
        }
    }

    private int getUsedChannels() {
        return this.usedChannels;
    }

    private void findConnections() {
        if (!this.gridProxy.isWorldAccessible()) {
            return;
        }

        final EnumSet<AEPartLocation> newSecurityConnections = EnumSet.noneOf(AEPartLocation.class);

        final DimensionalCoord dc = this.gridProxy.getLocation();
        for (final AEPartLocation f : AEPartLocation.SIDE_LOCATIONS) {
            final IGridHost te = this.findGridHost(dc.getWorld(), dc.x + f.xOffset, dc.y + f.yOffset, dc.z + f.zOffset);
            if (te != null) {
                final GridNode node = (GridNode) te.getGridNode(f.getOpposite());
                if (node == null) {
                    continue;
                }

                final boolean isValidConnection = this.canConnect(node, f) && node.canConnect(this, f.getOpposite());

                IGridConnection con = null; // find the connection for this
                // direction..
                for (final IGridConnection c : this.getConnections()) {
                    if (c.getDirection(this) == f) {
                        con = c;
                        break;
                    }
                }

                if (con != null) {
                    final IGridNode os = con.getOtherSide(this);
                    if (os == node) {
                        // if this connection is no longer valid, destroy it.
                        if (!isValidConnection) {
                            con.destroy();
                        }
                    } else {
                        con.destroy();
                        // throw new GridException( "invalid state found, encountered connection to
                        // phantom block." );
                    }
                } else if (isValidConnection) {
                    if (node.getLastSecurityKey() != -1) {
                        newSecurityConnections.add(f);
                    } else {
                        // construct a new connection between these two nodes.
                        try {
                            GridConnection.create(node, this, f.getOpposite());
                        } catch (SecurityConnectionException e) {
                            AELog.debug(e);
                            TickHandler.instance().addCallable(node.getWorld(), new MachineSecurityBreak(this));

                            return;
                        } catch (final FailedConnectionException e) {
                            AELog.debug(e);

                            return;
                        }
                    }
                }
            }
        }

        for (final AEPartLocation f : newSecurityConnections) {
            final IGridHost te = this.findGridHost(dc.getWorld(), dc.x + f.xOffset, dc.y + f.yOffset, dc.z + f.zOffset);
            if (te != null) {
                final GridNode node = (GridNode) te.getGridNode(f.getOpposite());
                if (node == null) {
                    continue;
                }

                // construct a new connection between these two nodes.
                try {
                    GridConnection.create(node, this, f.getOpposite());
                } catch (SecurityConnectionException e) {
                    AELog.debug(e);

                    TickHandler.instance().addCallable(node.getWorld(), new MachineSecurityBreak(this));

                    return;
                } catch (final FailedConnectionException e) {
                    AELog.debug(e);

                    return;
                }
            }
        }
    }

    private IGridHost findGridHost(final IWorld world, final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        if (world.isBlockLoaded(pos)) {
            final TileEntity te = world.getTileEntity(pos);
            if (te instanceof IGridHost) {
                return (IGridHost) te;
            }
        }
        return null;
    }

    private boolean canConnect(final GridNode from, final AEPartLocation dir) {
        if (!this.isValidDirection(dir)) {
            return false;
        }

        if (!from.getColor().matches(this.getColor())) {
            return false;
        }

        return true;
    }

    private boolean isValidDirection(final AEPartLocation dir) {
        return (this.compressedData & (1 << (8 + dir.ordinal()))) > 0;
    }

    private AEColor getColor() {
        return AEColor.values()[(this.compressedData >> 3) & 0x1F];
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
        if (this.connections.isEmpty() || this.getFlags().contains(GridFlags.CANNOT_CARRY)) {
            return null;
        }

        return (IPathItem) this.connections.get(0);
    }

    @Override
    public void setControllerRoute(final IPathItem fast, final boolean zeroOut) {
        if (zeroOut) {
            this.usedChannels = 0;
        }

        final int idx = this.connections.indexOf(fast);
        if (idx > 0) {
            this.connections.remove(fast);
            this.connections.add(0, (IGridConnection) fast);
        }
    }

    @Override
    public boolean canSupportMoreChannels() {
        return this.getUsedChannels() < this.getMaxChannels();
    }

    private int getMaxChannels() {
        return CHANNEL_COUNT[this.compressedData & 0x03];
    }

    @Override
    public IReadOnlyCollection<IPathItem> getPossibleOptions() {
        return (IReadOnlyCollection) this.getConnections();
    }

    @Override
    public void incrementChannelCount(final int usedChannels) {
        this.usedChannels += usedChannels;
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return this.gridProxy.getFlags();
    }

    @Override
    public void finalizeChannels() {
        if (this.getFlags().contains(GridFlags.CANNOT_CARRY)) {
            return;
        }

        if (this.getLastUsedChannels() != this.getUsedChannels()) {
            this.lastUsedChannels = this.usedChannels;

            if (this.getInternalGrid() != null) {
                this.getInternalGrid().postEventTo(this, EVENT);
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

    private static class MachineSecurityBreak implements IWorldCallable<Void> {
        private final GridNode node;

        public MachineSecurityBreak(final GridNode node) {
            this.node = node;
        }

        @Override
        public Void call(final World world) throws Exception {
            this.node.getMachine().securityBreak();

            return null;
        }
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

            return preferredA == preferredB ? 0 : (preferredA ? -1 : 1);
        }
    }
}
