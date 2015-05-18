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


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;


public final class PacketCompassResponse extends AppEngPacket
{

	public final long attunement;
	public final int cx;
	public final int cz;
	public final int cdy;

	public CompassResult cr;

	// automatic.
	public PacketCompassResponse( ByteBuf stream )
	{
		this.attunement = stream.readLong();
		this.cx = stream.readInt();
		this.cz = stream.readInt();
		this.cdy = stream.readInt();

		this.cr = new CompassResult( stream.readBoolean(), stream.readBoolean(), stream.readDouble() );
	}

	// api
	public PacketCompassResponse( PacketCompassRequest req, boolean hasResult, boolean spin, double radians )
	{

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeLong( this.attunement = req.attunement );
		data.writeInt( this.cx = req.cx );
		data.writeInt( this.cz = req.cz );
		data.writeInt( this.cdy = req.cdy );

		data.writeBoolean( hasResult );
		data.writeBoolean( spin );
		data.writeDouble( radians );

		this.configureWrite( data );
	}

	@Override
	public void clientPacketData( INetworkInfo network, AppEngPacket packet, EntityPlayer player )
	{
		CompassManager.INSTANCE.postResult( this.attunement, this.cx << 4, this.cdy << 5, this.cz << 4, this.cr );
	}
}