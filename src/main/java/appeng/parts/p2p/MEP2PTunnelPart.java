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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;

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
import appeng.api.util.AEPartLocation;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.settings.TickRates;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;
import appeng.me.service.helpers.Connections;
import appeng.me.service.helpers.TunnelConnection;
import appeng.parts.AEBasePart;

public class MEP2PTunnelPart extends P2PTunnelPart<MEP2PTunnelPart> implements IGridTickable {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_me");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final Connections connection = new Connections(this);

    private final IManagedGridNode outerNode = Api.instance().grid()
            .createManagedNode(this, AEBasePart.NodeListener.INSTANCE)
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
    public void readFromNBT(final CompoundNBT extra) {
        super.readFromNBT(extra);
        this.outerNode.loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(final CompoundNBT extra) {
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
        this.outerNode.create(getWorld(), getTile().getBlockPos());
    }

    @Override
    public void setPartHostInfo(final AEPartLocation side, final IPartHost host, final TileEntity tile) {
        super.setPartHostInfo(side, host, tile);
        this.outerNode.setExposedOnSides(EnumSet.of(side.getDirection()));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerNode.getNode();
    }

    @Override
    public void onPlacement(final PlayerEntity player, final Hand hand, final ItemStack held,
            final AEPartLocation side) {
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
            TickHandler.instance().addCallable(this.getTile().getLevel(), this.connection);

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
                    connections.getConnections().put(me.getGridNode(), new TunnelConnection(me, Api.instance()
                            .grid().createGridConnection(this.outerNode.getNode(), me.outerNode.getNode())));
                } catch (final FailedConnectionException e) {
                    final TileEntity start = this.getTile();
                    final TileEntity end = me.getTile();

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
