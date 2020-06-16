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

package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;

import appeng.api.AEApi;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IItems;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.block.networking.BlockCableBus;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.ToolNetworkTool;
import appeng.items.tools.powered.ToolColorApplicator;

public class PacketClick extends AppEngPacket {

    private final int x;
    private final int y;
    private final int z;
    private Direction side;
    private final float hitX;
    private final float hitY;
    private final float hitZ;
    private Hand hand;
    private final boolean leftClick;

    public PacketClick(final PacketBuffer stream) {
        this.x = stream.readInt();
        this.y = stream.readInt();
        this.z = stream.readInt();
        byte side = stream.readByte();
        if (side != -1) {
            this.side = Direction.values()[side];
        } else {
            this.side = null;
        }
        this.hitX = stream.readFloat();
        this.hitY = stream.readFloat();
        this.hitZ = stream.readFloat();
        this.hand = Hand.values()[stream.readByte()];
        this.leftClick = stream.readBoolean();
    }

    // API for when a block was right clicked
    public PacketClick(ItemUseContext context) {
        this(context.getPos(), context.getFace(), context.getPos().getX(), context.getPos().getY(),
                context.getPos().getZ(), context.getHand());
    }

    // API for when an item in hand was right-clicked, with no block context
    public PacketClick(Hand hand) {
        this(BlockPos.ZERO, null, 0, 0, 0, hand);
    }

    private PacketClick(final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ,
            final Hand hand) {
        this(pos, side, hitX, hitY, hitZ, hand, false);
    }

    public PacketClick(final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ,
            final Hand hand, boolean leftClick) {

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(this.x = pos.getX());
        data.writeInt(this.y = pos.getY());
        data.writeInt(this.z = pos.getZ());
        if (side == null) {
            data.writeByte(-1);
        } else {
            data.writeByte(side.ordinal());
        }
        data.writeFloat(this.hitX = hitX);
        data.writeFloat(this.hitY = hitY);
        data.writeFloat(this.hitZ = hitZ);
        data.writeByte(hand.ordinal());
        data.writeBoolean(this.leftClick = leftClick);

        this.configureWrite(data);
    }

    // Indicates that block pos, side and hit vector have valid data
    private boolean hasBlockContext() {
        return side != null;
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final BlockPos pos = new BlockPos(this.x, this.y, this.z);

        final ItemStack is = player.getHeldItem(hand);
        final IItems items = AEApi.instance().definitions().items();
        final IComparableDefinition maybeMemoryCard = items.memoryCard();
        final IComparableDefinition maybeColorApplicator = items.colorApplicator();

        if (this.leftClick) {
            final Block block = player.world.getBlockState(pos).getBlock();
            if (block instanceof BlockCableBus) {
                ((BlockCableBus) block).onBlockClickPacket(player.world, pos, player, this.hand,
                        new Vec3d(this.hitX, this.hitY, this.hitZ));
            }
        } else {
            if (!is.isEmpty()) {
                if (is.getItem() instanceof ToolNetworkTool) {
                    final ToolNetworkTool tnt = (ToolNetworkTool) is.getItem();

                    if (hasBlockContext()) {
                        // Reconstruct an item use context
                        ItemUseContext useContext = new ItemUseContext(player, hand,
                                new BlockRayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos, false));
                        tnt.serverSideToolLogic(useContext);
                    } else {
                        ContainerOpener.openContainer(ContainerNetworkTool.TYPE, player,
                                ContainerLocator.forHand(player, hand));
                    }
                }

                if (maybeMemoryCard.isSameAs(is)) {
                    final IMemoryCard mem = (IMemoryCard) is.getItem();
                    mem.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
                    is.setTag(null);
                }

                else if (maybeColorApplicator.isSameAs(is)) {
                    final ToolColorApplicator mem = (ToolColorApplicator) is.getItem();
                    mem.cycleColors(is, mem.getColor(is), 1);
                }
            }
        }
    }
}
