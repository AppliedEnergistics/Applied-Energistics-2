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


import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandlerBase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;


public class NetworkHandler
{
	private static NetworkHandler instance;

	private final SimpleChannel sc;
	private final ResourceLocation myChannelName;

	private final IPacketHandler packetHandler;

	public NetworkHandler( final ResourceLocation channelName )
	{
		this.sc = NetworkRegistry.ChannelBuilder
				.named( this.myChannelName = channelName )
				.simpleChannel();

		this.packetHandler = this.createPacketHandler();

		for( AppEngPacketHandlerBase.PacketTypes packetType : AppEngPacketHandlerBase.PacketTypes.values() )
		{
			packetType.register(sc, packetType.ordinal(), this.packetHandler );
		}
	}

	private IPacketHandler createPacketHandler()
	{
		return FMLEnvironment.dist.isClient() ? createClientSide() : createServerSide();
	}

	public static void init( final ResourceLocation channelName )
	{
		instance = new NetworkHandler( channelName );
	}

	public static NetworkHandler instance()
	{
		return instance;
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

	public ResourceLocation getChannel()
	{
		return this.myChannelName;
	}

	public void reply( final AppEngPacket message, NetworkEvent.Context ctx )
	{
		this.sc.reply( message, ctx );
	}

	public void sendToAll( final AppEngPacket message )
	{
		this.sc.send( PacketDistributor.ALL.noArg(), message );
	}

	public void sendTo( final AppEngPacket message, final ServerPlayerEntity player )
	{
		this.sc.send( PacketDistributor.PLAYER.with(() -> player ), message );
	}

	public void sendToAllAround( final AppEngPacket message, final PacketDistributor.TargetPoint point )
	{
		this.sc.send( PacketDistributor.NEAR.with( () -> point ), message );
	}

	public void sendToDimension( final AppEngPacket message, final DimensionType dimensionType )
	{
		this.sc.send( PacketDistributor.DIMENSION.with( () -> dimensionType ), message );
	}

	public void sendToServer( final AppEngPacket message )
	{
		this.sc.sendToServer( message );
	}
}
