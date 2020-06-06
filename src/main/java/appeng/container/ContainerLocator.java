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

package appeng.container;


import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.parts.AEBasePart;
import appeng.parts.misc.PartInterface;
import com.google.common.base.Preconditions;
import io.netty.handler.codec.DecoderException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Describes how a container the player has opened was originally
 * located. This can be one of three ways:
 *
 * <ul>
 *     <li>A tile entity at a given block position.</li>
 *     <li>A part (i.e. cable bus part) at the side of a given block position.</li>
 *     <li>An item held by the player.</li>
 * </ul>
 */
public final class ContainerLocator {

    private enum Type {
        ITEM,
        BLOCK,
        PART
    }

    private final Type type;
    private final int itemIndex;
    private final int dimensionId;
    private final BlockPos blockPos;
    private final AEPartLocation side;

    private ContainerLocator(Type type, int itemIndex, int dimensionId, BlockPos blockPos, AEPartLocation side) {
        this.type = type;
        this.itemIndex = itemIndex;
        this.dimensionId = dimensionId;
        this.blockPos = blockPos;
        this.side = side;
    }

    public static ContainerLocator forTileEntity(TileEntity te) {
        if (te.getWorld() == null) {
            throw new IllegalArgumentException("Cannot open a tile entity that is not in a world");
        }
        int dimensionId = te.getWorld().getDimension().getType().getId();
        return new ContainerLocator(Type.BLOCK, -1, dimensionId, te.getPos(), null);
    }

    public static ContainerLocator forTileEntitySide(TileEntity te, Direction side) {
        if (te.getWorld() == null) {
            throw new IllegalArgumentException("Cannot open a tile entity that is not in a world");
        }
        int dimensionId = te.getWorld().getDimension().getType().getId();
        return new ContainerLocator(Type.PART, -1, dimensionId, te.getPos(), AEPartLocation.fromFacing(side));
    }

    public static ContainerLocator forHand(Hand hand) {
        // FIXME can we get an inventory location for the hand?
        throw new IllegalStateException();
    }

    public static ContainerLocator forPart(AEBasePart part) {
        IPartHost host = part.getHost();
        DimensionalCoord pos = host.getLocation();
        return new ContainerLocator(
                Type.PART,
                -1,
                pos.getWorld().getDimension().getType().getId(),
                pos.getBlockPos(),
                part.getSide()
        );
    }

    public boolean hasItemIndex() {
        return type == Type.ITEM;
    }

    public int getItemIndex() {
        Preconditions.checkState(type == Type.ITEM);
        return itemIndex;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public boolean hasBlockPos() {
        return type == Type.BLOCK || type == Type.PART;
    }

    public BlockPos getBlockPos() {
        Preconditions.checkState(type == Type.BLOCK || type == Type.PART);
        return blockPos;
    }

    public boolean hasSide() {
        return type == Type.PART;
    }

    public AEPartLocation getSide() {
        Preconditions.checkState(type == Type.PART);
        return side;
    }

    public void write(PacketBuffer buf) {
        switch (type) {
            case ITEM:
                buf.writeByte(0);
                buf.writeInt(itemIndex);
                break;
            case BLOCK:
                buf.writeByte(1);
                buf.writeInt(dimensionId);
                buf.writeBlockPos(blockPos);
                break;
            case PART:
                buf.writeByte(2);
                buf.writeInt(dimensionId);
                buf.writeBlockPos(blockPos);
                buf.writeByte(side.ordinal());
                break;
            default:
                throw new IllegalStateException("Unsupported ContainerLocator type: " + type);
        }
    }

    public static ContainerLocator read(PacketBuffer buf) {
        byte type = buf.readByte();
        switch (type) {
            case 0:
                return new ContainerLocator(
                        Type.ITEM,
                        buf.readInt(),
                        -1,
                        null,
                        null
                );
            case 1:
                return new ContainerLocator(
                        Type.BLOCK,
                        -1,
                        buf.readInt(),
                        buf.readBlockPos(),
                        null
                );
            case 2:
                return new ContainerLocator(
                        Type.PART,
                        -1,
                        buf.readInt(),
                        buf.readBlockPos(),
                        AEPartLocation.values()[buf.readByte()]
                );
            default:
                throw new DecoderException("ContainerLocator type out of range: " + type);
        }
    }

}
