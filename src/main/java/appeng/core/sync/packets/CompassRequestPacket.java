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
import net.minecraft.world.entity.player.Player;

import appeng.api.util.DimensionalBlockPos;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.services.compass.ICompassCallback;

public class CompassRequestPacket extends BasePacket implements ICompassCallback {

    final long attunement;
    final int cx;
    final int cz;
    final int cdy;

    private Player talkBackTo;

    public CompassRequestPacket(final FriendlyByteBuf stream) {
        this.attunement = stream.readLong();
        this.cx = stream.readInt();
        this.cz = stream.readInt();
        this.cdy = stream.readInt();
    }

    // api
    public CompassRequestPacket(final long attunement, final int cx, final int cz, final int cdy) {

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeLong(this.attunement = attunement);
        data.writeInt(this.cx = cx);
        data.writeInt(this.cz = cz);
        data.writeInt(this.cdy = cdy);

        this.configureWrite(data);
    }

    @Override
    public void calculatedDirection(final boolean hasResult, final boolean spin, final double radians,
            final double dist) {
        NetworkHandler.instance().sendTo(new CompassResponsePacket(this, hasResult, spin, radians),
                (ServerPlayer) this.talkBackTo);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final Player player) {
        this.talkBackTo = player;

        final DimensionalBlockPos loc = new DimensionalBlockPos(player.level, this.cx << 4, this.cdy << 5,
                this.cz << 4);
        WorldData.instance().compassData().service().getCompassDirection(loc, 174, this);
    }
}
