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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.player.Player;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.block.networking.CableBusBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.me.networktool.NetworkToolContainer;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.NetworkToolItem;
import appeng.items.tools.powered.ColorApplicatorItem;
import net.minecraft.world.phys.Vec3;

public class ClickPacket extends BasePacket {

    private final int x;
    private final int y;
    private final int z;
    private net.minecraft.core.Direction side;
    private final float hitX;
    private final float hitY;
    private final float hitZ;
    private InteractionHand hand;
    private final boolean leftClick;

    public ClickPacket(final FriendlyByteBuf stream) {
        this.x = stream.readInt();
        this.y = stream.readInt();
        this.z = stream.readInt();
        byte side = stream.readByte();
        if (side != -1) {
            this.side = net.minecraft.core.Direction.values()[side];
        } else {
            this.side = null;
        }
        this.hitX = stream.readFloat();
        this.hitY = stream.readFloat();
        this.hitZ = stream.readFloat();
        this.hand = InteractionHand.values()[stream.readByte()];
        this.leftClick = stream.readBoolean();
    }

    // API for when a block was right clicked
    public ClickPacket(UseOnContext context) {
        this(context.getClickedPos(), context.getClickedFace(), context.getClickedPos().getX(), context.getClickedPos().getY(),
                context.getClickedPos().getZ(), context.getHand());
    }

    // API for when an item in hand was right-clicked, with no block context
    public ClickPacket(InteractionHand hand) {
        this(BlockPos.ZERO, null, 0, 0, 0, hand);
    }

    private ClickPacket(final BlockPos pos, final net.minecraft.core.Direction side, final float hitX, final float hitY, final float hitZ,
                        final InteractionHand hand) {
        this(pos, side, hitX, hitY, hitZ, hand, false);
    }

    public ClickPacket(final net.minecraft.core.BlockPos pos, final net.minecraft.core.Direction side, final float hitX, final float hitY, final float hitZ,
                       final InteractionHand hand, boolean leftClick) {

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

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
    public void serverPacketData(final INetworkInfo manager, final Player player) {
        final net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(this.x, this.y, this.z);

        final ItemStack is = player.getItemInHand(hand);
        final ItemDefinition<?> maybeMemoryCard = AEItems.MEMORY_CARD;
        final ItemDefinition<?> maybeColorApplicator = AEItems.COLOR_APPLICATOR;

        if (this.leftClick) {
            final Block block = player.level.getBlockState(pos).getBlock();
            if (block instanceof CableBusBlock) {
                ((CableBusBlock) block).onBlockClickPacket(player.level, pos, player, this.hand,
                        new Vec3(this.hitX, this.hitY, this.hitZ));
            }
        } else if (!is.isEmpty()) {
            if (is.getItem() instanceof NetworkToolItem tnt) {
                if (hasBlockContext()) {
                    // Reconstruct an item use context
                    UseOnContext useContext = new UseOnContext(player, hand,
                            new BlockHitResult(new Vec3(hitX, hitY, hitZ), side, pos, false));
                    tnt.serverSideToolLogic(useContext);
                } else {
                    ContainerOpener.openContainer(NetworkToolContainer.TYPE, player,
                            ContainerLocator.forHand(player, hand));
                }
            }

            if (maybeMemoryCard.isSameAs(is)) {
                final IMemoryCard mem = (IMemoryCard) is.getItem();
                mem.notifyUser(player, MemoryCardMessages.SETTINGS_CLEARED);
                is.setTag(null);
            } else if (maybeColorApplicator.isSameAs(is)) {
                final ColorApplicatorItem mem = (ColorApplicatorItem) is.getItem();
                mem.cycleColors(is, mem.getColor(is), 1);
            }
        }
    }
}
