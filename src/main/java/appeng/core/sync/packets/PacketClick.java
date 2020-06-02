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


import io.netty.buffer.Unpooled;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IItems;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.core.Api;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;


public class PacketClick extends AppEngPacket
{

	private final int x;
	private final int y;
	private final int z;
	private Direction side;
	private final float hitX;
	private final float hitY;
	private final float hitZ;
	private Hand hand;
	private final boolean leftClick;

	public PacketClick( final PacketBuffer stream )
	{
		this.x = stream.readInt();
		this.y = stream.readInt();
		this.z = stream.readInt();
		byte side = stream.readByte();
		if( side != -1 )
		{
			this.side = Direction.values()[side];
		}
		else
		{
			this.side = null;
		}
		this.hitX = stream.readFloat();
		this.hitY = stream.readFloat();
		this.hitZ = stream.readFloat();
		this.hand = Hand.values()[stream.readByte()];
		this.leftClick = stream.readBoolean();
	}

	// api
	public PacketClick( final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ, final Hand hand )
	{
		this( pos, side, hitX, hitY, hitZ, hand, false );
	}

	public PacketClick( final BlockPos pos, final Direction side, final float hitX, final float hitY, final float hitZ, final Hand hand, boolean leftClick )
	{

		final PacketBuffer data = new PacketBuffer( Unpooled.buffer() );

		data.writeInt( this.getPacketID() );
		data.writeInt( this.x = pos.getX() );
		data.writeInt( this.y = pos.getY() );
		data.writeInt( this.z = pos.getZ() );
		if( side == null )
		{
			data.writeByte( -1 );
		}
		else
		{
			data.writeByte( side.ordinal() );
		}
		data.writeFloat( this.hitX = hitX );
		data.writeFloat( this.hitY = hitY );
		data.writeFloat( this.hitZ = hitZ );
		data.writeByte( hand.ordinal() );
		data.writeBoolean( this.leftClick = leftClick );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final PlayerEntity player )
	{
		final ItemStack is = player.inventory.getCurrentItem();
		final IItems items = Api.INSTANCE.definitions().items();
		final IComparableDefinition maybeMemoryCard = items.memoryCard();
		final IComparableDefinition maybeColorApplicator = items.colorApplicator();
		final BlockPos pos = new BlockPos( this.x, this.y, this.z );
		if( this.leftClick )
		{
			final Block block = player.world.getBlockState( pos ).getBlock();
//	FIXME		 if( block instanceof BlockCableBus )
//	FIXME		 {
//	FIXME		 	( (BlockCableBus) block ).onBlockClickPacket( player.world, pos, player, this.hand, new Vec3d( this.hitX, this.hitY, this.hitZ ) );
//	FIXME		 }
		}
		else
		{
			if( !is.isEmpty() )
			{
				// FIXME if( is.getItem() instanceof ToolNetworkTool )
				// FIXME {
				// FIXME 	final ToolNetworkTool tnt = (ToolNetworkTool) is.getItem();
				// FIXME 	tnt.serverSideToolLogic( is, player, this.hand, player.world, pos, this.side, this.hitX, this.hitY,
				// FIXME 			this.hitZ );
				// FIXME }

				/* FIXME else */ if( maybeMemoryCard.isSameAs( is ) )
				{
					final IMemoryCard mem = (IMemoryCard) is.getItem();
					mem.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );
					is.setTag( null );
				}

				// FIXME else if( maybeColorApplicator.isSameAs( is ) )
				// FIXME {
				// FIXME 	final ToolColorApplicator mem = (ToolColorApplicator) is.getItem();
				// FIXME 	mem.cycleColors( is, mem.getColor( is ), 1 );
				// FIXME }
			}
		}
	}
}
