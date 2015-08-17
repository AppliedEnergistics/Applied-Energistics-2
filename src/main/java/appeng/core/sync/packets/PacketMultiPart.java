/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IFMP;


public class PacketMultiPart implements AppEngPacket, AppEngPacketHandler<PacketMultiPart, IMessage>
{

	// automatic.
	public PacketMultiPart()
	{
	}

	@Override
	public IMessage onMessage( PacketMultiPart message, MessageContext ctx )
	{
		final IFMP fmp = (IFMP) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.FMP );
		if( fmp != null )
		{
			final EntityPlayerMP sender = (EntityPlayerMP) ctx.getServerHandler().playerEntity;
			MinecraftForge.EVENT_BUS.post( fmp.newFMPPacketEvent( sender ) ); // when received it just posts this event.
		}
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
	}
}
