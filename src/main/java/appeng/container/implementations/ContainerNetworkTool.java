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


import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;


public class ContainerNetworkTool extends AEBaseContainer
{

	private final INetworkTool toolInv;

	@GuiSync( 1 )
	public boolean facadeMode;

	public ContainerNetworkTool( final InventoryPlayer ip, final INetworkTool te )
	{
		super( ip, null, null );
		this.toolInv = te;

		this.lockPlayerInventorySlot( ip.currentItem );

		for( int y = 0; y < 3; y++ )
		{
			for( int x = 0; x < 3; x++ )
			{
				this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, te
						                                                                                                   .getInventory(), y * 3 + x, 80 - 18 + x * 18, 37 - 18 + y * 18, this.getInventoryPlayer() ) ) );
			}
		}

		this.bindPlayerInventory( ip, 0, 166 - /* height of player inventory */82 );
	}

	public void toggleFacadeMode()
	{
		final NBTTagCompound data = Platform.openNbtData( this.toolInv.getItemStack() );
		data.setBoolean( "hideFacades", !data.getBoolean( "hideFacades" ) );
		this.detectAndSendChanges();
	}

	@Override
	public void detectAndSendChanges()
	{
		final ItemStack currentItem = this.getPlayerInv().getCurrentItem();

		if( currentItem != this.toolInv.getItemStack() )
		{
			if( !currentItem.isEmpty() )
			{
				if( ItemStack.areItemsEqual( this.toolInv.getItemStack(), currentItem ) )
				{
					this.getPlayerInv().setInventorySlotContents( this.getPlayerInv().currentItem, this.toolInv.getItemStack() );
				}
				else
				{
					this.setValidContainer( false );
				}
			}
			else
			{
				this.setValidContainer( false );
			}
		}

		if( this.isValidContainer() )
		{
			final NBTTagCompound data = Platform.openNbtData( currentItem );
			this.setFacadeMode( data.getBoolean( "hideFacades" ) );
		}

		super.detectAndSendChanges();
	}

	@Override
	public void onSlotChange( Slot s )
	{
		super.detectAndSendChanges();
	}


	public boolean isFacadeMode()
	{
		return this.facadeMode;
	}

	private void setFacadeMode( final boolean facadeMode )
	{
		this.facadeMode = facadeMode;
	}
}
