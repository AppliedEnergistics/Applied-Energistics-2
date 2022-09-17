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

package appeng.parts.networking;


import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.definitions.IParts;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IReadOnlyCollection;
import appeng.items.parts.ItemPart;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.io.IOException;
import java.util.EnumSet;


public class PartCable extends AEBasePart implements IPartCable {

    private static final ImmutableSet<AEPartLocation> STRAIGHT_PART_LOCATIONS = ImmutableSet.of(AEPartLocation.DOWN, AEPartLocation.NORTH,
            AEPartLocation.EAST);

    private final int[] channelsOnSide = {0, 0, 0, 0, 0, 0};

    private EnumSet<AEPartLocation> connections = EnumSet.noneOf(AEPartLocation.class);
    private boolean powered = false;

    public PartCable(final ItemStack is) {
        super(is);
        this.getProxy().setFlags(GridFlags.PREFERRED);
        this.getProxy().setIdlePowerUsage(0.0);
        this.getProxy().setColor(AEColor.values()[((ItemPart) is.getItem()).variantOf(is.getItemDamage())]);
    }

    @Override
    public BusSupport supportsBuses() {
        return BusSupport.CABLE;
    }

    @Override
    public AEColor getCableColor() {
        return this.getProxy().getColor();
    }

    @Override
    public AECableType getCableConnectionType() {
        return AECableType.GLASS;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        if (cable == this.getCableConnectionType()) {
            return 4;
        } else if (cable.ordinal() >= this.getCableConnectionType().ordinal()) {
            return -1;
        } else {
            return 8;
        }
    }

    @Override
    public boolean changeColor(final AEColor newColor, final EntityPlayer who) {
        if (this.getCableColor() != newColor) {
            ItemStack newPart = null;

            final IParts parts = AEApi.instance().definitions().parts();

            if (this.getCableConnectionType() == AECableType.GLASS) {
                newPart = parts.cableGlass().stack(newColor, 1);
            } else if (this.getCableConnectionType() == AECableType.COVERED) {
                newPart = parts.cableCovered().stack(newColor, 1);
            } else if (this.getCableConnectionType() == AECableType.SMART) {
                newPart = parts.cableSmart().stack(newColor, 1);
            } else if (this.getCableConnectionType() == AECableType.DENSE_COVERED) {
                newPart = parts.cableDenseCovered().stack(newColor, 1);
            } else if (this.getCableConnectionType() == AECableType.DENSE_SMART) {
                newPart = parts.cableDenseSmart().stack(newColor, 1);
            }

            boolean hasPermission = true;

            try {
                hasPermission = this.getProxy().getSecurity().hasPermission(who, SecurityPermissions.BUILD);
            } catch (final GridAccessException e) {
                // :P
            }

            if (newPart != null && hasPermission) {
                if (Platform.isClient()) {
                    return true;
                }

                this.getHost().removePart(AEPartLocation.INTERNAL, true);
                this.getHost().addPart(newPart, AEPartLocation.INTERNAL, who, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public void setValidSides(final EnumSet<EnumFacing> sides) {
        this.getProxy().setValidSides(sides);
    }

    @Override
    public boolean isConnected(final EnumFacing side) {
        return this.getConnections().contains(AEPartLocation.fromFacing(side));
    }

    public void markForUpdate() {
        this.getHost().markForUpdate();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);

        if (Platform.isServer()) {
            final IGridNode n = this.getGridNode();
            if (n != null) {
                this.setConnections(n.getConnectedSides());
            } else {
                this.getConnections().clear();
            }
        }

        final IPartHost ph = this.getHost();
        if (ph != null) {
            for (final AEPartLocation dir : AEPartLocation.SIDE_LOCATIONS) {
                final IPart p = ph.getPart(dir);
                if (p instanceof IGridHost) {
                    final double dist = p.getCableConnectionLength(this.getCableConnectionType());

                    if (dist > 8) {
                        continue;
                    }

                    switch (dir) {
                        case DOWN:
                            bch.addBox(6.0, dist, 6.0, 10.0, 6.0, 10.0);
                            break;
                        case EAST:
                            bch.addBox(10.0, 6.0, 6.0, 16.0 - dist, 10.0, 10.0);
                            break;
                        case NORTH:
                            bch.addBox(6.0, 6.0, dist, 10.0, 10.0, 6.0);
                            break;
                        case SOUTH:
                            bch.addBox(6.0, 6.0, 10.0, 10.0, 10.0, 16.0 - dist);
                            break;
                        case UP:
                            bch.addBox(6.0, 10.0, 6.0, 10.0, 16.0 - dist, 10.0);
                            break;
                        case WEST:
                            bch.addBox(dist, 6.0, 6.0, 6.0, 10.0, 10.0);
                            break;
                        default:
                    }
                }
            }
        }

        for (final AEPartLocation of : this.getConnections()) {
            switch (of) {
                case DOWN:
                    bch.addBox(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
                    break;
                case EAST:
                    bch.addBox(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
                    break;
                case NORTH:
                    bch.addBox(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
                    break;
                case SOUTH:
                    bch.addBox(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
                    break;
                case UP:
                    bch.addBox(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);
                    break;
                case WEST:
                    bch.addBox(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
                    break;
                default:
            }
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);

        if (Platform.isServer()) {
            final IGridNode node = this.getGridNode();

            if (node != null) {
                int howMany = 0;
                for (final IGridConnection gc : node.getConnections()) {
                    howMany = Math.max(gc.getUsedChannels(), howMany);
                }

                data.setByte("usedChannels", (byte) howMany);
            }
        }
    }

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        int flags = 0;
        boolean[] writeSide = new boolean[EnumFacing.values().length];
        int[] channelsPerSide = new int[EnumFacing.values().length];

        for (EnumFacing thisSide : EnumFacing.values()) {
            final IPart part = this.getHost().getPart(thisSide);
            if (part != null) {
                writeSide[thisSide.ordinal()] = true;
                int channels = 0;
                if (part.getGridNode() != null) {
                    final IReadOnlyCollection<IGridConnection> set = part.getGridNode().getConnections();
                    for (final IGridConnection gc : set) {
                        channels = Math.max(channels, gc.getUsedChannels());
                    }
                }
                channelsPerSide[thisSide.ordinal()] = channels;
            }
        }

        IGridNode n = this.getGridNode();
        if (n != null) {
            for (final IGridConnection gc : n.getConnections()) {
                final AEPartLocation side = gc.getDirection(n);
                if (side != AEPartLocation.INTERNAL) {
                    writeSide[side.ordinal()] = true;
                    channelsPerSide[side.ordinal()] = gc.getUsedChannels();
                    flags |= (1 << side.ordinal());
                }
            }
        }

        try {
            if (this.getProxy().getEnergy().isNetworkPowered()) {
                flags |= (1 << AEPartLocation.INTERNAL.ordinal());
            }
        } catch (final GridAccessException e) {
            // aww...
        }

        data.writeByte((byte) flags);
        // Only write the used channels for sides where we have a part or another cable
        for (int i = 0; i < writeSide.length; i++) {
            if (writeSide[i]) {
                data.writeByte(channelsPerSide[i]);
            }
        }
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        int cs = data.readByte();
        final EnumSet<AEPartLocation> myC = this.getConnections().clone();
        final boolean wasPowered = this.powered;
        this.powered = false;
        boolean channelsChanged = false;

        for (final AEPartLocation d : AEPartLocation.values()) {
            if (d == AEPartLocation.INTERNAL) {
                final int id = 1 << d.ordinal();
                if (id == (cs & id)) {
                    this.powered = true;
                }
            } else {
                boolean conOnSide = (cs & (1 << d.ordinal())) != 0;
                if (conOnSide) {
                    this.getConnections().add(d);
                } else {
                    this.getConnections().remove(d);
                }

                int ch = 0;

                // Only read channels if there's a part on this side or a cable connection
                // This works only because cables are always read *last* from the packet update for
                // a cable bus
                if (conOnSide || this.getHost().getPart(d) != null) {
                    ch = (data.readByte()) & 0xFF;
                }

                if (ch != this.getChannelsOnSide(d.ordinal())) {
                    channelsChanged = true;
                    this.setChannelsOnSide(d.ordinal(), ch);
                }
            }
        }

        return !myC.equals(this.getConnections()) || wasPowered != this.powered || channelsChanged;
    }

    int getChannelsOnSide(final int i) {
        return this.channelsOnSide[i];
    }

    public int getChannelsOnSide(EnumFacing side) {
        if (!this.powered) {
            return 0;
        }
        return this.channelsOnSide[side.ordinal()];
    }

    void setChannelsOnSide(final int i, final int channels) {
        this.channelsOnSide[i] = channels;
    }

    EnumSet<AEPartLocation> getConnections() {
        return this.connections;
    }

    void setConnections(final EnumSet<AEPartLocation> connections) {
        this.connections = connections;
    }

}
