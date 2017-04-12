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


import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.parts.PartPlacement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;


public class PacketPartPlacement extends AppEngPacket
{

	private int x;
	private int y;
	private int z;
	private int face;
	private float eyeHeight;

	// automatic.
	public PacketPartPlacement( final ByteBuf stream )
	{
		this.x = stream.readInt();
		this.y = stream.readInt();
		this.z = stream.readInt();
		this.face = stream.readByte();
		this.eyeHeight = stream.readFloat();
	}

	// api
	public PacketPartPlacement( final int x, final int y, final int z, final int face, final float eyeHeight )
	{
		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( x );
		data.writeInt( y );
		data.writeInt( z );
		data.writeByte( face );
		data.writeFloat( eyeHeight );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player )
	{
		final EntityPlayerMP sender = (EntityPlayerMP) player;
		CommonHelper.proxy.updateRenderMode( sender );
		PartPlacement.setEyeHeight( this.eyeHeight );
		PartPlacement.place( sender.getHeldItem(), this.x, this.y, this.z, this.face, sender, sender.worldObj, PartPlacement.PlaceType.INTERACT_FIRST_PASS, 0 );
		CommonHelper.proxy.updateRenderMode( null );
	}
}
