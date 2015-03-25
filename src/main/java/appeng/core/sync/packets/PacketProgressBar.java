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
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;


public class PacketProgressBar extends AppEngPacket
{

	final short id;
	final long value;

	// automatic.
	public PacketProgressBar( ByteBuf stream )
	{
		this.id = stream.readShort();
		this.value = stream.readLong();
	}

	// api
	public PacketProgressBar( int short_id, long value )
	{
		this.id = (short) short_id;
		this.value = value;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeShort( short_id );
		data.writeLong( value );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		Container c = player.openContainer;
		if( c instanceof AEBaseContainer )
			( (AEBaseContainer) c ).updateFullProgressBar( this.id, this.value );
	}

	@Override
	public void clientPacketData( INetworkInfo network, AppEngPacket packet, EntityPlayer player )
	{
		Container c = player.openContainer;
		if( c instanceof AEBaseContainer )
			( (AEBaseContainer) c ).updateFullProgressBar( this.id, this.value );
	}
}
