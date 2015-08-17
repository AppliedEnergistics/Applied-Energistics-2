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


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.EffectType;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.util.item.AEItemStack;


public class PacketAssemblerAnimation implements AppEngPacket, AppEngPacketHandler<PacketAssemblerAnimation, IMessage>
{

	private int x;
	private int y;
	private int z;
	private byte rate;
	private IAEItemStack is;

	// automatic.
	public PacketAssemblerAnimation()
	{
	}

	// api
	public PacketAssemblerAnimation( int x, int y, int z, byte rate, IAEItemStack is )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.rate = rate;
		this.is = is;
	}

	@Override
	public IMessage onMessage( PacketAssemblerAnimation message, MessageContext ctx )
	{
		final double d0 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);
		final double d1 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);
		final double d2 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);

		CommonHelper.proxy.spawnEffect( EffectType.Assembler, ctx.getServerHandler().playerEntity.getEntityWorld(), message.x + d0, message.y + d1, message.z +
				d2, message );

		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.rate = buf.readByte();
		try
		{
			this.is = AEItemStack.loadItemStackFromPacket( buf );
		}
		catch( IOException e )
		{
		}
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeInt( this.x );
		buf.writeInt( this.y );
		buf.writeInt( this.z );
		buf.writeByte( this.getRate() );
		try
		{
			this.is.writeToPacket( buf );
		}
		catch( IOException e )
		{
		}
	}

	public byte getRate()
	{
		return rate;
	}

	public IAEItemStack getIs()
	{
		return is;
	}
}
