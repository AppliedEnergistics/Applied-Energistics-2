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
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.ClientHelper;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.core.sync.GuiBridge;
import appeng.helpers.InventoryAction;
import appeng.helpers.Reflected;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class PacketInventoryAction implements AppEngPacket, AppEngPacketHandler<PacketInventoryAction, AppEngPacket>
{

	private InventoryAction action;
	private int slot;
	private long id;
	private IAEItemStack slotItem;

	@Reflected
	public PacketInventoryAction()
	{
		// automatic.
	}

	// api
	public PacketInventoryAction( final InventoryAction action, final int slot, final IAEItemStack slotItem )
	{
		if( Platform.isClient() )
		{
			throw new IllegalStateException( "invalid packet, client cannot post inv actions with stacks." );
		}

		this.action = action;
		this.slot = slot;
		this.id = 0;
		this.slotItem = slotItem;
	}

	// api
	public PacketInventoryAction( final InventoryAction action, final int slot, final long id )
	{
		this.action = action;
		this.slot = slot;
		this.id = id;
		this.slotItem = null;
	}

	@Override
	public AppEngPacket onMessage( final PacketInventoryAction message, final MessageContext ctx )
	{
		if( ctx.side == Side.CLIENT )
		{
			return this.onMessageClientSide( message );
		}
		else
		{
			return this.onMessageServerSide( message, ctx );
		}
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.action = InventoryAction.values()[buf.readInt()];
		this.slot = buf.readInt();
		this.id = buf.readLong();
		final boolean hasItem = buf.readBoolean();
		if( hasItem )
		{
			try
			{
				this.slotItem = AEItemStack.loadItemStackFromPacket( buf );
			}
			catch( final IOException e )
			{
			}
		}
		else
		{
			this.slotItem = null;
		}
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeInt( this.action.ordinal() );
		buf.writeInt( this.slot );
		buf.writeLong( this.id );

		if( this.slotItem == null )
		{
			buf.writeBoolean( false );
		}
		else
		{
			buf.writeBoolean( true );
			try
			{
				this.slotItem.writeToPacket( buf );
			}
			catch( final IOException e )
			{
				AELog.error( e );
			}
		}
	}

	private AppEngPacket onMessageClientSide( final PacketInventoryAction message )
	{
		if( message.action == InventoryAction.UPDATE_HAND )
		{
			if( message.slotItem == null )
			{
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( null );
			}
			else
			{
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( message.slotItem.getItemStack() );
			}
		}

		return null;
	}

	private AppEngPacket onMessageServerSide( final PacketInventoryAction message, final MessageContext ctx )
	{
		final EntityPlayerMP sender = (EntityPlayerMP) ctx.getServerHandler().playerEntity;

		if( sender.openContainer instanceof AEBaseContainer )
		{
			final AEBaseContainer baseContainer = (AEBaseContainer) sender.openContainer;
			if( message.action == InventoryAction.AUTO_CRAFT )
			{
				final ContainerOpenContext context = baseContainer.getOpenContext();
				if( context != null )
				{
					final TileEntity te = context.getTile();
					Platform.openGUI( sender, te, baseContainer.getOpenContext().getSide(), GuiBridge.GUI_CRAFTING_AMOUNT );

					if( sender.openContainer instanceof ContainerCraftAmount )
					{
						final ContainerCraftAmount cca = (ContainerCraftAmount) sender.openContainer;

						if( baseContainer.getTargetStack() != null )
						{
							cca.getCraftingItem().putStack( baseContainer.getTargetStack().getItemStack() );
							cca.setItemToCraft( baseContainer.getTargetStack() );
						}

						cca.detectAndSendChanges();
					}
				}
			}
			else
			{
				baseContainer.doAction( sender, message.action, message.slot, message.id );
			}
		}

		return null;
	}
}
