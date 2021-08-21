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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AELog;
import appeng.core.settings.TickRates;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;
import appeng.me.service.helpers.Connections;
import appeng.me.service.helpers.TunnelConnection;

public class MEP2PTunnelPart extends P2PTunnelPart<MEP2PTunnelPart> implements IGridTickable {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_me");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final Connections connection = new Connections(this);

    private final IManagedGridNode outerNode = AEApi.grid()
            .createManagedNode(this, NodeListener.INSTANCE)
            .setTagName("outer")
            .setFlags(GridFlags.DENSE_CAPACITY, GridFlags.CANNOT_CARRY_COMPRESSED);

    public MEP2PTunnelPart(final ItemStack is) {
        super(is);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.COMPRESSED_CHANNEL)
                .addService(IGridTickable.class, this);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    @Override
    public void readFromNBT(final CompoundTag extra) {
        super.readFromNBT(extra);
        this.outerNode.loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(final CompoundTag extra) {
        super.writeToNBT(extra);
        this.outerNode.saveToNBT(extra);
    }

    @Override
    public void onTunnelNetworkChange() {
        super.onTunnelNetworkChange();
        if (!this.isOutput()) {
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
    public void setPartHostInfo(final Direction side, final IPartHost host, final BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
        this.outerNode.setExposedOnSides(EnumSet.of(side));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerNode.getNode();
    }

    @Override
    public void onPlacement(final Player player, final InteractionHand hand, final ItemStack held,
            final Direction side) {
        super.onPlacement(player, hand, held, side);
        this.outerNode.setOwningPlayer(player);
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.METunnel.getMin(), TickRates.METunnel.getMax(), true, false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        // just move on...
        if (node.hasGridBooted()) {
            if (!node.isPowered() || !node.isActive()) {
                this.connection.markDestroy();
            } else {
                this.connection.markCreate();
            }
            TickHandler.instance().addCallable(this.getBlockEntity().getLevel(), this.connection);

            return TickRateModulation.SLEEP;
        }

        return TickRateModulation.IDLE;
    }

    public void updateConnections(final Connections connections) {
        if (connections.isDestroy()) {
            for (final TunnelConnection cw : this.connection.getConnections().values()) {
                cw.getConnection().destroy();
            }

            this.connection.getConnections().clear();
        } else if (connections.isCreate()) {

            var grid = this.getMainNode().getGrid();

            final Iterator<TunnelConnection> i = this.connection.getConnections().values().iterator();
            while (i.hasNext()) {
                final TunnelConnection cw = i.next();
                if (grid == null
                        || cw.getTunnel().getMainNode().getGrid() != grid
                        || !cw.getTunnel().getMainNode().isActive()) {
                    cw.getConnection().destroy();
                    i.remove();
                }
            }

            final List<MEP2PTunnelPart> newSides = new ArrayList<>();

            for (final MEP2PTunnelPart me : this.getOutputs()) {
                if (me.getMainNode().isActive() && connections.getConnections().get(me.getGridNode()) == null) {
                    newSides.add(me);
                }
            }

            for (final MEP2PTunnelPart me : newSides) {
                try {
                    connections.getConnections().put(me.getGridNode(), new TunnelConnection(me, AEApi
                            .grid().createGridConnection(this.outerNode.getNode(), me.outerNode.getNode())));
                } catch (final FailedConnectionException e) {
                    final BlockEntity start = this.getBlockEntity();
                    final BlockEntity end = me.getBlockEntity();

                    AELog.debug(e);

                    AELog.warn(
                            "Failed to establish a ME P2P Tunnel between the tunnels at [x=%d, y=%d, z=%d] and [x=%d, y=%d, z=%d]",
                            start.getBlockPos().getX(), start.getBlockPos().getY(), start.getBlockPos().getZ(),
                            end.getBlockPos().getX(), end.getBlockPos().getY(), end.getBlockPos().getZ());
                    // :(
                }
            }
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

}
