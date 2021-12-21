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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.GridBootingStatusChange;
import appeng.api.networking.events.GridChannelRequirementChanged;
import appeng.api.networking.events.GridControllerChange;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingService;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.IAdvancementTrigger;
import appeng.me.Grid;
import appeng.me.pathfinding.AdHocChannelUpdater;
import appeng.me.pathfinding.ControllerChannelUpdater;
import appeng.me.pathfinding.ControllerValidator;
import appeng.me.pathfinding.PathingCalculation;

public class PathingService implements IPathingService, IGridServiceProvider {
    private static final String TAG_CHANNEL_MODE = "channelMode";

    static {
        GridHelper.addGridServiceEventHandler(GridChannelRequirementChanged.class,
                IPathingService.class,
                (service, event) -> {
                    ((PathingService) service).updateNodReq(event);
                });
    }

    private PathingCalculation ongoingCalculation = null;
    private final Set<ControllerBlockEntity> controllers = new HashSet<>();
    private final Set<IGridNode> nodesNeedingChannels = new HashSet<>();
    private final Set<IGridNode> cannotCarryCompressedNodes = new HashSet<>();
    private final Grid grid;
    private int channelsInUse = 0;
    private int channelsByBlocks = 0;
    private double channelPowerUsage = 0.0;
    private boolean recalculateControllerNextTick = true;
    // Flag to indicate a reboot should occur next tick
    private boolean reboot = true;
    private boolean booting = false;
    @Nullable
    private AdHocNetworkError adHocNetworkError;
    private ControllerState controllerState = ControllerState.NO_CONTROLLER;
    private int ticksUntilReady = 0;
    private int lastChannels = 0;
    /**
     * This can be used for testing to set a specific channel mode on this grid that will not be overwritten by
     * repathing.
     */
    private boolean channelModeLocked;
    private ChannelMode channelMode = AEConfig.instance().getChannelMode();

    public PathingService(IGrid g) {
        this.grid = (Grid) g;
    }

    @Override
    public void onServerEndTick() {
        if (this.recalculateControllerNextTick) {
            this.updateControllerState();
        }

        if (this.reboot) {
            this.reboot = false;

            if (!this.booting) {
                this.booting = true;
                this.postBootingStatusChange();
            }

            this.channelsInUse = 0;
            this.adHocNetworkError = null;

            if (this.controllerState == ControllerState.NO_CONTROLLER) {
                // Returns 0 if there's an error
                this.channelsInUse = this.calculateAdHocChannels();

                var nodes = this.grid.size();
                this.ticksUntilReady = Math.max(5, nodes / 100);
                this.channelsByBlocks = nodes * this.channelsInUse;
                this.setChannelPowerUsage(this.channelsByBlocks / 128.0);

                this.grid.getPivot().beginVisit(new AdHocChannelUpdater(this.channelsInUse));
            } else if (this.controllerState == ControllerState.CONTROLLER_CONFLICT) {
                this.ticksUntilReady = 5;
                this.grid.getPivot().beginVisit(new AdHocChannelUpdater(0));
            } else {
                var nodes = this.grid.size();
                this.ticksUntilReady = Math.max(5, nodes / 100);
                this.ongoingCalculation = new PathingCalculation(grid);
            }
        }

        if (this.booting) {
            // Work on remaining pathfinding work
            if (ongoingCalculation != null) { // can be null for ad-hoc or invalid controller state
                for (var i = 0; i < AEConfig.instance().getPathfindingStepsPerTick(); i++) {
                    ongoingCalculation.step();
                    if (ongoingCalculation.isFinished()) {
                        this.channelsByBlocks = ongoingCalculation.getChannelsByBlocks();
                        this.channelsInUse = ongoingCalculation.getChannelsInUse();
                        ongoingCalculation = null;
                        break;
                    }
                }
            }

            this.ticksUntilReady--;

            // Booting completes when both pathfinding completes, and the minimum boot time has elapsed
            if (ongoingCalculation == null && ticksUntilReady <= 0) {
                if (this.controllerState == ControllerState.CONTROLLER_ONLINE) {
                    var controllerIterator = this.controllers.iterator();
                    if (controllerIterator.hasNext()) {
                        var controller = controllerIterator.next();
                        controller.getGridNode().beginVisit(new ControllerChannelUpdater());
                    }
                }

                // check for achievements
                this.achievementPost();

                this.booting = false;
                this.setChannelPowerUsage(this.channelsByBlocks / 128.0);
                this.postBootingStatusChange();
            } else if (ticksUntilReady == -2000) {
                AELog.warn("Booting has still not completed after 2000 ticks for %s", grid);
            }
        }
    }

    private void postBootingStatusChange() {
        this.grid.postEvent(new GridBootingStatusChange());
        this.grid.notifyAllNodes(IGridNodeListener.State.GRID_BOOT);
    }

    @Override
    public void removeNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof ControllerBlockEntity controller) {
            this.controllers.remove(controller);
            this.recalculateControllerNextTick = true;
        }

        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.nodesNeedingChannels.remove(gridNode);
        }

        if (gridNode.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED)) {
            this.cannotCarryCompressedNodes.remove(gridNode);
        }

        this.repath();
    }

    @Override
    public void addNode(IGridNode gridNode) {
        if (gridNode.getOwner() instanceof ControllerBlockEntity controller) {
            this.controllers.add(controller);
            this.recalculateControllerNextTick = true;
        }

        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.nodesNeedingChannels.add(gridNode);
        }

        if (gridNode.hasFlag(GridFlags.CANNOT_CARRY_COMPRESSED)) {
            this.cannotCarryCompressedNodes.add(gridNode);
        }

        this.repath();
    }

    private void updateControllerState() {
        this.recalculateControllerNextTick = false;
        final ControllerState old = this.controllerState;

        this.controllerState = ControllerValidator.calculateState(controllers);

        if (old != this.controllerState) {
            this.grid.postEvent(new GridControllerChange());
        }
    }

    @Nullable
    public AdHocNetworkError getAdHocNetworkError() {
        return adHocNetworkError;
    }

    private int calculateAdHocChannels() {
        var ignore = new HashSet<IGridNode>();

        this.adHocNetworkError = null;

        int channels = 0;
        for (var node : this.nodesNeedingChannels) {
            if (!ignore.contains(node)) {
                // Prevent ad-hoc networks from being connected to the outside and inside node of P2P tunnels at the
                // same time
                // this effectively prevents the nesting of P2P-tunnels in ad-hoc networks.
                if (node.hasFlag(GridFlags.COMPRESSED_CHANNEL) && !this.cannotCarryCompressedNodes.isEmpty()) {
                    this.adHocNetworkError = AdHocNetworkError.NESTED_P2P_TUNNEL;
                    return 0;
                }

                channels++;

                // Multiblocks only require a single channel. Add the remainder of the multi-block to the ignore-list,
                // to make this method skip them for channel calculation.
                if (node.hasFlag(GridFlags.MULTIBLOCK)) {
                    var multiblock = node.getService(IGridMultiblock.class);
                    if (multiblock != null) {
                        var it = multiblock.getMultiblockNodes();
                        while (it.hasNext()) {
                            ignore.add(it.next());
                        }
                    }
                }
            }
        }

        if (channels > channelMode.getAdHocNetworkChannels()) {
            this.adHocNetworkError = AdHocNetworkError.TOO_MANY_CHANNELS;
            return 0;
        }

        return channels;
    }

    private void achievementPost() {
        var server = grid.getPivot().getLevel().getServer();

        if (this.lastChannels != this.channelsInUse) {
            final IAdvancementTrigger currentBracket = this.getAchievementBracket(this.channelsInUse);
            final IAdvancementTrigger lastBracket = this.getAchievementBracket(this.lastChannels);
            if (currentBracket != lastBracket && currentBracket != null) {
                for (var n : this.nodesNeedingChannels) {
                    var player = IPlayerRegistry.getConnected(server, n.getOwningPlayerId());
                    if (player != null) {
                        currentBracket.trigger(player);
                    }
                }
            }
        }
        this.lastChannels = this.channelsInUse;
    }

    private IAdvancementTrigger getAchievementBracket(int ch) {
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

    private void updateNodReq(GridChannelRequirementChanged ev) {
        final IGridNode gridNode = ev.node;

        if (gridNode.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
            this.nodesNeedingChannels.add(gridNode);
        } else {
            this.nodesNeedingChannels.remove(gridNode);
        }

        this.repath();
    }

    @Override
    public boolean isNetworkBooting() {
        return this.booting;
    }

    @Override
    public ControllerState getControllerState() {
        return this.controllerState;
    }

    @Override
    public void repath() {
        if (!this.channelModeLocked) {
            this.channelMode = AEConfig.instance().getChannelMode();
        }

        // clean up...
        this.ongoingCalculation = null;

        this.channelsByBlocks = 0;
        this.reboot = true;
    }

    double getChannelPowerUsage() {
        return this.channelPowerUsage;
    }

    private void setChannelPowerUsage(double channelPowerUsage) {
        this.channelPowerUsage = channelPowerUsage;
    }

    public ChannelMode getChannelMode() {
        return channelMode;
    }

    public void setForcedChannelMode(@Nullable ChannelMode forcedChannelMode) {
        if (forcedChannelMode == null) {
            if (this.channelModeLocked) {
                this.channelModeLocked = false;
                repath();
            }
        } else {
            this.channelModeLocked = true;
            if (this.channelMode != forcedChannelMode) {
                this.channelMode = forcedChannelMode;
                this.repath();
            }
        }
    }

    @Override
    public int getUsedChannels() {
        return channelsInUse;
    }

    @Override
    public void onSplit(IGridStorage destinationStorage) {
        populateGridStorage(destinationStorage);
    }

    @Override
    public void onJoin(IGridStorage sourceStorage) {
        var tag = sourceStorage.dataObject();
        var channelModeName = tag.getString(TAG_CHANNEL_MODE);
        try {
            channelMode = ChannelMode.valueOf(channelModeName);
            channelModeLocked = true;
        } catch (IllegalArgumentException ignored) {
            channelModeLocked = false;
        }
    }

    @Override
    public void populateGridStorage(IGridStorage destinationStorage) {
        var tag = destinationStorage.dataObject();
        if (channelModeLocked) {
            tag.putString(TAG_CHANNEL_MODE, channelMode.name());
        } else {
            tag.remove(TAG_CHANNEL_MODE);
        }
    }
}
