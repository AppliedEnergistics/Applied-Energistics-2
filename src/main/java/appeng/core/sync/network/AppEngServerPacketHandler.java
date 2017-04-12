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


import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandlerBase;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.lang.reflect.InvocationTargetException;


public final class AppEngServerPacketHandler extends AppEngPacketHandlerBase implements IPacketHandler
{

	@Override
	public void onPacketData( final INetworkInfo manager, final FMLProxyPacket packet, final EntityPlayer player )
	{
		final ByteBuf stream = packet.payload();

		try
		{
			final int packetType = stream.readInt();
			final AppEngPacket pack = PacketTypes.getPacket( packetType ).parsePacket( stream );
			pack.serverPacketData( manager, pack, player );
		}
		catch( final InstantiationException e )
		{
			AELog.debug( e );
		}
		catch( final IllegalAccessException e )
		{
			AELog.debug( e );
		}
		catch( final IllegalArgumentException e )
		{
			AELog.debug( e );
		}
		catch( final InvocationTargetException e )
		{
			AELog.debug( e );
		}
	}
}
