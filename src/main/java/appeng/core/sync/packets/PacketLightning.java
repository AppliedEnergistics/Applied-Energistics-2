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

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.client.ClientHelper;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.helpers.Reflected;
import appeng.util.Platform;


public class PacketLightning implements AppEngPacket, AppEngPacketHandler<PacketLightning, AppEngPacket>
{

	private float x;
	private float y;
	private float z;

	@Reflected
	public PacketLightning()
	{
		// automatic.
	}

	// api
	public PacketLightning( final double x, final double y, final double z )
	{
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	@Override
	public AppEngPacket onMessage( final PacketLightning message, final MessageContext ctx )
	{
		try
		{
			if( Platform.isClient() && AEConfig.instance.enableEffects )
			{
				final LightningFX fx = new LightningFX( ClientHelper.proxy.getWorld(), message.x, message.y, message.z, 0.0f, 0.0f, 0.0f );
				Minecraft.getMinecraft().effectRenderer.addEffect( fx );
			}
		}
		catch( final Exception ignored )
		{
			// ignore
		}

		return null;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.x = buf.readFloat();
		this.y = buf.readFloat();
		this.z = buf.readFloat();
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeFloat( this.x );
		buf.writeFloat( this.y );
		buf.writeFloat( this.z );
	}
}
