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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.parts.ICablePart;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEParts;
import appeng.items.parts.ColoredPartItem;
import appeng.items.tools.powered.ColorApplicatorItem;
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

    public CablePart(ColoredPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setFlags(GridFlags.PREFERRED)
                .setIdlePowerUsage(0.0)
                .setInWorldNode(true)
                .setExposedOnSides(EnumSet.allOf(Direction.class));
        this.getMainNode().setGridColor(partItem.getColor());
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public BusSupport supportsBuses() {
        return BusSupport.CABLE;
    }

    @Override
    public AEColor getCableColor() {
        if (getPartItem() instanceof ColoredPartItem<?>coloredPartItem) {
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
    public void onPlacement(Player player) {
        super.onPlacement(player);

        // Apply the color of a held color applicator on placement
        var stack = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!stack.isEmpty() && stack.getItem() instanceof ColorApplicatorItem item) {
            var color = item.getActiveColor(stack);
            if (color != null && color != getCableColor() && item.consumeColor(stack, color, true)) {
                if (changeColor(color, player) && !player.getAbilities().instabuild) {
                    item.consumeColor(stack, color, false);
                }
            }
        }
    }

    @Override
    public boolean changeColor(AEColor newColor, Player who) {
        if (this.getCableColor() != newColor) {
            IPartItem<?> newPart = null;

            if (this.getCableConnectionType() == AECableType.GLASS) {
                newPart = AEParts.GLASS_CABLE.item(newColor);
            } else if (this.getCableConnectionType() == AECableType.COVERED) {
                newPart = AEParts.COVERED_CABLE.item(newColor);
            } else if (this.getCableConnectionType() == AECableType.SMART) {
                newPart = AEParts.SMART_CABLE.item(newColor);
            } else if (this.getCableConnectionType() == AECableType.DENSE_COVERED) {
                newPart = AEParts.COVERED_DENSE_CABLE.item(newColor);
            } else if (this.getCableConnectionType() == AECableType.DENSE_SMART) {
                newPart = AEParts.SMART_DENSE_CABLE.item(newColor);
            }

            boolean hasPermission = true;

            var grid = getMainNode().getGrid();
            if (grid != null) {
                hasPermission = grid.getSecurityService().hasPermission(who, SecurityPermissions.BUILD);
            }

            if (newPart != null && hasPermission) {
                if (isClientSide()) {
                    return true;
                }

                setPartItem(newPart);

                getMainNode().setGridColor(getCableColor());
                getHost().markForUpdate();
                getHost().markForSave();
                return true;
            }
        }
        return false;
    }

    @Override
    public void setExposedOnSides(EnumSet<Direction> sides) {
        this.getMainNode().setExposedOnSides(sides);
    }

    @Override
    public boolean isConnected(Direction side) {
        return this.getConnections().contains(side);
    }

    public void markForUpdate() {
        this.getHost().markForUpdate();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        updateConnections();

        bch.addBox(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);

        final IPartHost ph = this.getHost();
        if (ph != null) {
            for (Direction dir : Direction.values()) {
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

        for (Direction of : this.getConnections()) {
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
        if (!isClientSide()) {
            final IGridNode n = this.getGridNode();
            if (n != null) {
                this.setConnections(n.getConnectedSides());
            } else {
                this.setConnections(Collections.emptySet());
            }
        }
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
        int flags = 0;
        boolean[] writeSide = new boolean[Direction.values().length];
        byte[] channelsPerSide = new byte[Direction.values().length];

        for (Direction thisSide : Direction.values()) {
            var part = this.getHost().getPart(thisSide);
            if (part != null) {
                writeSide[thisSide.ordinal()] = true;
                int channels = 0;
                if (part.getGridNode() != null) {
                    for (var gc : part.getGridNode().getConnections()) {
                        channels = Math.max(channels, gc.getUsedChannels());
                    }
                }
                channelsPerSide[thisSide.ordinal()] = getVisualChannels(channels);
            }
        }

        var n = getGridNode();
        if (n != null) {
            for (var entry : n.getInWorldConnections().entrySet()) {
                var side = entry.getKey().ordinal();
                writeSide[side] = true;
                var connection = entry.getValue();
                channelsPerSide[side] = getVisualChannels(connection.getUsedChannels());
                flags |= 1 << side;
            }

            if (n.isPowered()) {
                flags |= 1 << Direction.values().length;
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

    private byte getVisualChannels(int channels) {
        var node = getGridNode();
        if (node == null) {
            return 0;
        }

        byte visualMaxChannels = switch (getCableConnectionType()) {
            case NONE -> 0;
            case GLASS, SMART, COVERED -> 8;
            case DENSE_COVERED, DENSE_SMART -> 32;
        };

        // In infinite mode, we either return 0 or full strength
        if (node.getGrid().getPathingService().getChannelMode() == ChannelMode.INFINITE) {
            return channels <= 0 ? 0 : visualMaxChannels;
        }

        int gridMaxChannels = node.getMaxChannels();
        if (visualMaxChannels == 0 || gridMaxChannels == 0) {
            return 0;
        }

        // Generally we round down here
        var result = (byte) (Math.min(visualMaxChannels, channels * visualMaxChannels / gridMaxChannels));
        // Except if at least 1 channel is used
        if (result == 0 && channels > 0) {
            return 1;
        } else {
            return result;
        }
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        int cs = data.readByte();
        // Save previous state for change-detection
        var previousConnections = this.getConnections();
        var wasPowered = this.powered;

        boolean channelsChanged = false;

        this.powered = (cs & (1 << Direction.values().length)) != 0;

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

    int getChannelsOnSide(int i) {
        return this.channelsOnSide[i];
    }

    public int getChannelsOnSide(Direction side) {
        if (!this.powered) {
            return 0;
        }
        return this.channelsOnSide[side.ordinal()];
    }

    void setChannelsOnSide(int i, int channels) {
        this.channelsOnSide[i] = channels;
    }

    Set<Direction> getConnections() {
        return this.connections;
    }

    void setConnections(Set<Direction> connections) {
        this.connections = connections;
    }

}
