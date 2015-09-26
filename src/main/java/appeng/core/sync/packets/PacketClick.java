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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.api.AEApi;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IItems;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.helpers.Reflected;
import appeng.items.tools.ToolNetworkTool;
import appeng.items.tools.powered.ToolColorApplicator;


public class PacketClick implements AppEngPacket, AppEngPacketHandler<PacketClick, AppEngPacket>
{

	private int x;
	private int y;
	private int z;
	private int side;
	private float hitX;
	private float hitY;
	private float hitZ;

	@Reflected
	public PacketClick()
	{
		// automatic.
	}

	// api
	public PacketClick( final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.side = side;
		this.hitX = hitX;
		this.hitY = hitY;
		this.hitZ = hitZ;
	}

	@Override
	public AppEngPacket onMessage( final PacketClick message, final MessageContext ctx )
	{
		final EntityPlayer player = ctx.getServerHandler().playerEntity;
		final ItemStack is = player.inventory.getCurrentItem();
		final IItems items = AEApi.instance().definitions().items();
		final IComparableDefinition maybeMemoryCard = items.memoryCard();
		final IComparableDefinition maybeColorApplicator = items.colorApplicator();

		if( is != null )
		{
			if( is.getItem() instanceof ToolNetworkTool )
			{
				final ToolNetworkTool tnt = (ToolNetworkTool) is.getItem();
				tnt.serverSideToolLogic( is, player, player.worldObj, message.x, message.y, message.z, message.side, message.hitX, message.hitY,
						message.hitZ );
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

		return null;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.side = buf.readInt();
		this.hitX = buf.readFloat();
		this.hitY = buf.readFloat();
		this.hitZ = buf.readFloat();
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeInt( this.x );
		buf.writeInt( this.y );
		buf.writeInt( this.z );
		buf.writeInt( this.side );
		buf.writeFloat( this.hitX );
		buf.writeFloat( this.hitY );
		buf.writeFloat( this.hitZ );
	}
}
