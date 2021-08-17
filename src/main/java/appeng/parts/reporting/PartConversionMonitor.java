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


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import appeng.api.config.Actionable;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.InventoryAction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import appeng.api.AEApi;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.PlayerSource;
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
	public boolean onPartActivate( EntityPlayer player, EnumHand hand, Vec3d pos )
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

		final ItemStack eq = player.getHeldItem( hand );
		FluidStack fluidInTank = null;
		if( eq.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) )
		{
			IFluidHandlerItem fluidHandlerItem = ( eq.getCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) );
			fluidInTank = fluidHandlerItem.drain( Integer.MAX_VALUE, false );
		}

		if( this.isLocked() )
		{
			if( eq.isEmpty() )
			{
				this.insertItem( player, hand, true );
			}
			else if( Platform.isWrench( player, eq, this.getLocation().getPos() ) && ( this.getDisplayed() == null || !this.getDisplayed().equals( eq ) ) )
			{
				// wrench it
				return super.onPartActivate( player, hand, pos );
			}
			else if( fluidInTank != null && fluidInTank.amount > 0 )
			{
				if( this.getDisplayed() != null && getDisplayed().equals( AEFluidStack.fromFluidStack( fluidInTank ) ) )
				{
					this.drainFluidContainer( player, hand );
				}
			}
			else
			{
				this.insertItem( player, hand, false );
			}
		}

		//If its a fluid container, grab its fluidstack. if its empty pass its itemstack;

		if( fluidInTank != null && fluidInTank.amount > 0 )
		{
			if( getDisplayed() instanceof IAEItemStack || getDisplayed() == null )
			{
				return super.onPartActivate( player, hand, pos );
			}
			if( ( (IAEFluidStack) this.getDisplayed() ).equals( AEFluidStack.fromFluidStack( fluidInTank ) ) )
			{
				this.drainFluidContainer( player, hand );
			}
			else
			{
				return super.onPartActivate( player, hand, pos );
			}
		}
		else if( this.getDisplayed() != null && this.getDisplayed().equals( player.getHeldItem( hand ) ) )
		{
			this.insertItem( player, hand, false );
		}
		else if( !player.getHeldItem( hand ).isEmpty() )
		{
			return super.onPartActivate( player, hand, pos );
		}
		return true;
	}

	@Override
	public boolean onClicked( EntityPlayer player, EnumHand hand, Vec3d pos )
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

		if( this.getDisplayed() != null && this.getDisplayed() instanceof IAEItemStack )
		{
			this.extractItem( player, ( (IAEItemStack) this.getDisplayed() ).getDefinition().getMaxStackSize() );
		}
		else if( this.getDisplayed() != null && this.getDisplayed() instanceof IAEFluidStack )
		{
			this.fillFluidContainer( player,hand );
		}

		return true;
	}

	@Override
	public boolean onShiftClicked( EntityPlayer player, EnumHand hand, Vec3d pos )
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

		if( this.getDisplayed() != null )
		{
			this.extractItem( player, 1 );
		}

		return true;
	}

	private void insertItem( final EntityPlayer player, final EnumHand hand, final boolean allItems )
	{
		try
		{
			final IEnergySource energy = this.getProxy().getEnergy();
			final IMEMonitor<IAEItemStack> cell = this.getProxy()
					.getStorage()
					.getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );

			if( allItems )
			{
				if( this.getDisplayed() != null && this.getDisplayed() instanceof IAEItemStack)
				{
					final IAEItemStack input = (IAEItemStack) this.getDisplayed().copy();
					IItemHandler inv = new PlayerMainInvWrapper( player.inventory );

					for( int x = 0; x < inv.getSlots(); x++ )
					{
						final ItemStack targetStack = inv.getStackInSlot( x );
						if( input.equals( targetStack ) )
						{
							final ItemStack canExtract = inv.extractItem( x, targetStack.getCount(), true );
							if( !canExtract.isEmpty() )
							{
								input.setStackSize( canExtract.getCount() );
								final IAEItemStack failedToInsert = Platform.poweredInsert( energy, cell, input, new PlayerSource( player, this ) );
								inv.extractItem( x,
										failedToInsert == null ? canExtract.getCount() : canExtract.getCount() - (int) failedToInsert.getStackSize(),
										false );
							}
						}
					}
				}
			}
			else
			{
				final IAEItemStack input = AEItemStack.fromItemStack( player.getHeldItem( hand ) );
				final IAEItemStack failedToInsert = Platform.poweredInsert( energy, cell, input, new PlayerSource( player, this ) );
				player.setHeldItem( hand, failedToInsert == null ? ItemStack.EMPTY : failedToInsert.createItemStack() );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	private void extractItem( final EntityPlayer player, int count )
	{
		if (!(this.getDisplayed() instanceof IAEItemStack))
			return;
		final IAEItemStack input = (IAEItemStack) this.getDisplayed().copy();
		if( input != null )
		{
			try
			{
				if( !this.getProxy().isActive() )
				{
					return;
				}

				final IEnergySource energy = this.getProxy().getEnergy();
				final IMEMonitor<IAEItemStack> cell = this.getProxy()
						.getStorage()
						.getInventory(
								AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );

				input.setStackSize( count );

				final IAEItemStack retrieved = Platform.poweredExtraction( energy, cell, input, new PlayerSource( player, this ) );
				if( retrieved != null )
				{
					ItemStack newItems = retrieved.createItemStack();
					final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( player );
					newItems = adaptor.addItems( newItems );
					if( !newItems.isEmpty() )
					{
						final TileEntity te = this.getTile();
						final List<ItemStack> list = Collections.singletonList( newItems );
						Platform.spawnDrops( player.world, te.getPos().offset( this.getSide().getFacing() ), list );
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

	private void drainFluidContainer( final EntityPlayer player, final EnumHand hand ) {
		try
		{
			final ItemStack held = player.getHeldItem( hand );
			if( held.getCount() != 1 )
			{
				// only support stacksize 1 for now
				return;
			}

			final IFluidHandlerItem fh = FluidUtil.getFluidHandler( held );
			if( fh == null )
			{
				// only fluid handlers items
				return;
			}

			// See how much we can drain from the item
			final FluidStack extract = fh.drain( Integer.MAX_VALUE, false );
			if( extract == null || extract.amount < 1 )
			{
				return;
			}

			// Check if we can push into the system
			final IEnergySource energy = this.getProxy().getEnergy();
			final IMEMonitor<IAEFluidStack> cell = this.getProxy()
					.getStorage()
					.getInventory(
							AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
			final IAEFluidStack notStorable = Platform.poweredInsert( energy, cell, AEFluidStack.fromFluidStack( extract ), new PlayerSource( player, this ), Actionable.SIMULATE );

			if( notStorable != null && notStorable.getStackSize() > 0 )
			{
				final int toStore = (int) ( extract.amount - notStorable.getStackSize() );
				final FluidStack storable = fh.drain( toStore, false );

				if( storable == null || storable.amount == 0 )
				{
					return;
				}
				else
				{
					extract.amount = storable.amount;
				}
			}

			// Actually drain
			final FluidStack drained = fh.drain( extract, true );
			extract.amount = drained.amount;

			final IAEFluidStack notInserted = Platform.poweredInsert( energy, cell, AEFluidStack.fromFluidStack( extract ), new PlayerSource( player, this ) );

			if( notInserted != null && notInserted.getStackSize() > 0 )
			{
				AELog.error( "Fluid item [%s] reported a different possible amount to drain than it actually provided.", held.getDisplayName() );
			}

			player.setHeldItem( hand, fh.getContainer() );
		}
		catch( GridAccessException e )
		{
			e.printStackTrace();
		}
	}

	private void fillFluidContainer( final EntityPlayer player, final EnumHand hand  )
	{
		try
		{
			final ItemStack held = player.getHeldItem( hand );
			if( held.getCount() != 1 )
			{
				// only support stacksize 1 for now
				return;
			}

			final IFluidHandlerItem fh = FluidUtil.getFluidHandler( held );
			if( fh == null )
			{
				// only fluid handlers items
				return;
			}

			final IAEFluidStack stack = (IAEFluidStack) this.getDisplayed().copy();

			// Check how much we can store in the item
			stack.setStackSize( Integer.MAX_VALUE );
			int amountAllowed = fh.fill( stack.getFluidStack(), false );
			stack.setStackSize( amountAllowed );

			// Check if we can pull out of the system
			final IEnergySource energy = this.getProxy().getEnergy();
			final IMEMonitor<IAEFluidStack> cell = this.getProxy()
					.getStorage()
					.getInventory(
							AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
			final IAEFluidStack canPull = Platform.poweredExtraction( energy, cell, stack, new PlayerSource( player, this ), Actionable.SIMULATE );
			if( canPull == null || canPull.getStackSize() < 1 )
			{
				return;
			}

			// How much could fit into the container
			final int canFill = fh.fill( canPull.getFluidStack(), false );
			if( canFill == 0 )
			{
				return;
			}

			// Now actually pull out of the system
			stack.setStackSize( canFill );
			final IAEFluidStack pulled = Platform.poweredExtraction( energy, cell, stack, new PlayerSource( player, this ) );
			if( pulled == null || pulled.getStackSize() < 1 )
			{
				// Something went wrong
				AELog.error( "Unable to pull fluid out of the ME system even though the simulation said yes " );
				return;
			}

			// Actually fill
			final int used = fh.fill( pulled.getFluidStack(), true );

			if( used != canFill )
			{
				AELog.error( "Fluid item [%s] reported a different possible amount than it actually accepted.", held.getDisplayName() );
			}
			player.setHeldItem( hand, fh.getContainer() );
		}
		catch( GridAccessException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public IPartModel getStaticModels()
	{
		return this.selectModel( MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL,
				MODELS_LOCKED_OFF, MODELS_LOCKED_ON, MODELS_LOCKED_HAS_CHANNEL );
	}

}
