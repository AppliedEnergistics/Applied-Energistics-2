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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.parts.PartModel;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class PartConversionMonitor extends AbstractPartMonitor
{

	@PartModels
	public static final ResourceLocation MODEL_OFF = new ResourceLocation( AppEng.MOD_ID, "part/conversion_monitor_off" );
	@PartModels
	public static final ResourceLocation MODEL_ON = new ResourceLocation( AppEng.MOD_ID, "part/conversion_monitor_on" );
	@PartModels
	public static final ResourceLocation MODEL_LOCKED_OFF = new ResourceLocation( AppEng.MOD_ID, "part/conversion_monitor_locked_off" );
	@PartModels
	public static final ResourceLocation MODEL_LOCKED_ON = new ResourceLocation( AppEng.MOD_ID, "part/conversion_monitor_locked_on" );

	public static final IPartModel MODELS_OFF = new PartModel( MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF );
	public static final IPartModel MODELS_ON = new PartModel( MODEL_BASE, MODEL_ON, MODEL_STATUS_ON );
	public static final IPartModel MODELS_HAS_CHANNEL = new PartModel( MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL );
	public static final IPartModel MODELS_LOCKED_OFF = new PartModel( MODEL_BASE, MODEL_LOCKED_OFF, MODEL_STATUS_OFF );
	public static final IPartModel MODELS_LOCKED_ON = new PartModel( MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_ON );
	public static final IPartModel MODELS_LOCKED_HAS_CHANNEL = new PartModel( MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_HAS_CHANNEL );

	@Reflected
	public PartConversionMonitor( final ItemStack is )
	{
		super( is );
	}

	@Override
	public boolean onPartShiftActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( Platform.isClient() )
		{
			return true;
		}

		if( !this.getProxy().isActive() )
		{
			return false;
		}

		if( !Platform.hasPermissions( this.getLocation(), player ) )
		{
			return false;
		}

		boolean ModeB = false;

		ItemStack item = player.getHeldItem( hand );
		if( item == null && this.getDisplayed() != null )
		{
			ModeB = true;
			item = ( (IAEItemStack) this.getDisplayed() ).getItemStack();
		}

		if( item != null )
		{
			try
			{
				if( !this.getProxy().isActive() )
				{
					return false;
				}

				final IEnergySource energy = this.getProxy().getEnergy();
				final IMEMonitor<IAEItemStack> cell = this.getProxy().getStorage().getItemInventory();
				final IAEItemStack input = AEItemStack.create( item );

				if( ModeB )
				{
					for( int x = 0; x < player.inventory.getSizeInventory(); x++ )
					{
						final ItemStack targetStack = player.inventory.getStackInSlot( x );
						if( input.equals( targetStack ) )
						{
							final IAEItemStack insertItem = input.copy();
							insertItem.setStackSize( targetStack.stackSize );
							final IAEItemStack failedToInsert = Platform.poweredInsert( energy, cell, insertItem, new PlayerSource( player, this ) );
							player.inventory.setInventorySlotContents( x, failedToInsert == null ? null : failedToInsert.getItemStack() );
						}
					}
				}
				else
				{
					final IAEItemStack failedToInsert = Platform.poweredInsert( energy, cell, input, new PlayerSource( player, this ) );
					player.inventory.setInventorySlotContents( player.inventory.currentItem, failedToInsert == null ? null : failedToInsert.getItemStack() );
				}
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}
		return true;
	}

	@Override
	protected void extractItem( final EntityPlayer player )
	{
		final IAEItemStack input = (IAEItemStack) this.getDisplayed();
		if( input != null )
		{
			try
			{
				if( !this.getProxy().isActive() )
				{
					return;
				}

				final IEnergySource energy = this.getProxy().getEnergy();
				final IMEMonitor<IAEItemStack> cell = this.getProxy().getStorage().getItemInventory();

				final ItemStack is = input.getItemStack();
				input.setStackSize( is.getMaxStackSize() );

				final IAEItemStack retrieved = Platform.poweredExtraction( energy, cell, input, new PlayerSource( player, this ) );
				if( retrieved != null )
				{
					ItemStack newItems = retrieved.getItemStack();
					final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( player, EnumFacing.UP );
					newItems = adaptor.addItems( newItems );
					if( newItems != null )
					{
						final TileEntity te = this.getTile();
						final List<ItemStack> list = Collections.singletonList( newItems );
						Platform.spawnDrops( player.worldObj, te.getPos().offset( this.getSide().getFacing() ), list );
					}

					if( player.openContainer != null )
					{
						player.openContainer.detectAndSendChanges();
					}
				}
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}
	}

	@Override
	public IPartModel getStaticModels()
	{
		return selectModel( MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL,
				MODELS_LOCKED_OFF, MODELS_LOCKED_ON, MODELS_LOCKED_HAS_CHANNEL );
	}

}
