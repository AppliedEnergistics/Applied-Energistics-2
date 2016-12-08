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

package appeng.tile.misc;


import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.capabilities.Capabilities;
import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;


public class TileCondenser extends AEBaseInvTile implements IConfigManagerHost, IConfigurableObject
{

	public static final int BYTE_MULTIPLIER = 8;

	private final int[] sides = {
			0,
			1
	};
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 3 );
	private final ConfigManager cm = new ConfigManager( this );
	private final IItemHandler itemHandler = new ItemHandler();
	private final IFluidHandler fluidHandler = new FluidHandler();
	private final MEHandler meHandler = new MEHandler();

	private double storedPower = 0;

	public TileCondenser()
	{
		this.cm.registerSetting( Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileCondenser( final NBTTagCompound data )
	{
		this.cm.writeToNBT( data );
		data.setDouble( "storedPower", this.getStoredPower() );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileCondenser( final NBTTagCompound data )
	{
		this.cm.readFromNBT( data );
		this.setStoredPower( data.getDouble( "storedPower" ) );
	}

	public double getStorage()
	{
		final ItemStack is = this.inv.getStackInSlot( 2 );
		if( is != null )
		{
			if( is.getItem() instanceof IStorageComponent )
			{
				final IStorageComponent sc = (IStorageComponent) is.getItem();
				if( sc.isStorageComponent( is ) )
				{
					return sc.getBytes( is ) * BYTE_MULTIPLIER;
				}
			}
		}
		return 0;
	}

	public void addPower( final double rawPower )
	{
		this.setStoredPower( this.getStoredPower() + rawPower );
		this.setStoredPower( Math.max( 0.0, Math.min( this.getStorage(), this.getStoredPower() ) ) );

		final double requiredPower = this.getRequiredPower();
		final ItemStack output = this.getOutput();
		while( requiredPower <= this.getStoredPower() && output != null && requiredPower > 0 )
		{
			if( this.canAddOutput( output ) )
			{
				this.setStoredPower( this.getStoredPower() - requiredPower );
				this.addOutput( output );
			}
			else
			{
				break;
			}
		}
	}

	private boolean canAddOutput( final ItemStack output )
	{
		final ItemStack outputStack = this.getStackInSlot( 1 );
		return outputStack == null || ( Platform.itemComparisons().isEqualItem( outputStack,
				output ) && outputStack.getCount() < outputStack.getMaxStackSize() );
	}

	/**
	 * make sure you validate with canAddOutput prior to this.
	 *
	 * @param output to be added output
	 */
	private void addOutput( final ItemStack output )
	{
		final ItemStack outputStack = this.getStackInSlot( 1 );
		if( outputStack == null )
		{
			this.setInventorySlotContents( 1, output.copy() );
		}
		else
		{
			outputStack.grow( 1 );
			this.setInventorySlotContents( 1, outputStack );
		}
	}

	private ItemStack getOutput()
	{
		final IMaterials materials = AEApi.instance().definitions().materials();

		switch( (CondenserOutput) this.cm.getSetting( Settings.CONDENSER_OUTPUT ) )
		{
			case MATTER_BALLS:
				return materials.matterBall().maybeStack( 1 ).orElse( null );

			case SINGULARITY:
				return materials.singularity().maybeStack( 1 ).orElse( null );

			case TRASH:
			default:
				return null;
		}
	}

	public double getRequiredPower()
	{
		return ( (CondenserOutput) this.cm.getSetting( Settings.CONDENSER_OUTPUT ) ).requiredPower;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void setInventorySlotContents( final int i, final ItemStack itemstack )
	{
		if( i == 0 )
		{
			if( itemstack != null )
			{
				this.addPower( itemstack.getCount() );
			}
		}
		else
		{
			this.inv.setInventorySlotContents( 1, itemstack );
		}
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return i == 0;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( slot == 0 )
		{
			final ItemStack is = inv.getStackInSlot( 0 );
			if( is != null )
			{
				this.addPower( is.getCount() );
				inv.setInventorySlotContents( 0, null );
			}
		}
	}

	@Override
	public boolean canInsertItem( final int slotIndex, final ItemStack insertingItem, final EnumFacing side )
	{
		return slotIndex == 0;
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final EnumFacing side )
	{
		return slotIndex != 0;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final EnumFacing side )
	{
		return this.sides;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
		this.addPower( 0 );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.cm;
	}

	public double getStoredPower()
	{
		return this.storedPower;
	}

	private void setStoredPower( final double storedPower )
	{
		this.storedPower = storedPower;
	}

	@Override
	public boolean hasCapability( Capability<?> capability, EnumFacing facing )
	{
		if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return true;
		}
		else if( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
			return true;
		}
		else if( capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR )
		{
			return true;
		}
		return super.hasCapability( capability, facing );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getCapability( Capability<T> capability, @Nullable EnumFacing facing )
	{
		if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return (T) itemHandler;
		}
		else if( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY )
		{
			return (T) fluidHandler;
		}
		else if( capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR )
		{
			return (T) meHandler;
		}
		return super.getCapability( capability, facing );
	}

	private class ItemHandler implements IItemHandler
	{

		@Override
		public int getSlots()
		{
			// We only expose the void slot
			return 1;
		}

		@Override
		public ItemStack getStackInSlot( int slot )
		{
			// The void slot never has any content
			return null;
		}

		@Override
		public ItemStack insertItem( int slot, ItemStack stack, boolean simulate )
		{
			if( slot != 0 )
			{
				return stack;
			}
			if( !simulate && stack != null )
			{
				addPower( stack.getCount() );
			}
			return null;
		}

		@Override
		public ItemStack extractItem( int slot, int amount, boolean simulate )
		{
			return null;
		}

		@Override
		public int getSlotLimit( int slot )
		{
			return 0;
		}
	}

	private static final IFluidTankProperties[] EMPTY = { new FluidTankProperties( null, 10, true, false ) };

	/**
	 * A fluid handler that exposes a 10 bucket tank that can only be filled, and - when filled - will add power
	 * to this condenser.
	 */
	private class FluidHandler implements IFluidHandler
	{

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			return EMPTY;
		}

		@Override
		public int fill( FluidStack resource, boolean doFill )
		{
			if( doFill )
			{
				addPower( ( resource == null ? 0.0 : (double) resource.amount ) / 500.0 );
			}

			return resource == null ? 0 : resource.amount;
		}

		@Nullable
		@Override
		public FluidStack drain( FluidStack resource, boolean doDrain )
		{
			return null;
		}

		@Nullable
		@Override
		public FluidStack drain( int maxDrain, boolean doDrain )
		{
			return null;
		}
	}

	/**
	 * This is used to expose a fake ME subnetwork that is only composed of this condenser tile. The purpose of this is
	 * to enable the condenser to
	 * override the {@link appeng.api.storage.IMEInventoryHandler#validForPass(int)} method to make sure a condenser is
	 * only ever used if an item
	 * can't go anywhere else.
	 */
	private class MEHandler implements IStorageMonitorableAccessor, IStorageMonitorable
	{
		private final CondenserFluidInventory fluidInventory = new CondenserFluidInventory( TileCondenser.this );

		private final CondenserItemInventory itemInventory = new CondenserItemInventory( TileCondenser.this );

		@Nullable
		@Override
		public IStorageMonitorable getInventory( BaseActionSource src )
		{
			return this;
		}

		@Override
		public IMEMonitor<IAEItemStack> getItemInventory()
		{
			return itemInventory;
		}

		@Override
		public IMEMonitor<IAEFluidStack> getFluidInventory()
		{
			return fluidInventory;
		}
	}

	@Override
	public boolean isEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
