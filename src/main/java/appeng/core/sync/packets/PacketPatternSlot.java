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


import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;


public class PacketPatternSlot extends AppEngPacket
{

	final public IAEItemStack slotItem;

	final public IAEItemStack[] pattern = new IAEItemStack[9];

	final public boolean shift;

	// automatic.
	public PacketPatternSlot( ByteBuf stream ) throws IOException
	{

		this.shift = stream.readBoolean();

		this.slotItem = this.readItem( stream );

		for( int x = 0; x < 9; x++ )
			this.pattern[x] = this.readItem( stream );
	}

	public IAEItemStack readItem( ByteBuf stream ) throws IOException
	{
		boolean hasItem = stream.readBoolean();

		if( hasItem )
			return AEItemStack.loadItemStackFromPacket( stream );

		return null;
	}

	// api
	public PacketPatternSlot( IInventory pat, IAEItemStack slotItem, boolean shift ) throws IOException
	{

		this.slotItem = slotItem;
		this.shift = shift;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );

		data.writeBoolean( shift );

		this.writeItem( slotItem, data );
		for( int x = 0; x < 9; x++ )
		{
			this.pattern[x] = AEApi.instance().storage().createItemStack( pat.getStackInSlot( x ) );
			this.writeItem( this.pattern[x], data );
		}

		this.configureWrite( data );
	}

	private void writeItem( IAEItemStack slotItem, ByteBuf data ) throws IOException
	{
		if( slotItem == null )
			data.writeBoolean( false );
		else
		{
			data.writeBoolean( true );
			slotItem.writeToPacket( data );
		}
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		if( sender.openContainer instanceof ContainerPatternTerm )
		{
			ContainerPatternTerm patternTerminal = (ContainerPatternTerm) sender.openContainer;
			patternTerminal.craftOrGetItem( this );
		}
	}
}
