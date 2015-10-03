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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.api.util.DimensionalCoord;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.helpers.Reflected;
import appeng.services.compass.ICompassCallback;


public class PacketCompassRequest implements AppEngPacket, AppEngPacketHandler<PacketCompassRequest, AppEngPacket>, ICompassCallback
{

	private static final int MAX_COMPASS_RANGE = 174;

	private long attunement;
	private int cx;
	private int cz;
	private int cdy;
	private EntityPlayer talkBackTo;

	@Reflected
	public PacketCompassRequest()
	{
		// automatic.
	}

	// api
	public PacketCompassRequest( final long attunement, final int cx, final int cz, final int cdy )
	{
		this.attunement = attunement;
		this.cx = cx;
		this.cz = cz;
		this.cdy = cdy;
	}

	@Override
	public void calculatedDirection( final boolean hasResult, final boolean spin, final double radians, final double dist )
	{
		this.talkBackTo = Minecraft.getMinecraft().thePlayer;
		NetworkHandler.INSTANCE.sendTo( new PacketCompassResponse( this, hasResult, spin, radians ), (EntityPlayerMP) this.talkBackTo );
	}

	@Override
	public AppEngPacket onMessage( final PacketCompassRequest message, final MessageContext ctx )
	{
		message.talkBackTo = ctx.getServerHandler().playerEntity;

		final DimensionalCoord loc = new DimensionalCoord( ctx.getServerHandler().playerEntity.worldObj, message.cx << 4, message.cdy << 5, message.cz << 4 );
		WorldData.instance().compassData().service().getCompassDirection( loc, MAX_COMPASS_RANGE, message );

		return null;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.attunement = buf.readLong();
		this.cx = buf.readInt();
		this.cz = buf.readInt();
		this.cdy = buf.readInt();
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeLong( this.attunement );
		buf.writeInt( this.cx );
		buf.writeInt( this.cz );
		buf.writeInt( this.cdy );
	}

	public long getAttunement()
	{
		return this.attunement;
	}

	public int getCx()
	{
		return this.cx;
	}

	public int getCz()
	{
		return this.cz;
	}

	public int getCdy()
	{
		return this.cdy;
	}
}
