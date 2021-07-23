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

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.parts.ICablePart;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.core.Api;
import appeng.core.definitions.AEParts;
import appeng.items.parts.ColoredPartItem;
import appeng.parts.AEBasePart;

public class CablePart extends AEBasePart implements ICablePart {

    private static final IGridNodeListener<CablePart> NODE_LISTENER = new NodeListener<>() {
        @Override
        public void onInWorldConnectionChanged(CablePart nodeOwner, IGridNode node) {
            super.onInWorldConnectionChanged(nodeOwner, node);
            nodeOwner.markForUpdate();
        }
    };

    private final int[] channelsOnSide = { 0, 0, 0, 0, 0, 0 };

    private Set<Direction> connections = Collections.emptySet();
    private boolean powered = false;

    public CablePart(final ItemStack is) {
        super(is);
        this.getMainNode()
                .setFlags(GridFlags.PREFERRED)
                .setIdlePowerUsage(0.0)
                .setInWorldNode(true)
                .setExposedOnSides(EnumSet.allOf(Direction.class));
        if (is.getItem() instanceof ColoredPartItem<?>coloredPartItem) {
            this.getMainNode().setGridColor(coloredPartItem.getColor());
        }
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return Api.instance().grid().createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public BusSupport supportsBuses() {
        return BusSupport.CABLE;
    }

    @Override
    public AEColor getCableColor() {
        if (getItemStack().getItem() instanceof ColoredPartItem<?>coloredPartItem) {
            return coloredPartItem.getColor();
        }
        return AEColor.TRANSPARENT;
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
    public boolean changeColor(final AEColor newColor, final PlayerEntity who) {
        if (this.getCableColor() != newColor) {
            ItemStack newPart = null;

            if (this.getCableConnectionType() == AECableType.GLASS) {
                newPart = AEParts.GLASS_CABLE.stack(newColor, 1);
            } else if (this.getCableConnectionType() == AECableType.COVERED) {
                newPart = AEParts.COVERED_CABLE.stack(newColor, 1);
            } else if (this.getCableConnectionType() == AECableType.SMART) {
                newPart = AEParts.SMART_CABLE.stack(newColor, 1);
            } else if (this.getCableConnectionType() == AECableType.DENSE_COVERED) {
                newPart = AEParts.COVERED_DENSE_CABLE.stack(newColor, 1);
            } else if (this.getCableConnectionType() == AECableType.DENSE_SMART) {
                newPart = AEParts.SMART_DENSE_CABLE.stack(newColor, 1);
            }

            boolean hasPermission = true;

            var grid = getMainNode().getGrid();
            if (grid != null) {
                hasPermission = grid.getSecurityService().hasPermission(who, SecurityPermissions.BUILD);
            }

            if (newPart != null && hasPermission) {
                if (isRemote()) {
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
    public void setExposedOnSides(final EnumSet<Direction> sides) {
        this.getMainNode().setExposedOnSides(sides);
    }

    @Override
    public boolean isConnected(final Direction side) {
        return this.getConnections().contains(side);
    }

    public void markForUpdate() {
        this.getHost().markForUpdate();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        updateConnections();

        bch.addBox(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);

        final IPartHost ph = this.getHost();
        if (ph != null) {
            for (final AEPartLocation dir : AEPartLocation.SIDE_LOCATIONS) {
                var p = ph.getPart(dir);
                if (p != null) {
                    var dist = p.getCableConnectionLength(this.getCableConnectionType());

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

        for (final Direction of : this.getConnections()) {
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

    protected void updateConnections() {
        if (!isRemote()) {
            final IGridNode n = this.getGridNode();
            if (n != null) {
                this.setConnections(n.getConnectedSides());
            } else {
                this.setConnections(Collections.emptySet());
            }
        }
    }

    @Override
    public void writeToStream(final PacketBuffer data) throws IOException {
        int flags = 0;
        boolean[] writeSide = new boolean[Direction.values().length];
        int[] channelsPerSide = new int[Direction.values().length];

        for (Direction thisSide : Direction.values()) {
            final IPart part = this.getHost().getPart(thisSide);
            if (part != null) {
                writeSide[thisSide.ordinal()] = true;
                int channels = 0;
                if (part.getGridNode() != null) {
                    for (var gc : part.getGridNode().getConnections()) {
                        channels = Math.max(channels, gc.getUsedChannels());
                    }
                }
                channelsPerSide[thisSide.ordinal()] = channels;
            }
        }

        var n = getGridNode();
        if (n != null) {
            for (var entry : n.getInWorldConnections().entrySet()) {
                var side = entry.getKey().ordinal();
                writeSide[side] = true;
                channelsPerSide[side] = entry.getValue().getUsedChannels();
                flags |= 1 << side;
            }

            if (n.isPowered()) {
                flags |= 1 << AEPartLocation.INTERNAL.ordinal();
            }
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
    public boolean readFromStream(final PacketBuffer data) throws IOException {
        int cs = data.readByte();
        // Save previous state for change-detection
        var previousConnections = this.getConnections();
        var wasPowered = this.powered;

        boolean channelsChanged = false;

        this.powered = (cs & (1 << AEPartLocation.INTERNAL.ordinal())) != 0;

        var connections = EnumSet.noneOf(Direction.class);
        for (var d : Direction.values()) {
            boolean conOnSide = (cs & 1 << d.ordinal()) != 0;
            if (conOnSide) {
                connections.add(d);
            }

            int ch = 0;

            // Only read channels if there's a part on this side or a cable connection
            // This works only because cables are always read *last* from the packet update
            // for
            // a cable bus
            if (conOnSide || this.getHost().getPart(d) != null) {
                ch = data.readByte() & 0xFF;
            }

            if (ch != this.getChannelsOnSide(d.ordinal())) {
                channelsChanged = true;
                this.setChannelsOnSide(d.ordinal(), ch);
            }
        }
        this.setConnections(connections);

        return !previousConnections.equals(this.getConnections()) || wasPowered != this.powered || channelsChanged;
    }

    int getChannelsOnSide(final int i) {
        return this.channelsOnSide[i];
    }

    public int getChannelsOnSide(Direction side) {
        if (!this.powered) {
            return 0;
        }
        return this.channelsOnSide[side.ordinal()];
    }

    void setChannelsOnSide(final int i, final int channels) {
        this.channelsOnSide[i] = channels;
    }

    Set<Direction> getConnections() {
        return this.connections;
    }

    void setConnections(final Set<Direction> connections) {
        this.connections = connections;
    }

}
