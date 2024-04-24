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

package appeng.parts.p2p;

import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;

public class MEP2PTunnelPart extends P2PTunnelPart<MEP2PTunnelPart> implements IGridTickable {

    private static final P2PModels MODELS = new P2PModels(AppEng.makeId("part/p2p/p2p_tunnel_me"));

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    /**
     * Updates to ME tunnel connections are always delayed until the end of the tick. This field stores which operation
     * should be performed. Even if multiple updates are queued, only the most recently queued will be performed.
     */
    private ConnectionUpdate pendingUpdate = ConnectionUpdate.NONE;

    // This maps outputs to the grid connection between the external node of this tunnel and the external node
    // of the other tunnel. Generally only maintained for the input side. May still have content for output tunnels
    // before cleanup.
    private final Map<MEP2PTunnelPart, IGridConnection> connections = new IdentityHashMap<>();

    private final IManagedGridNode outerNode = GridHelper
            .createManagedNode(this, NodeListener.INSTANCE)
            .setTagName("outer")
            .setInWorldNode(true)
            .setFlags(GridFlags.DENSE_CAPACITY, GridFlags.CANNOT_CARRY_COMPRESSED);

    public MEP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.COMPRESSED_CHANNEL)
                .addService(IGridTickable.class, this);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    @Override
    public void readFromNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.readFromNBT(extra, registries);
        this.outerNode.loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(CompoundTag extra, HolderLookup.Provider registries) {
        super.writeToNBT(extra, registries);
        this.outerNode.saveToNBT(extra);
    }

    @Override
    public void onTunnelNetworkChange() {
        super.onTunnelNetworkChange();
        // Trigger the delayed update of the tunnel connections. Usually this is only done for input
        // tunnels, but if an input is converted to an output, we need to clean up the connections.
        if (!this.isOutput() || !connections.isEmpty()) {
            getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }
    }

    @Override
    public AECableType getExternalCableConnectionType() {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.outerNode.destroy();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.outerNode.create(getLevel(), getBlockEntity().getBlockPos());
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
        this.outerNode.setExposedOnSides(EnumSet.of(side));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerNode.getNode();
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        this.outerNode.setOwningPlayer(player);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.METunnel, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!node.isOnline()) {
            pendingUpdate = ConnectionUpdate.DISCONNECT;
        } else {
            pendingUpdate = ConnectionUpdate.CONNECT;
        }

        TickHandler.instance().addCallable(getLevel(), this::updateConnections);
        return TickRateModulation.SLEEP;
    }

    private void updateConnections() {
        var operation = pendingUpdate;
        pendingUpdate = ConnectionUpdate.NONE;

        var mainGrid = getMainNode().getGrid();

        if (isOutput()) {
            // It's possible to be woken up while we're still an input, then be made an output,
            // and THEN this method is called afterwards. Disconnect any potentially remaining
            // connections.
            operation = ConnectionUpdate.DISCONNECT;
        } else if (mainGrid == null) {
            // We got disconnected completely (are we being destroyed?)
            operation = ConnectionUpdate.DISCONNECT;
        }

        if (operation == ConnectionUpdate.DISCONNECT) {
            for (var cw : connections.values()) {
                cw.destroy();
            }
            connections.clear();
        } else if (operation == ConnectionUpdate.CONNECT) {
            var outputs = getOutputs();

            // Sever existing connections to tunnels that are no longer outputs of this input or
            // that have become invalid for other reasons.
            var it = connections.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                var output = entry.getKey();
                var connection = entry.getValue();

                // The previous tunnel could have moved to a different main grid (net-split), or lost his channel.
                // Or it may have been relinked to a different input.
                if (output.getMainNode().getGrid() != mainGrid
                        || !output.getMainNode().isOnline()
                        || !outputs.contains(output)) {
                    connection.destroy();
                    it.remove();
                }
            }

            for (var output : outputs) {
                // The other tunnel has no channel, or it's already connected
                if (!output.getMainNode().isOnline() || connections.containsKey(output)) {
                    continue;
                }

                var connection = GridHelper.createConnection(getExternalFacingNode(),
                        output.getExternalFacingNode());
                connections.put(output, connection);
            }
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private enum ConnectionUpdate {
        NONE,
        DISCONNECT,
        CONNECT
    }
}
