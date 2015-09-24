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
import net.minecraft.inventory.Container;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;


public class PacketProgressBar implements AppEngPacket, AppEngPacketHandler<PacketProgressBar, AppEngPacket>
{

	private short id;
	private long value;

	// automatic.
	public PacketProgressBar()
	{
	}

	// api
	public PacketProgressBar( final int shortID, final long value )
	{
		this.id = (short) shortID;
		this.value = value;
	}

	@Override
	public AppEngPacket onMessage( PacketProgressBar message, MessageContext ctx )
	{
		if( ctx.side == Side.CLIENT )
		{
			final Container c = Minecraft.getMinecraft().thePlayer.openContainer;
			if( c instanceof AEBaseContainer )
			{
				( (AEBaseContainer) c ).updateFullProgressBar( message.id, message.value );
			}
		}
		else
		{
			final Container c = ctx.getServerHandler().playerEntity.openContainer;
			if( c instanceof AEBaseContainer )
			{
				( (AEBaseContainer) c ).updateFullProgressBar( message.id, message.value );
			}
		}
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.id = buf.readShort();
		this.value = buf.readLong();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeShort( this.id );
		buf.writeLong( value );
	}
}
