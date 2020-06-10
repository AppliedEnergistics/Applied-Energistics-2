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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import appeng.core.AppEng;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.parts.PartPlacement;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent;


public class PacketPartPlacement extends AppEngPacket
{

	private int x;
	private int y;
	private int z;
	private int face;
	private float eyeHeight;
	private Hand hand;

	// automatic.
	public PacketPartPlacement( final ByteBuf stream )
	{
		this.x = stream.readInt();
		this.y = stream.readInt();
		this.z = stream.readInt();
		this.face = stream.readByte();
		this.eyeHeight = stream.readFloat();
		this.hand = Hand.values()[stream.readByte()];
	}

	// api
	public PacketPartPlacement( final BlockPos pos, final Direction face, final float eyeHeight, final Hand hand )
	{
		final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

		data.writeInt( pos.getX() );
		data.writeInt( pos.getY() );
		data.writeInt( pos.getZ() );
		data.writeByte( face.ordinal() );
		data.writeFloat( eyeHeight );
		data.writeByte( hand.ordinal() );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final AppEngPacket packet, final PlayerEntity player, NetworkEvent.Context ctx )
	{
		AppEng.proxy.updateRenderMode( player );
		PartPlacement.setEyeHeight( this.eyeHeight );

		// sender.getHeldItem( this.hand ), new BlockPos( this.x, this.y, this.z ), EnumFacing.VALUES[this.face], sender, this.hand,
		//				sender.world,
		Vec3d hitvec = player.getPositionVec().subtract( new Vec3d( this.x, this.y, this.z ) );

		PartPlacement.place(new ItemUseContext( player, this.hand,
					new BlockRayTraceResult(hitvec, Direction.values()[this.face], new BlockPos( this.x, this.y, this.z ), false)),
				PartPlacement.PlaceType.INTERACT_FIRST_PASS, 0 );
		AppEng.proxy.updateRenderMode( null );
	}
}
