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

package appeng.container.implementations;


import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCompressedNBT;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.InventoryAction;
import appeng.items.misc.ItemEncodedPattern;
import appeng.parts.misc.PartInterface;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.inv.WrapperInvSlot;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public final class ContainerInterfaceTerminal extends AEBaseContainer
{

	/**
	 * this stuff is all server side..
	 */

	private static long autoBase = Long.MIN_VALUE;
	private final Map<IInterfaceHost, InvTracker> diList = new HashMap<IInterfaceHost, InvTracker>();
	private final Map<Long, InvTracker> byId = new HashMap<Long, InvTracker>();
	private IGrid grid;
	private NBTTagCompound data = new NBTTagCompound();

	public ContainerInterfaceTerminal( final InventoryPlayer ip, final PartInterfaceTerminal anchor )
	{
		super( ip, anchor );

		if( Platform.isServer() )
		{
			this.grid = anchor.getActionableNode().getGrid();
		}

		this.bindPlayerInventory( ip, 0, 222 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isClient() )
		{
			return;
		}

		super.detectAndSendChanges();

		if( this.grid == null )
		{
			return;
		}

		int total = 0;
		boolean missing = false;

		final IActionHost host = this.getActionHost();
		if( host != null )
		{
			final IGridNode agn = host.getActionableNode();
			if( agn != null && agn.isActive() )
			{
				for( final IGridNode gn : this.grid.getMachines( TileInterface.class ) )
				{
					if( gn.isActive() )
					{
						final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
						if( ih.getInterfaceDuality().getConfigManager().getSetting( Settings.INTERFACE_TERMINAL ) == YesNo.NO )
						{
							continue;
						}

						final InvTracker t = this.diList.get( ih );

						if( t == null )
						{
							missing = true;
						}
						else
						{
							final DualityInterface dual = ih.getInterfaceDuality();
							if( !t.unlocalizedName.equals( dual.getTermName() ) )
							{
								missing = true;
							}
						}

						total++;
					}
				}

				for( final IGridNode gn : this.grid.getMachines( PartInterface.class ) )
				{
					if( gn.isActive() )
					{
						final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
						if( ih.getInterfaceDuality().getConfigManager().getSetting( Settings.INTERFACE_TERMINAL ) == YesNo.NO )
						{
							continue;
						}

						final InvTracker t = this.diList.get( ih );

						if( t == null )
						{
							missing = true;
						}
						else
						{
							final DualityInterface dual = ih.getInterfaceDuality();
							if( !t.unlocalizedName.equals( dual.getTermName() ) )
							{
								missing = true;
							}
						}

						total++;
					}
				}
			}
		}

		if( total != this.diList.size() || missing )
		{
			this.regenList( this.data );
		}
		else
		{
			for( final Entry<IInterfaceHost, InvTracker> en : this.diList.entrySet() )
			{
				final InvTracker inv = en.getValue();
				for( int x = 0; x < inv.server.getSizeInventory(); x++ )
				{
					if( this.isDifferent( inv.server.getStackInSlot( x ), inv.client.getStackInSlot( x ) ) )
					{
						this.addItems( this.data, inv, x, 1 );
					}
				}
			}
		}

		if( !this.data.hasNoTags() )
		{
			try
			{
				NetworkHandler.instance.sendTo( new PacketCompressedNBT( this.data ), (EntityPlayerMP) this.getPlayerInv().player );
			}
			catch( final IOException e )
			{
				// :P
			}

			this.data = new NBTTagCompound();
		}
	}

	@Override
	public void doAction( final EntityPlayerMP player, final InventoryAction action, final int slot, final long id )
	{
		final InvTracker inv = this.byId.get( id );
		if( inv != null )
		{
			final ItemStack is = inv.server.getStackInSlot( slot );
			final boolean hasItemInHand = player.inventory.getItemStack() != null;

			final InventoryAdaptor playerHand = new AdaptorPlayerHand( player );

			final WrapperInvSlot slotInv = new PatternInvSlot( inv.server );

			final IInventory theSlot = slotInv.getWrapper( slot );
			final InventoryAdaptor interfaceSlot = new AdaptorIInventory( theSlot );

			switch( action )
			{
				case PICKUP_OR_SET_DOWN:

					if( hasItemInHand )
					{
						ItemStack inSlot = theSlot.getStackInSlot( 0 );
						if( inSlot == null )
						{
							player.inventory.setItemStack( interfaceSlot.addItems( player.inventory.getItemStack() ) );
						}
						else
						{
							inSlot = inSlot.copy();
							final ItemStack inHand = player.inventory.getItemStack().copy();

							theSlot.setInventorySlotContents( 0, null );
							player.inventory.setItemStack( null );

							player.inventory.setItemStack( interfaceSlot.addItems( inHand.copy() ) );

							if( player.inventory.getItemStack() == null )
							{
								player.inventory.setItemStack( inSlot );
							}
							else
							{
								player.inventory.setItemStack( inHand );
								theSlot.setInventorySlotContents( 0, inSlot );
							}
						}
					}
					else
					{
						final IInventory mySlot = slotInv.getWrapper( slot );
						mySlot.setInventorySlotContents( 0, playerHand.addItems( mySlot.getStackInSlot( 0 ) ) );
					}

					break;
				case SPLIT_OR_PLACE_SINGLE:

					if( hasItemInHand )
					{
						ItemStack extra = playerHand.removeItems( 1, null, null );
						if( extra != null )
						{
							extra = interfaceSlot.addItems( extra );
						}
						if( extra != null )
						{
							playerHand.addItems( extra );
						}
					}
					else if( is != null )
					{
						ItemStack extra = interfaceSlot.removeItems( ( is.stackSize + 1 ) / 2, null, null );
						if( extra != null )
						{
							extra = playerHand.addItems( extra );
						}
						if( extra != null )
						{
							interfaceSlot.addItems( extra );
						}
					}

					break;
				case SHIFT_CLICK:

					final IInventory mySlot = slotInv.getWrapper( slot );
					final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
					mySlot.setInventorySlotContents( 0, playerInv.addItems( mySlot.getStackInSlot( 0 ) ) );

					break;
				case MOVE_REGION:

					final InventoryAdaptor playerInvAd = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
					for( int x = 0; x < inv.server.getSizeInventory(); x++ )
					{
						inv.server.setInventorySlotContents( x, playerInvAd.addItems( inv.server.getStackInSlot( x ) ) );
					}

					break;
				case CREATIVE_DUPLICATE:

					if( player.capabilities.isCreativeMode && !hasItemInHand )
					{
						player.inventory.setItemStack( is == null ? null : is.copy() );
					}

					break;
				default:
					return;
			}

			this.updateHeld( player );
		}
	}

	private void regenList( final NBTTagCompound data )
	{
		this.byId.clear();
		this.diList.clear();

		final IActionHost host = this.getActionHost();
		if( host != null )
		{
			final IGridNode agn = host.getActionableNode();
			if( agn != null && agn.isActive() )
			{
				for( final IGridNode gn : this.grid.getMachines( TileInterface.class ) )
				{
					final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
					final DualityInterface dual = ih.getInterfaceDuality();
					if( gn.isActive() && dual.getConfigManager().getSetting( Settings.INTERFACE_TERMINAL ) == YesNo.YES )
					{
						this.diList.put( ih, new InvTracker( dual, dual.getPatterns(), dual.getTermName() ) );
					}
				}

				for( final IGridNode gn : this.grid.getMachines( PartInterface.class ) )
				{
					final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
					final DualityInterface dual = ih.getInterfaceDuality();
					if( gn.isActive() && dual.getConfigManager().getSetting( Settings.INTERFACE_TERMINAL ) == YesNo.YES )
					{
						this.diList.put( ih, new InvTracker( dual, dual.getPatterns(), dual.getTermName() ) );
					}
				}
			}
		}

		data.setBoolean( "clear", true );

		for( final Entry<IInterfaceHost, InvTracker> en : this.diList.entrySet() )
		{
			final InvTracker inv = en.getValue();
			this.byId.put( inv.which, inv );
			this.addItems( data, inv, 0, inv.server.getSizeInventory() );
		}
	}

	private boolean isDifferent( final ItemStack a, final ItemStack b )
	{
		if( a == null && b == null )
		{
			return false;
		}

		if( a == null || b == null )
		{
			return true;
		}

		return !ItemStack.areItemStacksEqual( a, b );
	}

	private void addItems( final NBTTagCompound data, final InvTracker inv, final int offset, final int length )
	{
		final String name = '=' + Long.toString( inv.which, Character.MAX_RADIX );
		final NBTTagCompound tag = data.getCompoundTag( name );

		if( tag.hasNoTags() )
		{
			tag.setLong( "sortBy", inv.sortBy );
			tag.setString( "un", inv.unlocalizedName );
		}

		for( int x = 0; x < length; x++ )
		{
			final NBTTagCompound itemNBT = new NBTTagCompound();

			final ItemStack is = inv.server.getStackInSlot( x + offset );

			// "update" client side.
			inv.client.setInventorySlotContents( x + offset, is == null ? null : is.copy() );

			if( is != null )
			{
				is.writeToNBT( itemNBT );
			}

			tag.setTag( Integer.toString( x + offset ), itemNBT );
		}

		data.setTag( name, tag );
	}

	private static class InvTracker
	{

		private final long sortBy;
		private final long which = autoBase++;
		private final String unlocalizedName;
		private final IInventory client;
		private final IInventory server;

		public InvTracker( final DualityInterface dual, final IInventory patterns, final String unlocalizedName )
		{
			this.server = patterns;
			this.client = new AppEngInternalInventory( null, this.server.getSizeInventory() );
			this.unlocalizedName = unlocalizedName;
			this.sortBy = dual.getSortValue();
		}
	}


	private static class PatternInvSlot extends WrapperInvSlot
	{

		public PatternInvSlot( final IInventory inv )
		{
			super( inv );
		}

		@Override
		public boolean isItemValid( final ItemStack itemstack )
		{
			return itemstack != null && itemstack.getItem() instanceof ItemEncodedPattern;
		}
	}
}
