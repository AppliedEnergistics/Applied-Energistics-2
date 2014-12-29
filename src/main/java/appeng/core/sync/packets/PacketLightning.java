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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.client.ClientHelper;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;

public class PacketLightning extends AppEngPacket
{

	final double x;
	final double y;
	final double z;

	// automatic.
	public PacketLightning(ByteBuf stream) {
		this.x = stream.readFloat();
		this.y = stream.readFloat();
		this.z = stream.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		try
		{
			if ( Platform.isClient() && AEConfig.instance.enableEffects )
			{
				LightningFX fx = new LightningFX( ClientHelper.proxy.getWorld(), this.x, this.y, this.z, 0.0f, 0.0f, 0.0f );
				Minecraft.getMinecraft().effectRenderer.addEffect( fx );
			}
		}
		catch (Exception ignored)
		{
		}
	}

	// api
	public PacketLightning(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeFloat( (float) x );
		data.writeFloat( (float) y );
		data.writeFloat( (float) z );

		this.configureWrite( data );
	}

}
