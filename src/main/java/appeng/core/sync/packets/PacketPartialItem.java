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

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;


public class PacketPartialItem implements AppEngPacket, AppEngPacketHandler<PacketPartialItem, IMessage>
{

	private short pageNum;
	private byte[] data;

	// automatic.
	public PacketPartialItem()
	{
	}

	// api
	public PacketPartialItem( final int page, final int maxPages, final byte[] buf )
	{
		this.pageNum = (short) ( page | ( maxPages << 8 ) );
		this.data = buf;
	}

	@Override
	public IMessage onMessage( PacketPartialItem message, MessageContext ctx )
	{
		if( ctx.getServerHandler().playerEntity.openContainer instanceof AEBaseContainer )
		{
			( (AEBaseContainer) ctx.getServerHandler().playerEntity.openContainer ).postPartial( message );
		}
		return null;
	}

	public int getPageCount()
	{
		return this.pageNum >> 8;
	}

	public int getSize()
	{
		return this.data.length;
	}

	public int write( final byte[] buffer, final int cursor )
	{
		System.arraycopy( this.data, 0, buffer, cursor, this.data.length );
		return cursor + this.data.length;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.pageNum = buf.readShort();
		buf.readBytes( this.data = new byte[buf.readableBytes()] );
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeShort( this.pageNum );
		buf.writeBytes( this.data );
	}
}
