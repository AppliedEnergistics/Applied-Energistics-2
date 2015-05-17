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

package appeng.core.sync.network;


import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

import appeng.core.WorldSettings;
import appeng.core.sync.AppEngPacket;


public final class NetworkHandler
{

	public static NetworkHandler instance;

	final FMLEventChannel ec;
	final String myChannelName;

	final IPacketHandler clientHandler;
	final IPacketHandler serveHandler;

	public NetworkHandler( String channelName )
	{
		FMLCommonHandler.instance().bus().register( this );
		this.ec = NetworkRegistry.INSTANCE.newEventDrivenChannel( this.myChannelName = channelName );
		this.ec.register( this );

		this.clientHandler = this.createClientSide();
		this.serveHandler = this.createServerSide();
	}

	private IPacketHandler createClientSide()
	{
		try
		{
			return new AppEngClientPacketHandler();
		}
		catch( Throwable t )
		{
			return null;
		}
	}

	private IPacketHandler createServerSide()
	{
		try
		{
			return new AppEngServerPacketHandler();
		}
		catch( Throwable t )
		{
			return null;
		}
	}

	@SubscribeEvent
	public void newConnection( ServerConnectionFromClientEvent ev )
	{
		WorldSettings.getInstance().sendToPlayer( ev.manager );
	}

	@SubscribeEvent
	public void newConnection( PlayerLoggedInEvent loginEvent )
	{
		if( loginEvent.player instanceof EntityPlayerMP )
		{
			WorldSettings.getInstance().sendToPlayer( null );
		}
	}

	@SubscribeEvent
	public void serverPacket( ServerCustomPacketEvent ev )
	{
		NetHandlerPlayServer srv = (NetHandlerPlayServer) ev.packet.handler();
		if( this.serveHandler != null )
		{
			this.serveHandler.onPacketData( null, ev.packet, srv.playerEntity );
		}
	}

	@SubscribeEvent
	public void clientPacket( ClientCustomPacketEvent ev )
	{
		if( this.clientHandler != null )
		{
			this.clientHandler.onPacketData( null, ev.packet, null );
		}
	}

	public final String getChannel()
	{
		return this.myChannelName;
	}

	public final void sendToAll( AppEngPacket message )
	{
		this.ec.sendToAll( message.getProxy() );
	}

	public final void sendTo( AppEngPacket message, EntityPlayerMP player )
	{
		this.ec.sendTo( message.getProxy(), player );
	}

	public final void sendToAllAround( AppEngPacket message, NetworkRegistry.TargetPoint point )
	{
		this.ec.sendToAllAround( message.getProxy(), point );
	}

	public void sendToDimension( AppEngPacket message, int dimensionId )
	{
		this.ec.sendToDimension( message.getProxy(), dimensionId );
	}

	public final void sendToServer( AppEngPacket message )
	{
		this.ec.sendToServer( message.getProxy() );
	}
}
