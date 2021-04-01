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
import net.minecraft.network.PacketBuffer;
import appeng.api.util.AEColor;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.hooks.ticking.PlayerColor;
import appeng.hooks.ticking.TickHandler;

public class PaintedEntityPacket extends BasePacket {

    private final AEColor myColor;
    private final int entityId;
    private int ticks;

    public PaintedEntityPacket(final PacketBuffer stream) {
        this.entityId = stream.readInt();
        this.myColor = AEColor.values()[stream.readByte()];
        this.ticks = stream.readInt();
    }

    // api
    public PaintedEntityPacket(final int myEntity, final AEColor myColor, final int ticksLeft) {

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(this.entityId = myEntity);
        data.writeByte((this.myColor = myColor).ordinal());
        data.writeInt(ticksLeft);

        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final PlayerColor pc = new PlayerColor(this.entityId, this.myColor, this.ticks);
        TickHandler.instance().getPlayerColors().put(this.entityId, pc);
    }
}
