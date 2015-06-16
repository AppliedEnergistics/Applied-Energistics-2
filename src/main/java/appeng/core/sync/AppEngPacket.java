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

package appeng.core.sync;


import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;


public abstract class AppEngPacket implements Packet
{

	AppEngPacketHandlerBase.PacketTypes id;
	private PacketBuffer p;

	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		throw new UnsupportedOperationException( "This packet ( " + this.getPacketID() + " does not implement a server side handler." );
	}

	public final int getPacketID()
	{
		return AppEngPacketHandlerBase.PacketTypes.getID( this.getClass() ).ordinal();
	}

	public void clientPacketData( INetworkInfo network, AppEngPacket packet, EntityPlayer player )
	{
		throw new UnsupportedOperationException( "This packet ( " + this.getPacketID() + " does not implement a client side handler." );
	}

	protected void configureWrite( ByteBuf data )
	{
		data.capacity( data.readableBytes() );
		this.p = new PacketBuffer(data);
	}

	public FMLProxyPacket getProxy()
	{
		if( this.p.array().length > 2 * 1024 * 1024 ) // 2k walking room :)
		{
			throw new IllegalArgumentException( "Sorry AE2 made a " + this.p.array().length + " byte packet by accident!" );
		}

		FMLProxyPacket pp = new FMLProxyPacket( this.p, NetworkHandler.instance.getChannel() );

		if( AEConfig.instance.isFeatureEnabled( AEFeature.PacketLogging ) )
		{
			AELog.info( this.getClass().getName() + " : " + pp.payload().readableBytes() );
		}

		return pp;
	}
	
	@Override
    public void readPacketData(PacketBuffer buf) throws IOException
    {
    	throw new RuntimeException( "Not Implemented" );
    }

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException
    {
    	throw new RuntimeException( "Not Implemented" );
    }

	PacketCallState caller;
	
	public void setCallParam( PacketCallState call ){caller = call;}
	
	@Override
	public void processPacket(INetHandler handler)
    {
		caller.call(this);
    }

}
