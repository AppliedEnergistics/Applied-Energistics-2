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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import appeng.core.WorldSettings;
import appeng.core.sync.AppEngPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

public class NetworkHandler
{

	public static NetworkHandler instance;

	final FMLEventChannel ec;
	final String myChannelName;

	final IPacketHandler clientHandler;
	final IPacketHandler serveHandler;

	public NetworkHandler(String channelName) {
		FMLCommonHandler.instance().bus().register( this );
		ec = NetworkRegistry.INSTANCE.newEventDrivenChannel( myChannelName = channelName );
		ec.register( this );

		clientHandler = createClientSide();
		serveHandler = createServerSide();
	}

	private IPacketHandler createServerSide()
	{
		try
		{
			return new AppEngServerPacketHandler();
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	private IPacketHandler createClientSide()
	{
		try
		{
			return new AppEngClientPacketHandler();
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	@SubscribeEvent
	public void newConnection(ServerConnectionFromClientEvent ev)
	{
		WorldSettings.getInstance().sendToPlayer( ev.manager, null );
	}

	@SubscribeEvent
	public void newConnection(PlayerLoggedInEvent loginEvent)
	{
		if ( loginEvent.player instanceof EntityPlayerMP )
			WorldSettings.getInstance().sendToPlayer( null, (EntityPlayerMP) loginEvent.player );
	}

	@SubscribeEvent
	public void serverPacket(ServerCustomPacketEvent ev)
	{
		NetHandlerPlayServer srv = (NetHandlerPlayServer) ev.packet.handler();
		if ( serveHandler != null )
			serveHandler.onPacketData( null, ev.packet, srv.playerEntity );
	}

	@SubscribeEvent
	public void clientPacket(ClientCustomPacketEvent ev)
	{
		if ( clientHandler != null )
			clientHandler.onPacketData( null, ev.packet, null );
	}

	public String getChannel()
	{
		return myChannelName;
	}

	public void sendToAll(AppEngPacket message)
	{
		ec.sendToAll( message.getProxy() );
	}

	public void sendTo(AppEngPacket message, EntityPlayerMP player)
	{
		ec.sendTo( message.getProxy(), player );
	}

	public void sendToAllAround(AppEngPacket message, NetworkRegistry.TargetPoint point)
	{
		ec.sendToAllAround( message.getProxy(), point );
	}

	public void sendToDimension(AppEngPacket message, int dimensionId)
	{
		ec.sendToDimension( message.getProxy(), dimensionId );
	}

	public void sendToServer(AppEngPacket message)
	{
		ec.sendToServer( message.getProxy() );
	}

}
