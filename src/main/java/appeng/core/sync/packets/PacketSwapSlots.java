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

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;


public class PacketSwapSlots implements AppEngPacket, AppEngPacketHandler<PacketSwapSlots, IMessage>
{

	private int slotA;
	private int slotB;

	// automatic.
	public PacketSwapSlots()
	{
	}

	// api
	public PacketSwapSlots( final int slotA, final int slotB )
	{
		this.slotA = slotA;
		this.slotB = slotB;
	}

	@Override
	public IMessage onMessage( PacketSwapSlots message, MessageContext ctx )
	{
		if( ctx.getServerHandler().playerEntity != null && ctx.getServerHandler().playerEntity.openContainer instanceof AEBaseContainer )
		{
			( (AEBaseContainer) ctx.getServerHandler().playerEntity.openContainer ).swapSlotContents( message.slotA, message.slotB );
		}
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.slotA = buf.readInt();
		this.slotB = buf.readInt();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeInt( this.slotA );
		buf.writeInt( this.slotB );

	}
}
