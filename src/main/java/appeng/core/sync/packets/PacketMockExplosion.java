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

import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;


public class PacketMockExplosion implements AppEngPacket, AppEngPacketHandler<PacketMockExplosion, AppEngPacket>
{

	private double x;
	private double y;
	private double z;

	// automatic.
	public PacketMockExplosion()
	{
	}

	// api
	public PacketMockExplosion( final double x, final double y, final double z )
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public AppEngPacket onMessage( PacketMockExplosion message, MessageContext ctx )
	{
		final World world = CommonHelper.proxy.getWorld();
		world.spawnParticle( "largeexplode", message.x, message.y, message.z, 1.0D, 0.0D, 0.0D );
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.x = buf.readDouble();
		this.y = buf.readDouble();
		this.z = buf.readDouble();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeDouble( x );
		buf.writeDouble( y );
		buf.writeDouble( z );
	}
}
