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

package appeng.core.sync.network;


import io.netty.buffer.ByteBuf;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketThreadUtil;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandlerBase;
import appeng.core.sync.PacketCallState;


public final class AppEngServerPacketHandler extends AppEngPacketHandlerBase implements IPacketHandler
{

	@Override
	public void onPacketData( final INetworkInfo manager, INetHandler handler, FMLProxyPacket packet, final EntityPlayer player )
	{
		ByteBuf stream = packet.payload();
		int packetType = -1;

		try
		{
			packetType = stream.readInt();
			AppEngPacket pack = PacketTypes.getPacket( packetType ).parsePacket( stream );
			
			PacketCallState callState = 
			 new PacketCallState(){
				
				@Override
				public void call(
						AppEngPacket appEngPacket )
				{
					appEngPacket.serverPacketData( manager, appEngPacket, player);
				}
			};
			
			pack.setCallParam(callState);			
			PacketThreadUtil.checkThreadAndEnqueue( pack, handler, ((EntityPlayerMP)player).getServerForPlayer() );
			callState.call( pack );
		}
		catch( InstantiationException e )
		{
			AELog.error( e );
		}
		catch( IllegalAccessException e )
		{
			AELog.error( e );
		}
		catch( IllegalArgumentException e )
		{
			AELog.error( e );
		}
		catch( InvocationTargetException e )
		{
			AELog.error( e );
		}
	}
}
