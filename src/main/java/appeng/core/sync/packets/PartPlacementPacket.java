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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import appeng.core.AppEng;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.parts.PartPlacement;

public class PartPlacementPacket extends BasePacket {

    private int x;
    private int y;
    private int z;
    private int face;
    private float eyeHeight;
    private Hand hand;

    public PartPlacementPacket(final PacketBuffer stream) {
        this.x = stream.readInt();
        this.y = stream.readInt();
        this.z = stream.readInt();
        this.face = stream.readByte();
        this.eyeHeight = stream.readFloat();
        this.hand = Hand.values()[stream.readByte()];
    }

    // api
    public PartPlacementPacket(final BlockPos pos, final Direction face, final float eyeHeight, final Hand hand) {
        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(pos.getX());
        data.writeInt(pos.getY());
        data.writeInt(pos.getZ());
        data.writeByte(face.ordinal());
        data.writeFloat(eyeHeight);
        data.writeByte(hand.ordinal());

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final ServerPlayerEntity sender = (ServerPlayerEntity) player;
        AppEng.proxy.setPartInteractionPlayer(sender);
        try {
            PartPlacement.setEyeHeight(this.eyeHeight);
            PartPlacement.place(sender.getHeldItem(this.hand), new BlockPos(this.x, this.y, this.z),
                    Direction.values()[this.face], sender, this.hand, sender.world,
                    PartPlacement.PlaceType.INTERACT_FIRST_PASS, 0);
        } finally {
            AppEng.proxy.setPartInteractionPlayer(null);
        }
    }
}
