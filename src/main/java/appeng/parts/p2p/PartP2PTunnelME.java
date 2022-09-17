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


import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AELog;
import appeng.core.settings.TickRates;
import appeng.hooks.TickHandler;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.Connections;
import appeng.me.cache.helpers.TunnelConnection;
import appeng.me.helpers.AENetworkProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;


public class PartP2PTunnelME extends PartP2PTunnel<PartP2PTunnelME> implements IGridTickable {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_me");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final Connections connection = new Connections(this);
    private final AENetworkProxy outerProxy = new AENetworkProxy(this, "outer", ItemStack.EMPTY, true);

    public PartP2PTunnelME(final ItemStack is) {
        super(is);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.COMPRESSED_CHANNEL);
        this.outerProxy.setFlags(GridFlags.DENSE_CAPACITY, GridFlags.CANNOT_CARRY_COMPRESSED);
    }

    @Override
    public void readFromNBT(final NBTTagCompound extra) {
        super.readFromNBT(extra);
        this.outerProxy.readFromNBT(extra);
    }

    @Override
    public void writeToNBT(final NBTTagCompound extra) {
        super.writeToNBT(extra);
        this.outerProxy.writeToNBT(extra);
    }

    @Override
    public void onTunnelNetworkChange() {
        super.onTunnelNetworkChange();
        if (!this.isOutput()) {
            try {
                this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
            } catch (final GridAccessException e) {
                // :P
            }
        }
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.outerProxy.invalidate();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.outerProxy.onReady();
    }

    @Override
    public void setPartHostInfo(final AEPartLocation side, final IPartHost host, final TileEntity tile) {
        super.setPartHostInfo(side, host, tile);
        this.outerProxy.setValidSides(EnumSet.of(side.getFacing()));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerProxy.getNode();
    }

    @Override
    public void onPlacement(final EntityPlayer player, final EnumHand hand, final ItemStack held, final AEPartLocation side) {
        super.onPlacement(player, hand, held, side);
        this.outerProxy.setOwner(player);
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.METunnel.getMin(), TickRates.METunnel.getMax(), true, false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        // just move on...
        try {
            if (!this.getProxy().getPath().isNetworkBooting()) {
                if (!this.getProxy().getEnergy().isNetworkPowered()) {
                    this.connection.markDestroy();
                    TickHandler.INSTANCE.addCallable(this.getTile().getWorld(), this.connection);
                } else {
                    if (this.getProxy().isActive()) {
                        this.connection.markCreate();
                        TickHandler.INSTANCE.addCallable(this.getTile().getWorld(), this.connection);
                    } else {
                        this.connection.markDestroy();
                        TickHandler.INSTANCE.addCallable(this.getTile().getWorld(), this.connection);
                    }
                }

                return TickRateModulation.SLEEP;
            }
        } catch (final GridAccessException e) {
            // meh?
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

            final Iterator<TunnelConnection> i = this.connection.getConnections().values().iterator();
            while (i.hasNext()) {
                final TunnelConnection cw = i.next();
                try {
                    if (cw.getTunnel().getProxy().getGrid() != this.getProxy().getGrid()) {
                        cw.getConnection().destroy();
                        i.remove();
                    } else if (!cw.getTunnel().getProxy().isActive()) {
                        cw.getConnection().destroy();
                        i.remove();
                    }
                } catch (final GridAccessException e) {
                    // :P
                }
            }

            final List<PartP2PTunnelME> newSides = new ArrayList<>();
            try {
                for (final PartP2PTunnelME me : this.getOutputs()) {
                    if (me.getProxy().isActive() && connections.getConnections().get(me.getGridNode()) == null) {
                        newSides.add(me);
                    }
                }

                for (final PartP2PTunnelME me : newSides) {
                    try {
                        connections.getConnections()
                                .put(me.getGridNode(),
                                        new TunnelConnection(me, AEApi.instance()
                                                .grid()
                                                .createGridConnection(this.outerProxy.getNode(),
                                                        me.outerProxy.getNode())));
                    } catch (final FailedConnectionException e) {
                        final TileEntity start = this.getTile();
                        final TileEntity end = me.getTile();

                        AELog.debug(e);

                        AELog.warn("Failed to establish a ME P2P Tunnel between the tunnels at [x=%d, y=%d, z=%d] and [x=%d, y=%d, z=%d]",
                                start.getPos().getX(), start.getPos().getY(), start.getPos().getZ(), end.getPos().getX(), end.getPos().getY(),
                                end.getPos().getZ());
                        // :(
                    }
                }
            } catch (final GridAccessException e) {
                AELog.debug(e);
            }
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

}
