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


import appeng.core.sync.AppEngPacket;
import appeng.core.worlddata.WorldData;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;


public class NetworkHandler
{

	public static NetworkHandler instance;

	private final FMLEventChannel ec;
	private final String myChannelName;

	private final IPacketHandler clientHandler;
	private final IPacketHandler serveHandler;

	public NetworkHandler( final String channelName )
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
		catch( final Throwable t )
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
		catch( final Throwable t )
		{
			return null;
		}
	}

	@SubscribeEvent
	public void newConnection( final ServerConnectionFromClientEvent ev )
	{
		WorldData.instance().dimensionData().sendToPlayer( ev.manager );
	}

	@SubscribeEvent
	public void newConnection( final PlayerLoggedInEvent loginEvent )
	{
		if( loginEvent.player instanceof EntityPlayerMP )
		{
			WorldData.instance().dimensionData().sendToPlayer( null );
		}
	}

	@SubscribeEvent
	public void serverPacket( final ServerCustomPacketEvent ev )
	{
		final NetHandlerPlayServer srv = (NetHandlerPlayServer) ev.packet.handler();
		if( this.serveHandler != null )
		{
			this.serveHandler.onPacketData( null, ev.packet, srv.playerEntity );
		}
	}

	@SubscribeEvent
	public void clientPacket( final ClientCustomPacketEvent ev )
	{
		if( this.clientHandler != null )
		{
			this.clientHandler.onPacketData( null, ev.packet, null );
		}
	}

	public String getChannel()
	{
		return this.myChannelName;
	}

	public void sendToAll( final AppEngPacket message )
	{
		this.ec.sendToAll( message.getProxy() );
	}

	public void sendTo( final AppEngPacket message, final EntityPlayerMP player )
	{
		this.ec.sendTo( message.getProxy(), player );
	}

	public void sendToAllAround( final AppEngPacket message, final NetworkRegistry.TargetPoint point )
	{
		this.ec.sendToAllAround( message.getProxy(), point );
	}

	public void sendToDimension( final AppEngPacket message, final int dimensionId )
	{
		this.ec.sendToDimension( message.getProxy(), dimensionId );
	}

	public void sendToServer( final AppEngPacket message )
	{
		this.ec.sendToServer( message.getProxy() );
	}
}
