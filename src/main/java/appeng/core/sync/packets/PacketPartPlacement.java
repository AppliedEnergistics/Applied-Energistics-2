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

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.parts.PartPlacement;


public class PacketPartPlacement implements AppEngPacket, AppEngPacketHandler<PacketPartPlacement, IMessage>
{

	private int x;
	private int y;
	private int z;
	private int face;
	private float eyeHeight;

	// automatic.
	public PacketPartPlacement()
	{
	}

	// api
	public PacketPartPlacement( final int x, final int y, final int z, final int face, final float eyeHeight )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.face = face;
		this.eyeHeight = eyeHeight;
	}

	@Override
	public IMessage onMessage( PacketPartPlacement message, MessageContext ctx )
	{
		final EntityPlayerMP sender = (EntityPlayerMP) ctx.getServerHandler().playerEntity;
		CommonHelper.proxy.updateRenderMode( sender );

		PartPlacement.setEyeHeight( message.eyeHeight );
		PartPlacement.place( sender.getHeldItem(), message.x, message.y, message.z, message.face, sender, sender.worldObj,
				PartPlacement.PlaceType.INTERACT_FIRST_PASS, 0 );
		CommonHelper.proxy.updateRenderMode( null );
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.face = buf.readByte();
		this.eyeHeight = buf.readFloat();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeInt( x );
		buf.writeInt( y );
		buf.writeInt( z );
		buf.writeByte( face );
		buf.writeFloat( eyeHeight );
	}
}
