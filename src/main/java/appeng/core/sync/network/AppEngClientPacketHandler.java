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


import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.*;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandlerBase;
import net.minecraftforge.fml.network.NetworkEvent;


public class AppEngClientPacketHandler extends AppEngPacketHandlerBase
{
	private Minecraft client;

	public AppEngClientPacketHandler()
	{
		this.client = Minecraft.getInstance();
	}

	@Override
	public <MSG extends AppEngPacket> void onPacketData( MSG pack, Supplier<NetworkEvent.Context> contextSupplier )
	{
		NetworkEvent.Context ctx = contextSupplier.get();
		NetworkManager manager = ctx.getNetworkManager();

		pack.setCallParam( (packet) -> packet.clientPacketData( null, packet, this.client.player, ctx ) );

		//TODO find a better way to get an INetHandler than to instantiate one every time
		PacketThreadUtil.checkThreadAndEnqueue( pack, new DummyINetHandler(manager), this.client );
	}
}
