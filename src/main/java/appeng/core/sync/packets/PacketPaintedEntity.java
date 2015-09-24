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


import io.netty.buffer.ByteBuf;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.api.util.AEColor;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;


public class PacketPaintedEntity implements AppEngPacket, AppEngPacketHandler<PacketPaintedEntity, AppEngPacket>
{

	private AEColor myColor;
	private int entityId;
	private int ticks;

	// automatic.
	public PacketPaintedEntity()
	{
	}

	// api
	public PacketPaintedEntity( final int myEntity, final AEColor myColor, final int ticksLeft )
	{
		this.entityId = myEntity;
		this.myColor = myColor;
		this.ticks = ticksLeft;
	}

	@Override
	public AppEngPacket onMessage( PacketPaintedEntity message, MessageContext ctx )
	{
		final PlayerColor pc = new PlayerColor( message.entityId, message.myColor, message.ticks );
		TickHandler.INSTANCE.getPlayerColors().put( message.entityId, pc );
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.entityId = buf.readInt();
		this.myColor = AEColor.values()[buf.readByte()];
		this.ticks = buf.readInt();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeInt( this.entityId );
		buf.writeByte( this.myColor.ordinal() );
		buf.writeInt( this.ticks );
	}
}
