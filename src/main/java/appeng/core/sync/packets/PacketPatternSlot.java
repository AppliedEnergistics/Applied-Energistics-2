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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.helpers.Reflected;
import appeng.util.item.AEItemStack;


public class PacketPatternSlot implements AppEngPacket, AppEngPacketHandler<PacketPatternSlot, AppEngPacket>
{

	private IInventory pat;
	private IAEItemStack slotItem;
	private final IAEItemStack[] pattern = new IAEItemStack[9];
	private boolean shift;

	@Reflected
	public PacketPatternSlot()
	{
		// automatic.
	}

	private IAEItemStack readItem( final ByteBuf stream )
	{
		final boolean hasItem = stream.readBoolean();

		if( hasItem )
		{
			try
			{
				return AEItemStack.loadItemStackFromPacket( stream );
			}
			catch( final IOException e )
			{
				return null;
			}
		}

		return null;
	}

	// api
	public PacketPatternSlot( final IInventory pat, final IAEItemStack slotItem, final boolean shift )
	{
		this.pat = pat;
		this.slotItem = slotItem;
		this.shift = shift;
	}

	@Override
	public AppEngPacket onMessage( final PacketPatternSlot message, final MessageContext ctx )
	{
		final EntityPlayerMP sender = (EntityPlayerMP) ctx.getServerHandler().playerEntity;

		if( sender.openContainer instanceof ContainerPatternTerm )
		{
			final ContainerPatternTerm patternTerminal = (ContainerPatternTerm) sender.openContainer;
			patternTerminal.craftOrGetItem( message );
		}

		return null;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.shift = buf.readBoolean();
		this.slotItem = this.readItem( buf );

		for( int x = 0; x < 9; x++ )
		{
			this.pattern[x] = this.readItem( buf );
		}
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeBoolean( this.shift );

		this.writeItem( this.slotItem, buf );
		for( int x = 0; x < 9; x++ )
		{
			this.pattern[x] = AEApi.instance().storage().createItemStack( this.pat.getStackInSlot( x ) );
			this.writeItem( this.pattern[x], buf );
		}
	}

	public IInventory getPat()
	{
		return this.pat;
	}

	public IAEItemStack getSlotItem()
	{
		return this.slotItem;
	}

	public IAEItemStack getPatternSlot( int i )
	{
		return this.pattern[i];
	}

	public boolean isShift()
	{
		return this.shift;
	}

	private void writeItem( final IAEItemStack slotItem, final ByteBuf data )
	{
		if( slotItem == null )
		{
			data.writeBoolean( false );
		}
		else
		{
			data.writeBoolean( true );
			try
			{
				slotItem.writeToPacket( data );
			}
			catch( final IOException e )
			{
			}
		}
	}
}
