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

package appeng.me.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import appeng.api.networking.IGridCacheProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.GridBootingStatusChange;
import appeng.api.networking.events.GridChannelRequirementChanged;
import appeng.api.networking.events.GridControllerChange;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.stats.IAdvancementTrigger;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.pathfinding.AdHocChannelUpdater;
import appeng.me.pathfinding.ControllerChannelUpdater;
import appeng.me.pathfinding.ControllerValidator;
import appeng.me.pathfinding.IPathItem;
import appeng.me.pathfinding.PathSegment;
import appeng.tile.networking.ControllerTileEntity;

public class PathGridCache implements IPathingGrid, IGridCacheProvider {

    static {
        Api.instance().grid().addGridCacheEventHandler(GridChannelRequirementChanged.class, IPathingGrid.class, (cache, event) -> {
            ((PathGridCache) cache).updateNodReq(event);
        });
    }

    private final List<PathSegment> active = new ArrayList<>();
    private final Set<ControllerTileEntity> controllers = new HashSet<>();
    private final Set<IGridNode> requireChannels = new HashSet<>();
    private final Set<IGridNode> blockDense = new HashSet<>();
    private final IGrid myGrid;
    private int channelsInUse = 0;
    private int channelsByBlocks = 0;
    private double channelPowerUsage = 0.0;
    private boolean recalculateControllerNextTick = true;
    private boolean updateNetwork = true;
    private boolean booting = false;
    private ControllerState controllerState = ControllerState.NO_CONTROLLER;
    private int ticksUntilReady = 20;
    private int lastChannels = 0;
    private HashSet<IPathItem> semiOpen = new HashSet<>();

    public PathGridCache(final IGrid g) {
        this.myGrid = g;
    }

    @Override
    public void onUpdateTick() {
        if (this.recalculateControllerNextTick) {
            this.recalcController();
        }

        if (this.updateNetwork) {
            if (!this.booting) {
                this.myGrid.postEvent(new GridBootingStatusChange());
            }

            this.booting = true;
            this.updateNetwork = false;
            this.setChannelsInUse(0);

            if (this.controllerState == ControllerState.NO_CONTROLLER) {
                final int requiredChannels = this.calculateRequiredChannels();
                int used = requiredChannels;
                if (requiredChannels > 8) {
                    used = 0;
                }

                final int nodes = this.myGrid.size();
                this.setChannelsInUse(used);

                this.ticksUntilReady = 20 + Math.max(0, nodes / 100 - 20);
                this.setChannelsByBlocks(nodes * used);
                this.setChannelPowerUsage(this.getChannelsByBlocks() / 128.0);

                this.myGrid.getPivot().beginVisit(new AdHocChannelUpdater(used));
            } else if (this.controllerState == ControllerState.CONTROLLER_CONFLICT) {
                this.ticksUntilReady = 20;
                this.myGrid.getPivot().beginVisit(new AdHocChannelUpdater(0));
            } else {
                var nodes = this.myGrid.size();
                this.ticksUntilReady = 20 + Math.max(0, nodes / 100 - 20);
                final HashSet<IPathItem> closedList = new HashSet<>();
                this.semiOpen = new HashSet<>();

                for (final IGridNode node : this.myGrid.getMachineNodes(ControllerTileEntity.class)) {
                    closedList.add((IPathItem) node);
                    for (final IGridConnection gcc : node.getConnections()) {
                        var gc = (GridConnection) gcc;
                        if (!(gc.getOtherSide(node).getNodeOwner() instanceof ControllerTileEntity)) {
                            final List<IPathItem> open = new ArrayList<>();
                            closedList.add(gc);
                            open.add(gc);
                            gc.setControllerRoute((GridNode) node, true);
                            this.active.add(new PathSegment(this, open, this.semiOpen, closedList));
                        }
                    }
                }
            }
        }

        if (!this.active.isEmpty() || this.ticksUntilReady > 0) {
            final Iterator<PathSegment> i = this.active.iterator();
            while (i.hasNext()) {
                final PathSegment pat = i.next();
                if (pat.step()) {
                    pat.setDead(true);
                    i.remove();
                }
            }

            this.ticksUntilReady--;

            if (this.active.isEmpty() && this.ticksUntilReady <= 0) {
                if (this.controllerState == ControllerState.CONTROLLER_ONLINE) {
                    final Iterator<ControllerTileEntity> controllerIterator = this.controllers.iterator();
                    if (controllerIterator.hasNext()) {
                        final ControllerTileEntity controller = controllerIterator.next();
                        controller.getGridNode().beginVisit(new ControllerChannelUpdater());
                    }
                }

                // check for achievements
                this.achievementPost();

                this.booting = false;
                this.setChannelPowerUsage(this.getChannelsByBlocks() / 128.0);
                this.myGrid.postEvent(new GridBootingStatusChange());
            }
        }
    }

    @Override
    public void removeNode(final IGridNode gridNode) {
        if (gridNode.getNodeOwner() instanceof ControllerTileEntity controller) {
            this.controllers.remove(controller);
            this.recalculateControllerNextTick = true;
        }

        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.requireChannels.remove(gridNode);
        }

        if (gridNode.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED)) {
            this.blockDense.remove(gridNode);
        }

        this.repath();
    }

    @Override
    public void addNode(final IGridNode gridNode) {
        if (gridNode.getNodeOwner() instanceof ControllerTileEntity controller) {
            this.controllers.add(controller);
            this.recalculateControllerNextTick = true;
        }

        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.requireChannels.add(gridNode);
        }

        if (gridNode.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED)) {
            this.blockDense.add(gridNode);
        }

        this.repath();
    }

    private void recalcController() {
        this.recalculateControllerNextTick = false;
        final ControllerState old = this.controllerState;

        if (this.controllers.isEmpty()) {
            this.controllerState = ControllerState.NO_CONTROLLER;
        } else {
            var startingController = this.controllers.iterator().next();
            final IGridNode startingNode = startingController.getGridNode();
            if (startingNode == null) {
                this.controllerState = ControllerState.CONTROLLER_CONFLICT;
                return;
            }

            final ControllerValidator cv = new ControllerValidator(startingController.getPos());

            startingNode.beginVisit(cv);

            if (cv.isValid() && cv.getFound() == this.controllers.size()) {
                this.controllerState = ControllerState.CONTROLLER_ONLINE;
            } else {
                this.controllerState = ControllerState.CONTROLLER_CONFLICT;
            }
        }

        if (old != this.controllerState) {
            this.myGrid.postEvent(new GridControllerChange());
        }
    }

    private int calculateRequiredChannels() {
        this.semiOpen.clear();

        int depth = 0;
        for (final IGridNode node : this.requireChannels) {
            if (!this.semiOpen.contains(node)) {
                if (node.hasFlag(GridFlags.COMPRESSED_CHANNEL) && !this.blockDense.isEmpty()) {
                    return 9;
                }

                depth++;

                if (node.hasFlag(GridFlags.MULTIBLOCK)) {
                    var multiblock = node.getService(IGridMultiblock.class);
                    if (multiblock != null) {
                        var it = multiblock.getMultiblockNodes();
                        while (it.hasNext()) {
                            this.semiOpen.add((IPathItem) it.next());
                        }
                    }
                }
            }
        }

        return depth;
    }

    private void achievementPost() {
        if (this.lastChannels != this.getChannelsInUse()) {
            final IAdvancementTrigger currentBracket = this.getAchievementBracket(this.getChannelsInUse());
            final IAdvancementTrigger lastBracket = this.getAchievementBracket(this.lastChannels);
            if (currentBracket != lastBracket && currentBracket != null) {
                for (final IGridNode n : this.requireChannels) {
                    PlayerEntity player = Api.instance().registries().players().findPlayer(n.getOwningPlayerId());
                    if (player instanceof ServerPlayerEntity) {
                        currentBracket.trigger((ServerPlayerEntity) player);
                    }
                }
            }
        }
        this.lastChannels = this.getChannelsInUse();
    }

    private IAdvancementTrigger getAchievementBracket(final int ch) {
        if (ch < 8) {
            return null;
        }

        if (ch < 128) {
            return AppEng.instance().getAdvancementTriggers().getNetworkApprentice();
        }

        if (ch < 2048) {
            return AppEng.instance().getAdvancementTriggers().getNetworkEngineer();
        }

        return AppEng.instance().getAdvancementTriggers().getNetworkAdmin();
    }

    private void updateNodReq(final GridChannelRequirementChanged ev) {
        final IGridNode gridNode = ev.node;

        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.requireChannels.add(gridNode);
        } else {
            this.requireChannels.remove(gridNode);
        }

        this.repath();
    }

    @Override
    public boolean isNetworkBooting() {
        return !this.booting && !this.active.isEmpty();
    }

    @Override
    public ControllerState getControllerState() {
        return this.controllerState;
    }

    @Override
    public void repath() {
        // clean up...
        this.active.clear();

        this.setChannelsByBlocks(0);
        this.updateNetwork = true;
    }

    double getChannelPowerUsage() {
        return this.channelPowerUsage;
    }

    private void setChannelPowerUsage(final double channelPowerUsage) {
        this.channelPowerUsage = channelPowerUsage;
    }

    public int getChannelsByBlocks() {
        return this.channelsByBlocks;
    }

    public void setChannelsByBlocks(final int channelsByBlocks) {
        this.channelsByBlocks = channelsByBlocks;
    }

    public int getChannelsInUse() {
        return this.channelsInUse;
    }

    public void setChannelsInUse(final int channelsInUse) {
        this.channelsInUse = channelsInUse;
    }
}
