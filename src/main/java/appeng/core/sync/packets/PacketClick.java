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


import appeng.api.AEApi;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IItems;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.ToolNetworkTool;
import appeng.items.tools.powered.ToolColorApplicator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;


public class PacketClick extends AppEngPacket
{

	private final int x;
	private final int y;
	private final int z;
	private final int side;
	private final float hitX;
	private final float hitY;
	private final float hitZ;

	// automatic.
	public PacketClick( final ByteBuf stream )
	{
		this.x = stream.readInt();
		this.y = stream.readInt();
		this.z = stream.readInt();
		this.side = stream.readInt();
		this.hitX = stream.readFloat();
		this.hitY = stream.readFloat();
		this.hitZ = stream.readFloat();
	}

	// api
	public PacketClick( final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( this.x = x );
		data.writeInt( this.y = y );
		data.writeInt( this.z = z );
		data.writeInt( this.side = side );
		data.writeFloat( this.hitX = hitX );
		data.writeFloat( this.hitY = hitY );
		data.writeFloat( this.hitZ = hitZ );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player )
	{
		final ItemStack is = player.inventory.getCurrentItem();
		final IItems items = AEApi.instance().definitions().items();
		final IComparableDefinition maybeMemoryCard = items.memoryCard();
		final IComparableDefinition maybeColorApplicator = items.colorApplicator();

		if( is != null )
		{
			if( is.getItem() instanceof ToolNetworkTool )
			{
				final ToolNetworkTool tnt = (ToolNetworkTool) is.getItem();
				tnt.serverSideToolLogic( is, player, player.worldObj, this.x, this.y, this.z, this.side, this.hitX, this.hitY, this.hitZ );
			}

			else if( maybeMemoryCard.isSameAs( is ) )
			{
				final IMemoryCard mem = (IMemoryCard) is.getItem();
				mem.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );
				is.setTagCompound( null );
			}

			else if( maybeColorApplicator.isSameAs( is ) )
			{
				final ToolColorApplicator mem = (ToolColorApplicator) is.getItem();
				mem.cycleColors( is, mem.getColor( is ), 1 );
			}
		}
	}
}
