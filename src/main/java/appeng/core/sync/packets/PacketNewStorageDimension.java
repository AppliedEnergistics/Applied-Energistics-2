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

package appeng.core.sync.packets;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.core.AEConfig;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;


public class PacketNewStorageDimension extends AppEngPacket
{

	final int newDim;

	// automatic.
	public PacketNewStorageDimension( ByteBuf stream )
	{
		this.newDim = stream.readInt();
	}

	// api
	public PacketNewStorageDimension( int newDim )
	{
		this.newDim = newDim;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( newDim );

		this.configureWrite( data );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void clientPacketData( INetworkInfo network, AppEngPacket packet, EntityPlayer player )
	{
		try
		{
			DimensionManager.registerDimension( this.newDim, AEConfig.instance.storageProviderID );
		}
		catch( IllegalArgumentException iae )
		{
			// ok!
		}
	}
}
