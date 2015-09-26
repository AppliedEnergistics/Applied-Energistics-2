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

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.client.ClientHelper;
import appeng.client.render.effects.EnergyFx;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.helpers.Reflected;
import appeng.util.Platform;


public class PacketTransitionEffect implements AppEngPacket, AppEngPacketHandler<PacketTransitionEffect, AppEngPacket>
{

	private boolean mode;
	private double x;
	private double y;
	private double z;
	private ForgeDirection d;

	@Reflected
	public PacketTransitionEffect()
	{
		// automatic.
	}

	// api
	public PacketTransitionEffect( final double x, final double y, final double z, final ForgeDirection dir, final boolean wasBlock )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.d = dir;
		this.mode = wasBlock;
	}

	@Override
	public AppEngPacket onMessage( final PacketTransitionEffect message, final MessageContext ctx )
	{
		final World world = ClientHelper.proxy.getWorld();

		for( int zz = 0; zz < ( message.mode ? 32 : 8 ); zz++ )
		{
			if( CommonHelper.proxy.shouldAddParticles( Platform.getRandom() ) )
			{
				final EnergyFx fx = new EnergyFx( world, message.x + ( message.mode ? ( Platform.getRandomInt() % 100 ) * 0.01 : (
						Platform.getRandomInt() % 100 ) * 0.005 - 0.25 ), message.y + ( message.mode ? ( Platform.getRandomInt() % 100 ) * 0.01
								: ( Platform.getRandomInt() % 100 ) * 0.005 - 0.25 ), message.z + ( message.mode ? ( Platform.getRandomInt() % 100 ) *
										0.01 : ( Platform.getRandomInt() % 100 ) * 0.005 - 0.25 ), Items.diamond );

				if( !message.mode )
				{
					fx.fromItem( message.d );
				}

				fx.motionX = -0.1 * message.d.offsetX;
				fx.motionY = -0.1 * message.d.offsetY;
				fx.motionZ = -0.1 * message.d.offsetZ;

				Minecraft.getMinecraft().effectRenderer.addEffect( fx );
			}
		}

		if( message.mode )
		{
			final Block block = world.getBlock( (int) message.x, (int) message.y, (int) message.z );

			Minecraft.getMinecraft().getSoundHandler().playSound( new PositionedSoundRecord( new ResourceLocation(
					block.stepSound.getBreakSound() ), ( block.stepSound.getVolume() + 1.0F ) / 2.0F, block.stepSound.getPitch() *
					0.8F, (float) message.x + 0.5F, (float) message.y + 0.5F, (float) message.z + 0.5F ) );
		}
		return null;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.x = buf.readFloat();
		this.y = buf.readFloat();
		this.z = buf.readFloat();
		this.d = ForgeDirection.getOrientation( buf.readByte() );
		this.mode = buf.readBoolean();
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeFloat( (float) this.x );
		buf.writeFloat( (float) this.y );
		buf.writeFloat( (float) this.z );
		buf.writeByte( this.d.ordinal() );
		buf.writeBoolean( this.mode );
	}
}
