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

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.client.render.effects.MatterCannonFX;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;


public class PacketMatterCannon implements AppEngPacket, AppEngPacketHandler<PacketMatterCannon, IMessage>
{

	private float x;
	private float y;
	private float z;
	private float dx;
	private float dy;
	private float dz;
	private byte len;

	// automatic.
	public PacketMatterCannon()
	{
	}

	// api
	public PacketMatterCannon( final double x, final double y, final double z, final float dx, final float dy, final float dz, final byte len )
	{
		final float dl = dx * dx + dy * dy + dz * dz;
		final float dlz = (float) Math.sqrt( dl );

		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
		this.dx = dx / dlz;
		this.dy = dy / dlz;
		this.dz = dz / dlz;
		this.len = len;
	}

	@Override
	public IMessage onMessage( PacketMatterCannon message, MessageContext ctx )
	{
		try
		{
			final World world = FMLClientHandler.instance().getClient().theWorld;
			for( int a = 1; a < message.len; a++ )
			{
				final MatterCannonFX fx = new MatterCannonFX( world, message.x + message.dx * a, message.y + message.dy * a, message.z + message.dz * a,
						Items.diamond );

				Minecraft.getMinecraft().effectRenderer.addEffect( fx );
			}
		}
		catch( final Exception ignored )
		{
		}
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.x = buf.readFloat();
		this.y = buf.readFloat();
		this.z = buf.readFloat();
		this.dx = buf.readFloat();
		this.dy = buf.readFloat();
		this.dz = buf.readFloat();
		this.len = buf.readByte();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeFloat( x );
		buf.writeFloat( y );
		buf.writeFloat( z );
		buf.writeFloat( this.dx );
		buf.writeFloat( this.dy );
		buf.writeFloat( this.dz );
		buf.writeByte( len );
	}
}
