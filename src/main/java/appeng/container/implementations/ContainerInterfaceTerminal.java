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


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

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
import appeng.parts.reporting.PartMonitor;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.inv.WrapperInvSlot;


public class ContainerInterfaceTerminal extends AEBaseContainer
{

	/**
	 * this stuff is all server side..
	 */

	static private long autoBase = Long.MIN_VALUE;
	final Map<IInterfaceHost, InvTracker> diList = new HashMap<IInterfaceHost, InvTracker>();
	final Map<Long, InvTracker> byId = new HashMap<Long, InvTracker>();
	IGrid g;
	NBTTagCompound data = new NBTTagCompound();

	public ContainerInterfaceTerminal( InventoryPlayer ip, PartMonitor anchor )
	{
		super( ip, anchor );

		if( Platform.isServer() )
			this.g = anchor.getActionableNode().getGrid();

		this.bindPlayerInventory( ip, 0, 222 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isClient() )
			return;

		super.detectAndSendChanges();

		if( this.g == null )
			return;

		int total = 0;
		boolean missing = false;

		IActionHost host = this.getActionHost();
		if( host != null )
		{
			IGridNode agn = host.getActionableNode();
			if( agn != null && agn.isActive() )
			{
				for( IGridNode gn : this.g.getMachines( TileInterface.class ) )
				{
					if( gn.isActive() )
					{
						IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
						if( ih.getInterfaceDuality().getConfigManager().getSetting( Settings.INTERFACE_TERMINAL ) == YesNo.NO )
							continue;

						InvTracker t = this.diList.get( ih );

						if( t == null )
							missing = true;
						else
						{
							DualityInterface dual = ih.getInterfaceDuality();
							if( !t.unlocalizedName.equals( dual.getTermName() ) )
								missing = true;
						}

						total++;
					}
				}

				for( IGridNode gn : this.g.getMachines( PartInterface.class ) )
				{
					if( gn.isActive() )
					{
						IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
						if( ih.getInterfaceDuality().getConfigManager().getSetting( Settings.INTERFACE_TERMINAL ) == YesNo.NO )
							continue;

						InvTracker t = this.diList.get( ih );

						if( t == null )
							missing = true;
						else
						{
							DualityInterface dual = ih.getInterfaceDuality();
							if( !t.unlocalizedName.equals( dual.getTermName() ) )
								missing = true;
						}

						total++;
					}
				}
			}
		}

		if( total != this.diList.size() || missing )
			this.regenList( this.data );
		else
		{
			for( Entry<IInterfaceHost, InvTracker> en : this.diList.entrySet() )
			{
				InvTracker inv = en.getValue();
				for( int x = 0; x < inv.server.getSizeInventory(); x++ )
				{
					if( this.isDifferent( inv.server.getStackInSlot( x ), inv.client.getStackInSlot( x ) ) )
						this.addItems( this.data, inv, x, 1 );
				}
			}
		}

		if( !this.data.hasNoTags() )
		{
			try
			{
				NetworkHandler.instance.sendTo( new PacketCompressedNBT( this.data ), (EntityPlayerMP) this.getPlayerInv().player );
			}
			catch( IOException e )
			{
				// :P
			}

			this.data = new NBTTagCompound();
		}
	}

	@Override
	public void doAction( EntityPlayerMP player, InventoryAction action, int slot, long id )
	{
		InvTracker inv = this.byId.get( id );
		if( inv != null )
		{
			ItemStack is = inv.server.getStackInSlot( slot );
			boolean hasItemInHand = player.inventory.getItemStack() != null;

			InventoryAdaptor playerHand = new AdaptorPlayerHand( player );

			WrapperInvSlot slotInv = new PatternInvSlot( inv.server );

			IInventory theSlot = slotInv.getWrapper( slot );
			InventoryAdaptor interfaceSlot = new AdaptorIInventory( theSlot );

			switch( action )
			{
				case PICKUP_OR_SET_DOWN:

					if( hasItemInHand )
					{
						ItemStack inSlot = theSlot.getStackInSlot( 0 );
						if( inSlot == null )
							player.inventory.setItemStack( interfaceSlot.addItems( player.inventory.getItemStack() ) );
						else
						{
							inSlot = inSlot.copy();
							ItemStack inHand = player.inventory.getItemStack().copy();

							theSlot.setInventorySlotContents( 0, null );
							player.inventory.setItemStack( null );

							player.inventory.setItemStack( interfaceSlot.addItems( inHand.copy() ) );

							if( player.inventory.getItemStack() == null )
								player.inventory.setItemStack( inSlot );
							else
							{
								player.inventory.setItemStack( inHand );
								theSlot.setInventorySlotContents( 0, inSlot );
							}
						}
					}
					else
					{
						IInventory mySlot = slotInv.getWrapper( slot );
						mySlot.setInventorySlotContents( 0, playerHand.addItems( mySlot.getStackInSlot( 0 ) ) );
					}

					break;
				case SPLIT_OR_PLACE_SINGLE:

					if( hasItemInHand )
					{
						ItemStack extra = playerHand.removeItems( 1, null, null );
						if( extra != null )
							extra = interfaceSlot.addItems( extra );
						if( extra != null )
							playerHand.addItems( extra );
					}
					else if( is != null )
					{
						ItemStack extra = interfaceSlot.removeItems( ( is.stackSize + 1 ) / 2, null, null );
						if( extra != null )
							extra = playerHand.addItems( extra );
						if( extra != null )
							interfaceSlot.addItems( extra );
					}

					break;
				case SHIFT_CLICK:

					IInventory mySlot = slotInv.getWrapper( slot );
					InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
					mySlot.setInventorySlotContents( 0, playerInv.addItems( mySlot.getStackInSlot( 0 ) ) );

					break;
				case MOVE_REGION:

					InventoryAdaptor playerInvAd = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
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

	private void regenList( NBTTagCompound data )
	{
		this.byId.clear();
		this.diList.clear();

		IActionHost host = this.getActionHost();
		if( host != null )
		{
			IGridNode agn = host.getActionableNode();
			if( agn != null && agn.isActive() )
			{
				for( IGridNode gn : this.g.getMachines( TileInterface.class ) )
				{
					IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
					DualityInterface dual = ih.getInterfaceDuality();
					if( gn.isActive() && dual.getConfigManager().getSetting( Settings.INTERFACE_TERMINAL ) == YesNo.YES )
						this.diList.put( ih, new InvTracker( dual, dual.getPatterns(), dual.getTermName() ) );
				}

				for( IGridNode gn : this.g.getMachines( PartInterface.class ) )
				{
					IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
					DualityInterface dual = ih.getInterfaceDuality();
					if( gn.isActive() && dual.getConfigManager().getSetting( Settings.INTERFACE_TERMINAL ) == YesNo.YES )
						this.diList.put( ih, new InvTracker( dual, dual.getPatterns(), dual.getTermName() ) );
				}
			}
		}

		data.setBoolean( "clear", true );

		for( Entry<IInterfaceHost, InvTracker> en : this.diList.entrySet() )
		{
			InvTracker inv = en.getValue();
			this.byId.put( inv.which, inv );
			this.addItems( data, inv, 0, inv.server.getSizeInventory() );
		}
	}

	private boolean isDifferent( ItemStack a, ItemStack b )
	{
		if( a == null && b == null )
			return false;

		if( a == null || b == null )
			return true;

		return !ItemStack.areItemStacksEqual( a, b );
	}

	private void addItems( NBTTagCompound data, InvTracker inv, int offset, int length )
	{
		String name = '=' + Long.toString( inv.which, Character.MAX_RADIX );
		NBTTagCompound tag = data.getCompoundTag( name );

		if( tag.hasNoTags() )
		{
			tag.setLong( "sortBy", inv.sortBy );
			tag.setString( "un", inv.unlocalizedName );
		}

		for( int x = 0; x < length; x++ )
		{
			NBTTagCompound itemNBT = new NBTTagCompound();

			ItemStack is = inv.server.getStackInSlot( x + offset );

			// "update" client side.
			inv.client.setInventorySlotContents( x + offset, is == null ? null : is.copy() );

			if( is != null )
				is.writeToNBT( itemNBT );

			tag.setTag( Integer.toString( x + offset ), itemNBT );
		}

		data.setTag( name, tag );
	}

	static class InvTracker
	{

		public final long sortBy;
		final long which = autoBase++;
		final String unlocalizedName;
		final IInventory client;
		final IInventory server;

		public InvTracker( DualityInterface dual, IInventory patterns, String unlocalizedName )
		{
			this.server = patterns;
			this.client = new AppEngInternalInventory( null, this.server.getSizeInventory() );
			this.unlocalizedName = unlocalizedName;
			this.sortBy = dual.getSortValue();
		}
	}


	static class PatternInvSlot extends WrapperInvSlot
	{

		public PatternInvSlot( IInventory inv )
		{
			super( inv );
		}

		@Override
		public boolean isItemValid( ItemStack itemstack )
		{
			return itemstack != null && itemstack.getItem() instanceof ItemEncodedPattern;
		}
	}
}
