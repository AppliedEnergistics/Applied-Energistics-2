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

package appeng.parts.reporting;


import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PartConversionMonitor extends PartStorageMonitor
{

	public PartConversionMonitor(ItemStack is) {
		super( PartConversionMonitor.class, is );
		frontBright = CableBusTextures.PartConversionMonitor_Bright;
		frontColored = CableBusTextures.PartConversionMonitor_Colored;
		frontDark = CableBusTextures.PartConversionMonitor_Dark;
		// frontSolid = CableBusTextures.PartConversionMonitor_Solid;
	}

	@Override
	public boolean onPartShiftActivate(EntityPlayer player, Vec3 pos)
	{
		if ( Platform.isClient() )
			return true;

		if ( !proxy.isActive() )
			return false;

		if ( !Platform.hasPermissions( getLocation(), player ) )
			return false;

		boolean ModeB = false;

		ItemStack item = player.getCurrentEquippedItem();
		if ( item == null && getDisplayed() != null )
		{
			ModeB = true;
			item = ((IAEItemStack) getDisplayed()).getItemStack();
		}

		if ( item != null )
		{
			try
			{
				if ( !proxy.isActive() )
					return false;

				IEnergySource energy = proxy.getEnergy();
				IMEMonitor<IAEItemStack> cell = proxy.getStorage().getItemInventory();
				IAEItemStack input = AEItemStack.create( item );

				if ( ModeB )
				{
					for (int x = 0; x < player.inventory.getSizeInventory(); x++)
					{
						ItemStack targetStack = player.inventory.getStackInSlot( x );
						if ( input.equals( targetStack ) )
						{
							IAEItemStack insertItem = input.copy();
							insertItem.setStackSize( targetStack.stackSize );
							IAEItemStack failedToInsert = Platform.poweredInsert( energy, cell, insertItem, new PlayerSource( player, this ) );
							player.inventory.setInventorySlotContents( x, failedToInsert == null ? null : failedToInsert.getItemStack() );
						}
					}
				}
				else
				{
					IAEItemStack failedToInsert = Platform.poweredInsert( energy, cell, input, new PlayerSource( player, this ) );
					player.inventory.setInventorySlotContents( player.inventory.currentItem, failedToInsert == null ? null : failedToInsert.getItemStack() );
				}
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
		return true;
	}

	@Override
	protected void extractItem(EntityPlayer player)
	{
		IAEItemStack input = (IAEItemStack) getDisplayed();
		if ( input != null )
		{
			try
			{
				if ( !proxy.isActive() )
					return;

				IEnergySource energy = proxy.getEnergy();
				IMEMonitor<IAEItemStack> cell = proxy.getStorage().getItemInventory();

				ItemStack is = input.getItemStack();
				input.setStackSize( is.getMaxStackSize() );

				IAEItemStack retrieved = Platform.poweredExtraction( energy, cell, input, new PlayerSource( player, this ) );
				if ( retrieved != null )
				{
					ItemStack newItems = retrieved.getItemStack();
					InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
					newItems = adaptor.addItems( newItems );
					if ( newItems != null )
					{
						TileEntity te = tile;
						List<ItemStack> list = Collections.singletonList( newItems );
						Platform.spawnDrops( player.worldObj, te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ, list );
					}

					if ( player.openContainer != null )
						player.openContainer.detectAndSendChanges();
				}
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
	}

}
