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

import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.core.AEConfig;
import appeng.core.sync.AppEngPacket;


public class PacketNewStorageDimension extends AppEngPacket<PacketNewStorageDimension>
{

	private int newDim;

	// automatic.
	public PacketNewStorageDimension()
	{
	}

	// api
	public PacketNewStorageDimension( final int newDim )
	{
		this.newDim = newDim;
	}

	@Override
	public PacketNewStorageDimension onMessage( PacketNewStorageDimension message, MessageContext ctx )
	{
		try
		{
			DimensionManager.registerDimension( message.newDim, AEConfig.instance.storageProviderID );
		}
		catch( final IllegalArgumentException iae )
		{
			// ok!
		}
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.newDim = buf.readInt();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeInt( newDim );
	}
}
