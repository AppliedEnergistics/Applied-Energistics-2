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


import appeng.core.AEConfig;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;


public class PacketNewStorageDimension extends AppEngPacket
{

	private final int newDim;

	// automatic.
	public PacketNewStorageDimension( final ByteBuf stream )
	{
		this.newDim = stream.readInt();
	}

	// api
	public PacketNewStorageDimension( final int newDim )
	{
		this.newDim = newDim;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( newDim );

		this.configureWrite( data );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void clientPacketData( final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player )
	{
		try
		{
			DimensionManager.registerDimension( this.newDim, AEConfig.instance.storageProviderID );
		}
		catch( final IllegalArgumentException iae )
		{
			// ok!
		}
	}
}
