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

import java.util.*;

import appeng.api.AEApi;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.*;
import appeng.api.networking.events.GridBootingStatusChange;
import appeng.api.networking.events.GridChannelRequirementChanged;
import appeng.api.networking.events.GridControllerChange;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingService;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.IAdvancementTrigger;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.pathfinding.*;

public class PathServiceService implements IPathingService, IGridServiceProvider {

    static {
        AEApi.grid().addGridServiceEventHandler(GridChannelRequirementChanged.class,
                IPathingService.class,
                (service, event) -> {
                    ((PathServiceService) service).updateNodReq(event);
                });
    }

    private final List<PathSegment> active = new ArrayList<>();
    private final Set<ControllerBlockEntity> controllers = new HashSet<>();
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

    public PathServiceService(final IGrid g) {
        this.myGrid = g;
    }

    @Override
    public void onServerEndTick() {
        if (this.recalculateControllerNextTick) {
            this.updateControllerState();
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

                for (final IGridNode node : this.myGrid.getMachineNodes(ControllerBlockEntity.class)) {
                    closedList.add((IPathItem) node);
                    for (final IGridConnection gcc : node.getConnections()) {
                        var gc = (GridConnection) gcc;
                        if (!(gc.getOtherSide(node).getOwner() instanceof ControllerBlockEntity)) {
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
                    final Iterator<ControllerBlockEntity> controllerIterator = this.controllers.iterator();
                    if (controllerIterator.hasNext()) {
                        final ControllerBlockEntity controller = controllerIterator.next();
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
        if (gridNode.getOwner() instanceof ControllerBlockEntity controller) {
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
        if (gridNode.getOwner() instanceof ControllerBlockEntity controller) {
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

    private void updateControllerState() {
        this.recalculateControllerNextTick = false;
        final ControllerState old = this.controllerState;

        this.controllerState = ControllerValidator.calculateState(controllers);

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
        var server = myGrid.getPivot().getLevel().getServer();

        if (this.lastChannels != this.getChannelsInUse()) {
            final IAdvancementTrigger currentBracket = this.getAchievementBracket(this.getChannelsInUse());
            final IAdvancementTrigger lastBracket = this.getAchievementBracket(this.lastChannels);
            if (currentBracket != lastBracket && currentBracket != null) {
                for (var n : this.requireChannels) {
                    var player = IPlayerRegistry.getConnected(server, n.getOwningPlayerId());
                    if (player != null) {
                        currentBracket.trigger(player);
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
            return AdvancementTriggers.NETWORK_APPRENTICE;
        }

        if (ch < 2048) {
            return AdvancementTriggers.NETWORK_ENGINEER;
        }

        return AdvancementTriggers.NETWORK_ADMIN;
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
        return this.booting && !this.active.isEmpty();
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
