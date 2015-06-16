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


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.AEApi;
import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.tile.AEBaseInvTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;


public class TileCondenser extends AEBaseInvTile implements IFluidHandler, IConfigManagerHost, IConfigurableObject
{

	private static final FluidTankInfo[] EMPTY = new FluidTankInfo[] { new FluidTankInfo( null, 10 ) };
	final int[] sides = new int[] { 0, 1 };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 3 );
	final ConfigManager cm = new ConfigManager( this );

	public double storedPower = 0;

	public TileCondenser()
	{
		this.cm.registerSetting( Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileCondenser( NBTTagCompound data )
	{
		this.cm.writeToNBT( data );
		data.setDouble( "storedPower", this.storedPower );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileCondenser( NBTTagCompound data )
	{
		this.cm.readFromNBT( data );
		this.storedPower = data.getDouble( "storedPower" );
	}

	public double getStorage()
	{
		ItemStack is = this.inv.getStackInSlot( 2 );
		if( is != null )
		{
			if( is.getItem() instanceof IStorageComponent )
			{
				IStorageComponent sc = (IStorageComponent) is.getItem();
				if( sc.isStorageComponent( is ) )
				{
					return sc.getBytes( is ) * 8;
				}
			}
		}
		return 0;
	}

	public void addPower( double rawPower )
	{
		this.storedPower += rawPower;
		this.storedPower = Math.max( 0.0, Math.min( this.getStorage(), this.storedPower ) );

		double requiredPower = this.getRequiredPower();
		ItemStack output = this.getOutput();
		while( requiredPower <= this.storedPower && output != null && requiredPower > 0 )
		{
			if( this.canAddOutput( output ) )
			{
				this.storedPower -= requiredPower;
				this.addOutput( output );
			}
			else
			{
				break;
			}
		}
	}

	private boolean canAddOutput( ItemStack output )
	{
		ItemStack outputStack = this.getStackInSlot( 1 );
		return outputStack == null || ( Platform.isSameItem( outputStack, output ) && outputStack.stackSize < outputStack.getMaxStackSize() );
	}

	/**
	 * make sure you validate with canAddOutput prior to this.
	 *
	 * @param output to be added output
	 */
	private void addOutput( ItemStack output )
	{
		ItemStack outputStack = this.getStackInSlot( 1 );
		if( outputStack == null )
		{
			this.setInventorySlotContents( 1, output.copy() );
		}
		else
		{
			outputStack.stackSize++;
			this.setInventorySlotContents( 1, outputStack );
		}
	}

	private ItemStack getOutput()
	{
		final IMaterials materials = AEApi.instance().definitions().materials();

		switch( (CondenserOutput) this.cm.getSetting( Settings.CONDENSER_OUTPUT ) )
		{
			case MATTER_BALLS:
				for( ItemStack matterBallStack : materials.matterBall().maybeStack( 1 ).asSet() )
				{
					return matterBallStack;
				}

			case SINGULARITY:
				for( ItemStack singularityStack : materials.singularity().maybeStack( 1 ).asSet() )
				{
					return singularityStack;
				}

			case TRASH:
			default:
		}
		return null;
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
	public void setInventorySlotContents( int i, ItemStack itemstack )
	{
		if( i == 0 )
		{
			if( itemstack != null )
			{
				this.addPower( itemstack.stackSize );
			}
		}
		else
		{
			this.inv.setInventorySlotContents( 1, itemstack );
		}
	}

	@Override
	public boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return i == 0;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added )
	{
		if( slot == 0 )
		{
			ItemStack is = inv.getStackInSlot( 0 );
			if( is != null )
			{
				this.addPower( is.stackSize );
				inv.setInventorySlotContents( 0, null );
			}
		}
	}

	@Override
	public boolean canInsertItem( int slotIndex, ItemStack insertingItem, EnumFacing side )
	{
		return slotIndex == 0;
	}

	@Override
	public boolean canExtractItem( int slotIndex, ItemStack extractedItem, EnumFacing side )
	{
		return slotIndex != 0;
	}

	@Override
	public int[] getAccessibleSlotsBySide( EnumFacing side )
	{
		return this.sides;
	}

	@Override
	public int fill( EnumFacing from, FluidStack resource, boolean doFill )
	{
		if( doFill )
		{
			this.addPower( ( resource == null ? 0.0 : (double) resource.amount ) / 500.0 );
		}

		return resource == null ? 0 : resource.amount;
	}

	@Override
	public FluidStack drain( EnumFacing from, FluidStack resource, boolean doDrain )
	{
		return null;
	}

	@Override
	public FluidStack drain( EnumFacing from, int maxDrain, boolean doDrain )
	{
		return null;
	}

	@Override
	public boolean canFill( EnumFacing from, Fluid fluid )
	{
		return true;
	}

	@Override
	public boolean canDrain( EnumFacing from, Fluid fluid )
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo( EnumFacing from )
	{
		return EMPTY;
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{
		this.addPower( 0 );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.cm;
	}
}
