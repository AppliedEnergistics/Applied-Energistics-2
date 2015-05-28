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
import net.minecraft.entity.player.EntityPlayerMP;
import appeng.api.util.DimensionalCoord;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.services.compass.ICompassCallback;


public class PacketCompassRequest extends AppEngPacket implements ICompassCallback
{

	public final long attunement;
	public final int cx;
	public final int cz;
	public final int cdy;

	EntityPlayer talkBackTo;

	// automatic.
	public PacketCompassRequest( ByteBuf stream )
	{
		this.attunement = stream.readLong();
		this.cx = stream.readInt();
		this.cz = stream.readInt();
		this.cdy = stream.readInt();
	}

	// api
	public PacketCompassRequest( long attunement, int cx, int cz, int cdy )
	{

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeLong( this.attunement = attunement );
		data.writeInt( this.cx = cx );
		data.writeInt( this.cz = cz );
		data.writeInt( this.cdy = cdy );

		this.configureWrite( data );
	}

	@Override
	public void calculatedDirection( boolean hasResult, boolean spin, double radians, double dist )
	{
		NetworkHandler.instance.sendTo( new PacketCompassResponse( this, hasResult, spin, radians ), (EntityPlayerMP) this.talkBackTo );
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		this.talkBackTo = player;

		DimensionalCoord loc = new DimensionalCoord( player.worldObj, this.cx << 4, this.cdy << 5, this.cz << 4 );
		WorldData.instance().compassData().service().getCompassDirection( loc, 174, this );
	}
}
