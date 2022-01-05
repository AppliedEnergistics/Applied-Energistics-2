/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;
import appeng.server.services.compass.CompassService;

public class CompassRequestPacket extends BasePacket {

    final long attunement;
    final int cx;
    final int cz;
    final int cdy;

    public CompassRequestPacket(FriendlyByteBuf stream) {
        this.attunement = stream.readLong();
        this.cx = stream.readInt();
        this.cz = stream.readInt();
        this.cdy = stream.readInt();
    }

    // api
    public CompassRequestPacket(long attunement, int cx, int cz, int cdy) {

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeLong(this.attunement = attunement);
        data.writeInt(this.cx = cx);
        data.writeInt(this.cz = cz);
        data.writeInt(this.cdy = cdy);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, ServerPlayer player) {
        var pos = new ChunkPos(this.cx, this.cz);
        var result = CompassService.getDirection(player.getLevel(), pos, 174);

        var responsePacket = new CompassResponsePacket(this, result.hasResult(), result.spin(), result.radians());
        NetworkHandler.instance().sendTo(responsePacket, player);
    }
}
