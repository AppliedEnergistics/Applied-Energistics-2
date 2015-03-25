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

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.client.ClientHelper;
import appeng.client.render.effects.EnergyFx;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;


public class PacketTransitionEffect extends AppEngPacket
{

	final public boolean mode;
	final double x;
	final double y;
	final double z;
	final ForgeDirection d;

	// automatic.
	public PacketTransitionEffect( ByteBuf stream )
	{
		this.x = stream.readFloat();
		this.y = stream.readFloat();
		this.z = stream.readFloat();
		this.d = ForgeDirection.getOrientation( stream.readByte() );
		this.mode = stream.readBoolean();
	}

	// api
	public PacketTransitionEffect( double x, double y, double z, ForgeDirection dir, boolean wasBlock )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.d = dir;
		this.mode = wasBlock;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeFloat( (float) x );
		data.writeFloat( (float) y );
		data.writeFloat( (float) z );
		data.writeByte( this.d.ordinal() );
		data.writeBoolean( wasBlock );

		this.configureWrite( data );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void clientPacketData( INetworkInfo network, AppEngPacket packet, EntityPlayer player )
	{
		World world = ClientHelper.proxy.getWorld();

		for( int zz = 0; zz < ( this.mode ? 32 : 8 ); zz++ )
			if( CommonHelper.proxy.shouldAddParticles( Platform.getRandom() ) )
			{
				EnergyFx fx = new EnergyFx( world, this.x + ( this.mode ? ( Platform.getRandomInt() % 100 ) * 0.01 : ( Platform.getRandomInt() % 100 ) * 0.005 - 0.25 ), this.y + ( this.mode ? ( Platform.getRandomInt() % 100 ) * 0.01 : ( Platform.getRandomInt() % 100 ) * 0.005 - 0.25 ), this.z + ( this.mode ? ( Platform.getRandomInt() % 100 ) * 0.01 : ( Platform.getRandomInt() % 100 ) * 0.005 - 0.25 ), Items.diamond );

				if( !this.mode )
					fx.fromItem( this.d );

				fx.motionX = -0.1 * this.d.offsetX;
				fx.motionY = -0.1 * this.d.offsetY;
				fx.motionZ = -0.1 * this.d.offsetZ;

				Minecraft.getMinecraft().effectRenderer.addEffect( fx );
			}

		if( this.mode )
		{
			Block block = world.getBlock( (int) this.x, (int) this.y, (int) this.z );

			Minecraft.getMinecraft().getSoundHandler().playSound( new PositionedSoundRecord( new ResourceLocation( block.stepSound.getBreakSound() ), ( block.stepSound.getVolume() + 1.0F ) / 2.0F, block.stepSound.getPitch() * 0.8F, (float) this.x + 0.5F, (float) this.y + 0.5F, (float) this.z + 0.5F ) );
		}
	}
}
