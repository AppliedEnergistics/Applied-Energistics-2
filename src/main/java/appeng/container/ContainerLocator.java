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

import com.google.common.base.Preconditions;

import io.netty.handler.codec.DecoderException;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.parts.AEBasePart;

/**
 * Describes how a container the player has opened was originally located. This can be one of three ways:
 *
 * <ul>
 * <li>A block entity at a given block position.</li>
 * <li>A part (i.e. cable bus part) at the side of a given block position.</li>
 * <li>An item held by the player.</li>
 * </ul>
 */
public final class ContainerLocator {

    private enum Type {
        /**
         * An item used from the player's inventory.
         */
        PLAYER_INVENTORY,
        /**
         * An item used from the player's inventory, but right-clicked on a block face, has block position and side in
         * addition to the above.
         */
        PLAYER_INVENTORY_WITH_BLOCK_CONTEXT, BLOCK, PART
    }

    private final Type type;
    private final int itemIndex;
    private final Identifier worldId;
    private final BlockPos blockPos;
    private final AEPartLocation side;

    private ContainerLocator(Type type, int itemIndex, World world, BlockPos blockPos, AEPartLocation side) {
        this(type, itemIndex, world.getRegistryKey().getValue(), blockPos, side);
    }

    private ContainerLocator(Type type, int itemIndex, Identifier worldId, BlockPos blockPos, AEPartLocation side) {
        this.type = type;
        this.itemIndex = itemIndex;
        this.worldId = worldId;
        this.blockPos = blockPos;
        this.side = side;
    }

    public static ContainerLocator forTileEntity(BlockEntity te) {
        if (te.getWorld() == null) {
            throw new IllegalArgumentException("Cannot open a block entity that is not in a world");
        }
        return new ContainerLocator(Type.BLOCK, -1, te.getWorld(), te.getPos(), null);
    }

    public static ContainerLocator forTileEntitySide(BlockEntity te, Direction side) {
        if (te.getWorld() == null) {
            throw new IllegalArgumentException("Cannot open a block entity that is not in a world");
        }
        return new ContainerLocator(Type.PART, -1, te.getWorld(), te.getPos(), AEPartLocation.fromFacing(side));
    }

    /**
     * Construct a container locator for an item being used on a block. The item could still open a container for
     * itself, but it might also open a special container for the block being right-clicked.
     */
    public static ContainerLocator forItemUseContext(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            throw new IllegalArgumentException("Cannot open a container without a player");
        }
        int slot = getPlayerInventorySlotFromHand(player, context.getHand());
        AEPartLocation side = AEPartLocation.fromFacing(context.getSide());
        return new ContainerLocator(Type.PLAYER_INVENTORY_WITH_BLOCK_CONTEXT, slot, player.world, context.getBlockPos(),
                side);
    }

    public static ContainerLocator forHand(PlayerEntity player, Hand hand) {
        int slot = getPlayerInventorySlotFromHand(player, hand);
        return new ContainerLocator(Type.PLAYER_INVENTORY, slot, (Identifier) null, null, null);
    }

    private static int getPlayerInventorySlotFromHand(PlayerEntity player, Hand hand) {
        ItemStack is = player.getStackInHand(hand);
        if (is.isEmpty()) {
            throw new IllegalArgumentException("Cannot open an item-inventory with empty hands");
        }
        int invSize = player.getInventory().size();
        for (int i = 0; i < invSize; i++) {
            if (player.getInventory().getStack(i) == is) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find item held in hand " + hand + " in player inventory");
    }

    public static ContainerLocator forPart(AEBasePart part) {
        IPartHost host = part.getHost();
        DimensionalCoord pos = host.getLocation();
        return new ContainerLocator(Type.PART, -1, pos.getWorld(), pos.getBlockPos(), part.getSide());
    }

    public boolean hasItemIndex() {
        return type == Type.PLAYER_INVENTORY || type == Type.PLAYER_INVENTORY_WITH_BLOCK_CONTEXT;
    }

    public int getItemIndex() {
        Preconditions.checkState(hasItemIndex());
        return itemIndex;
    }

    public Identifier getWorldId() {
        return worldId;
    }

    public boolean hasBlockPos() {
        return type == Type.BLOCK || type == Type.PART || type == Type.PLAYER_INVENTORY_WITH_BLOCK_CONTEXT;
    }

    public BlockPos getBlockPos() {
        Preconditions.checkState(hasBlockPos());
        return blockPos;
    }

    public boolean hasSide() {
        return type == Type.PART || type == Type.PLAYER_INVENTORY_WITH_BLOCK_CONTEXT;
    }

    public AEPartLocation getSide() {
        Preconditions.checkState(hasSide());
        return side;
    }

    public void write(PacketByteBuf buf) {
        switch (type) {
            case PLAYER_INVENTORY:
                buf.writeByte(0);
                buf.writeInt(itemIndex);
                break;
            case PLAYER_INVENTORY_WITH_BLOCK_CONTEXT:
                buf.writeByte(1);
                buf.writeInt(itemIndex);
                buf.writeIdentifier(worldId);
                buf.writeBlockPos(blockPos);
                buf.writeByte(side.ordinal());
                break;
            case BLOCK:
                buf.writeByte(2);
                buf.writeIdentifier(worldId);
                buf.writeBlockPos(blockPos);
                break;
            case PART:
                buf.writeByte(3);
                buf.writeIdentifier(worldId);
                buf.writeBlockPos(blockPos);
                buf.writeByte(side.ordinal());
                break;
            default:
                throw new IllegalStateException("Unsupported ContainerLocator type: " + type);
        }
    }

    public static ContainerLocator read(PacketByteBuf buf) {
        byte type = buf.readByte();
        switch (type) {
            case 0:
                return new ContainerLocator(Type.PLAYER_INVENTORY, buf.readInt(), (Identifier) null, null, null);
            case 1:
                return new ContainerLocator(Type.PLAYER_INVENTORY_WITH_BLOCK_CONTEXT, buf.readInt(),
                        buf.readIdentifier(), buf.readBlockPos(), AEPartLocation.values()[buf.readByte()]);
            case 2:
                return new ContainerLocator(Type.BLOCK, -1, buf.readIdentifier(), buf.readBlockPos(), null);
            case 3:
                return new ContainerLocator(Type.PART, -1, buf.readIdentifier(), buf.readBlockPos(),
                        AEPartLocation.values()[buf.readByte()]);
            default:
                throw new DecoderException("ContainerLocator type out of range: " + type);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(type.name());
        result.append('{');
        if (hasItemIndex()) {
            result.append("slot=").append(itemIndex).append(',');
        }
        if (hasBlockPos()) {
            result.append("dim=").append(worldId).append(',');
            result.append("pos=").append(blockPos).append(',');
        }
        if (hasSide()) {
            result.append("side=").append(side).append(',');
        }
        if (result.charAt(result.length() - 1) == ',') {
            result.setLength(result.length() - 1);
        }
        result.append('}');
        return result.toString();
    }

}
