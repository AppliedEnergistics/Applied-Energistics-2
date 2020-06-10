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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public final class AppEngServerPacketHandler extends AppEngPacketHandlerBase
{

	@Override
	public <MSG extends AppEngPacket> void onPacketData( MSG pack, Supplier<NetworkEvent.Context> contextSupplier )
	{
		NetworkEvent.Context ctx = contextSupplier.get();
		NetworkManager manager = ctx.getNetworkManager();
		ServerPlayerEntity serverPlayerEntity = ctx.getSender();

		pack.setCallParam( (packet) -> packet.serverPacketData( null, packet, serverPlayerEntity, ctx) );

		//TODO find a better way to get an INetHandler than to instantiate one every time
		PacketThreadUtil.checkThreadAndEnqueue( pack, new DummyINetHandler(manager), serverPlayerEntity.getServerWorld() );
	}
}
