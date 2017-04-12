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


import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.INetworkEncodable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.tile.misc.TileSecurity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class ContainerSecurity extends ContainerMEMonitorable implements IAEAppEngInventory
{

	private final SlotRestrictedInput configSlot;

	private final AppEngInternalInventory wirelessEncoder = new AppEngInternalInventory( this, 2 );

	private final SlotRestrictedInput wirelessIn;
	private final SlotOutput wirelessOut;

	private final TileSecurity securityBox;
	@GuiSync( 0 )
	public int permissionMode = 0;

	public ContainerSecurity( final InventoryPlayer ip, final ITerminalHost monitorable )
	{
		super( ip, monitorable, false );

		this.securityBox = (TileSecurity) monitorable;

		this.addSlotToContainer( this.configSlot = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.BIOMETRIC_CARD, this.securityBox.getConfigSlot(), 0, 37, -33, ip ) );

		this.addSlotToContainer( this.wirelessIn = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ENCODABLE_ITEM, this.wirelessEncoder, 0, 212, 10, ip ) );
		this.addSlotToContainer( this.wirelessOut = new SlotOutput( this.wirelessEncoder, 1, 212, 68, -1 ) );

		this.bindPlayerInventory( ip, 0, 0 );
	}

	public void toggleSetting( final String value, final EntityPlayer player )
	{
		try
		{
			final SecurityPermissions permission = SecurityPermissions.valueOf( value );

			final ItemStack a = this.configSlot.getStack();
			if( a != null && a.getItem() instanceof IBiometricCard )
			{
				final IBiometricCard bc = (IBiometricCard) a.getItem();
				if( bc.hasPermission( a, permission ) )
				{
					bc.removePermission( a, permission );
				}
				else
				{
					bc.addPermission( a, permission );
				}
			}
		}
		catch( final EnumConstantNotPresentException ex )
		{
			// :(
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.SECURITY, false );

		this.setPermissionMode( 0 );

		final ItemStack a = this.configSlot.getStack();
		if( a != null && a.getItem() instanceof IBiometricCard )
		{
			final IBiometricCard bc = (IBiometricCard) a.getItem();

			for( final SecurityPermissions sp : bc.getPermissions( a ) )
			{
				this.setPermissionMode( this.getPermissionMode() | ( 1 << sp.ordinal() ) );
			}
		}

		this.updatePowerStatus();

		super.detectAndSendChanges();
	}

	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		super.onContainerClosed( player );

		if( this.wirelessIn.getHasStack() )
		{
			player.dropPlayerItemWithRandomChoice( this.wirelessIn.getStack(), false );
		}

		if( this.wirelessOut.getHasStack() )
		{
			player.dropPlayerItemWithRandomChoice( this.wirelessOut.getStack(), false );
		}
	}

	@Override
	public void saveChanges()
	{
		// :P
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{
		if( !this.wirelessOut.getHasStack() )
		{
			if( this.wirelessIn.getHasStack() )
			{
				final ItemStack term = this.wirelessIn.getStack().copy();
				INetworkEncodable networkEncodable = null;

				if( term.getItem() instanceof INetworkEncodable )
				{
					networkEncodable = (INetworkEncodable) term.getItem();
				}

				final IWirelessTermHandler wTermHandler = AEApi.instance().registries().wireless().getWirelessTerminalHandler( term );
				if( wTermHandler != null )
				{
					networkEncodable = wTermHandler;
				}

				if( networkEncodable != null )
				{
					networkEncodable.setEncryptionKey( term, String.valueOf( this.securityBox.getSecurityKey() ), "" );

					this.wirelessIn.putStack( null );
					this.wirelessOut.putStack( term );

					// update the two slots in question...
					for( final Object crafter : this.crafters )
					{
						final ICrafting icrafting = (ICrafting) crafter;
						icrafting.sendSlotContents( this, this.wirelessIn.slotNumber, this.wirelessIn.getStack() );
						icrafting.sendSlotContents( this, this.wirelessOut.slotNumber, this.wirelessOut.getStack() );
					}
				}
			}
		}
	}

	public int getPermissionMode()
	{
		return this.permissionMode;
	}

	private void setPermissionMode( final int permissionMode )
	{
		this.permissionMode = permissionMode;
	}
}
