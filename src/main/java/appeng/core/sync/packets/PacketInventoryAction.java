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
import net.minecraft.tileentity.TileEntity;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.ClientHelper;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class PacketInventoryAction extends AppEngPacket
{

	public final InventoryAction action;
	public final int slot;
	public final long id;
	public final IAEItemStack slotItem;

	// automatic.
	public PacketInventoryAction( ByteBuf stream ) throws IOException
	{
		this.action = InventoryAction.values()[stream.readInt()];
		this.slot = stream.readInt();
		this.id = stream.readLong();
		boolean hasItem = stream.readBoolean();
		if( hasItem )
			this.slotItem = AEItemStack.loadItemStackFromPacket( stream );
		else
			this.slotItem = null;
	}

	// api
	public PacketInventoryAction( InventoryAction action, int slot, IAEItemStack slotItem ) throws IOException
	{

		if( Platform.isClient() )
			throw new RuntimeException( "invalid packet, client cannot post inv actions with stacks." );

		this.action = action;
		this.slot = slot;
		this.id = 0;
		this.slotItem = slotItem;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );
		data.writeLong( this.id );

		if( slotItem == null )
			data.writeBoolean( false );
		else
		{
			data.writeBoolean( true );
			slotItem.writeToPacket( data );
		}

		this.configureWrite( data );
	}

	// api
	public PacketInventoryAction( InventoryAction action, int slot, long id )
	{
		this.action = action;
		this.slot = slot;
		this.id = id;
		this.slotItem = null;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );
		data.writeLong( id );
		data.writeBoolean( false );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		if( sender.openContainer instanceof AEBaseContainer )
		{
			AEBaseContainer baseContainer = (AEBaseContainer) sender.openContainer;
			if( this.action == InventoryAction.AUTO_CRAFT )
			{
				ContainerOpenContext context = baseContainer.openContext;
				if( context != null )
				{
					TileEntity te = context.getTile();
					Platform.openGUI( sender, te, baseContainer.openContext.side, GuiBridge.GUI_CRAFTING_AMOUNT );

					if( sender.openContainer instanceof ContainerCraftAmount )
					{
						ContainerCraftAmount cca = (ContainerCraftAmount) sender.openContainer;

						if( baseContainer.getTargetStack() != null )
						{
							cca.craftingItem.putStack( baseContainer.getTargetStack().getItemStack() );
							cca.whatToMake = baseContainer.getTargetStack();
						}

						cca.detectAndSendChanges();
					}
				}
			}
			else
			{
				baseContainer.doAction( sender, this.action, this.slot, this.id );
			}
		}
	}

	@Override
	public void clientPacketData( INetworkInfo network, AppEngPacket packet, EntityPlayer player )
	{
		if( this.action == InventoryAction.UPDATE_HAND )
		{
			if( this.slotItem == null )
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( null );
			else
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( this.slotItem.getItemStack() );
		}
	}
}
