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

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.helpers.Reflected;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;


public class PacketCompassResponse implements AppEngPacket, AppEngPacketHandler<PacketCompassResponse, AppEngPacket>
{

	private long attunement;
	private int cx;
	private int cz;
	private int cdy;
	private CompassResult cr;
	private boolean hasResult;
	private boolean spin;
	private double radians;

	@Reflected
	public PacketCompassResponse()
	{
		// automatic.
	}

	// api
	public PacketCompassResponse( final PacketCompassRequest req, final boolean hasResult, final boolean spin, final double radians )
	{
		this.attunement = req.getAttunement();
		this.cx = req.getCx();
		this.cz = req.getCz();
		this.cdy = req.getCdy();
		this.hasResult = hasResult;
		this.spin = spin;
		this.radians = radians;

	}

	@Override
	public AppEngPacket onMessage( final PacketCompassResponse message, final MessageContext ctx )
	{
		CompassManager.INSTANCE.postResult( message.attunement, message.cx << 4, message.cdy << 5, message.cz << 4, message.cr );
		return null;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{

		this.attunement = buf.readLong();
		this.cx = buf.readInt();
		this.cz = buf.readInt();
		this.cdy = buf.readInt();

		this.cr = new CompassResult( buf.readBoolean(), buf.readBoolean(), buf.readDouble() );
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeLong( this.attunement );
		buf.writeInt( this.cx );
		buf.writeInt( this.cz );
		buf.writeInt( this.cdy );

		buf.writeBoolean( this.hasResult );
		buf.writeBoolean( this.spin );
		buf.writeDouble( this.radians );

	}
}