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


import appeng.api.util.AEColor;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;


public class PacketPaintedEntity extends AppEngPacket {

    private final AEColor myColor;
    private final int entityId;
    private int ticks;

    // automatic.
    public PacketPaintedEntity(final ByteBuf stream) {
        this.entityId = stream.readInt();
        this.myColor = AEColor.values()[stream.readByte()];
        this.ticks = stream.readInt();
    }

    // api
    public PacketPaintedEntity(final int myEntity, final AEColor myColor, final int ticksLeft) {

        final ByteBuf data = Unpooled.buffer();

        data.writeInt(this.getPacketID());
        data.writeInt(this.entityId = myEntity);
        data.writeByte((this.myColor = myColor).ordinal());
        data.writeInt(ticksLeft);

        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player) {
        final PlayerColor pc = new PlayerColor(this.entityId, this.myColor, this.ticks);
        TickHandler.INSTANCE.getPlayerColors().put(this.entityId, pc);
    }
}
