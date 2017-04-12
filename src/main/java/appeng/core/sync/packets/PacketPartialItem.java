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


import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;


public class PacketPartialItem extends AppEngPacket
{

	private final short pageNum;
	private final byte[] data;

	// automatic.
	public PacketPartialItem( final ByteBuf stream )
	{
		this.pageNum = stream.readShort();
		stream.readBytes( this.data = new byte[stream.readableBytes()] );
	}

	// api
	public PacketPartialItem( final int page, final int maxPages, final byte[] buf )
	{

		final ByteBuf data = Unpooled.buffer();

		this.pageNum = (short) ( page | ( maxPages << 8 ) );
		this.data = buf;
		data.writeInt( this.getPacketID() );
		data.writeShort( this.pageNum );
		data.writeBytes( buf );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player )
	{
		if( player.openContainer instanceof AEBaseContainer )
		{
			( (AEBaseContainer) player.openContainer ).postPartial( this );
		}
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
}
