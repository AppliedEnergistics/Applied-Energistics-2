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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.pathing.ChannelMode;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEParts;
import appeng.items.parts.ColoredPartItem;
import appeng.items.tools.powered.ColorApplicatorItem;
import appeng.parts.AEBasePart;
import appeng.parts.AEBasePart.NodeListener;

public abstract class CablePart extends AEBasePart implements ICablePart {

    private static final IGridNodeListener<CablePart> NODE_LISTENER = new NodeListener<>() {
        @Override
        public void onInWorldConnectionChanged(CablePart nodeOwner, IGridNode node) {
            super.onInWorldConnectionChanged(nodeOwner, node);
            nodeOwner.markForUpdate();
        }
    };

    private final int[] channelsOnSide = { 0, 0, 0, 0, 0, 0 };

    private Set<Direction> connections = Collections.emptySet();

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
    public final void getBoxes(IPartCollisionHelper bch) {
        getBoxes(bch, dir -> true);
    }

    /**
     * @param filterConnections Only add boxes for connections towards sides matching this. Use null to check for
     *                          center.
     */
    public abstract void getBoxes(IPartCollisionHelper bch, Predicate<@Nullable Direction> filterConnections);

    protected static void addConnectionBox(IPartCollisionHelper bch, Direction direction, double min, double max,
            double distanceFromEnd) {
        switch (direction) {
            case DOWN -> bch.addBox(min, distanceFromEnd, min, max, min, max);
            case EAST -> bch.addBox(max, min, min, 16.0 - distanceFromEnd, max, max);
            case NORTH -> bch.addBox(min, min, distanceFromEnd, max, max, min);
            case SOUTH -> bch.addBox(min, min, max, max, max, 16.0 - distanceFromEnd);
            case UP -> bch.addBox(min, max, min, max, 16.0 - distanceFromEnd, max);
            case WEST -> bch.addBox(distanceFromEnd, min, min, min, max, max);
        }
    }

    protected void addNonDenseBoxes(IPartCollisionHelper bch, Predicate<@Nullable Direction> filterConnections,
            double min, double max) {
        if (filterConnections.test(null)) {
            bch.addBox(min, min, min, max, max, max);
        }

        var ph = this.getHost();
        if (ph != null) {
            for (var dir : Direction.values()) {
                if (!filterConnections.test(dir)) {
                    continue;
                }

                var p = ph.getPart(dir);
                if (p != null) {
                    var dist = p.getCableConnectionLength(this.getCableConnectionType());

                    if (dist <= 0 || dist > 8) {
                        continue;
                    }

                    addConnectionBox(bch, dir, min, max, dist);
                }
            }
        }

        for (var of : this.getConnections()) {
            if (!filterConnections.test(of)) {
                continue;
            }

            addConnectionBox(bch, of, min, max, 0.0);
        }
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

            if (newPart != null) {
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

    protected void updateConnections() {
        if (!isClientSide()) {
            var n = this.getGridNode();
            if (n != null) {
                this.setConnections(n.getConnectedSides());
            } else {
                this.setConnections(Collections.emptySet());
            }
        }
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);

        boolean[] writeChannels = new boolean[Direction.values().length];
        byte[] channelsPerSide = new byte[Direction.values().length];

        for (var thisSide : Direction.values()) {
            var part = this.getHost().getPart(thisSide);
            if (part != null) {
                int channels = 0;
                if (part.getGridNode() != null) {
                    for (var gc : part.getGridNode().getConnections()) {
                        channels = Math.max(channels, gc.getUsedChannels());
                    }
                }
                channelsPerSide[thisSide.ordinal()] = getVisualChannels(channels);
                writeChannels[thisSide.ordinal()] = true;
            }
        }

        int connectedSidesPacked = 0;
        var n = getGridNode();
        if (n != null) {
            for (var entry : n.getInWorldConnections().entrySet()) {
                var side = entry.getKey().ordinal();
                var connection = entry.getValue();
                channelsPerSide[side] = getVisualChannels(connection.getUsedChannels());
                writeChannels[side] = true;
                connectedSidesPacked |= 1 << side;
            }
        }
        data.writeByte((byte) connectedSidesPacked);

        // Only write the used channels for sides where we have a part or another cable
        for (int i = 0; i < writeChannels.length; i++) {
            if (writeChannels[i]) {
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
        var changed = super.readFromStream(data);

        int connectedSidesPacked = data.readByte();
        // Save previous state for change-detection
        var previousConnections = this.getConnections();

        boolean channelsChanged = false;

        var connections = EnumSet.noneOf(Direction.class);
        for (var d : Direction.values()) {
            boolean conOnSide = (connectedSidesPacked & (1 << d.ordinal())) != 0;
            if (conOnSide) {
                connections.add(d);
            }

            int ch = 0;

            // Only read channels if there's a part on this side or a cable connection
            // This works only because cables are always read *last* from the packet update
            // for a cable bus
            if (conOnSide || this.getHost().getPart(d) != null) {
                ch = data.readByte() & 0xFF;
            }

            if (ch != this.channelsOnSide[d.ordinal()]) {
                channelsChanged = true;
                this.setChannelsOnSide(d.ordinal(), ch);
            }
        }
        this.setConnections(connections);

        return changed || !previousConnections.equals(this.getConnections()) || channelsChanged;
    }

    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);

        // Hacky hacky hacky, but it works. Refreshes the client-side state even if we're on the server
        if (!isClientSide()) {
            updateConnections();
            var packet = new FriendlyByteBuf(Unpooled.buffer());
            writeToStream(packet);
            readFromStream(packet);
        }

        for (var side : Direction.values()) {
            if (connections.contains(side)) {
                var sideName = "channels" + StringUtils.capitalize(side.getSerializedName());
                data.putInt(sideName, channelsOnSide[side.ordinal()]);
            }
        }

        var connectionsTag = new ListTag();
        for (var connection : connections) {
            connectionsTag.add(StringTag.valueOf(connection.getSerializedName()));
        }
        data.put("connections", connectionsTag);
    }

    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);

        // Restore channels per side for smart cables and also support as
        // a convenience to set them for all sides at once
        if (data.contains("channels")) {
            Arrays.fill(this.channelsOnSide, data.getInt("channels"));
        } else {
            for (var side : Direction.values()) {
                var sideName = "channels" + StringUtils.capitalize(side.getSerializedName());
                channelsOnSide[side.ordinal()] = data.getInt(sideName);
            }
        }

        // Restore adjacent connections
        var connections = EnumSet.noneOf(Direction.class);
        var connectionsTag = data.getList("connections", Tag.TAG_STRING);
        for (var connectionTag : connectionsTag) {
            var side = Direction.byName(connectionTag.getAsString());
            if (side != null) {
                connections.add(side);
            }
        }
        setConnections(connections);
    }

    public int getChannelsOnSide(Direction side) {
        if (!this.isPowered()) {
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
