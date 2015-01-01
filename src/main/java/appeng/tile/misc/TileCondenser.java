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
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import appeng.api.AEApi;
import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
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

	final int[] sides = new int[] { 0, 1 };
	static private final FluidTankInfo[] EMPTY = new FluidTankInfo[] { new FluidTankInfo( null, 10 ) };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 3 );
	final ConfigManager cm = new ConfigManager( this );

	public double storedPower = 0;

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileCondenser(NBTTagCompound data)
	{
		this.cm.writeToNBT( data );
		data.setDouble( "storedPower", this.storedPower );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileCondenser(NBTTagCompound data)
	{
		this.cm.readFromNBT( data );
		this.storedPower = data.getDouble( "storedPower" );
	}

	public TileCondenser() {
		this.cm.registerSetting( Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH );
	}

	public double getStorage()
	{
		ItemStack is = this.inv.getStackInSlot( 2 );
		if ( is != null )
		{
			if ( is.getItem() instanceof IStorageComponent )
			{
				IStorageComponent sc = (IStorageComponent) is.getItem();
				if ( sc.isStorageComponent( is ) )
					return sc.getBytes( is ) * 8;
			}
		}
		return 0;
	}

	public void addPower(double rawPower)
	{
		this.storedPower += rawPower;
		this.storedPower = Math.max( 0.0, Math.min( this.getStorage(), this.storedPower ) );

		double requiredPower = this.getRequiredPower();
		ItemStack output = this.getOutput();
		while (requiredPower <= this.storedPower && output != null && requiredPower > 0)
		{
			if ( this.canAddOutput( output ) )
			{
				this.storedPower -= requiredPower;
				this.addOutput( output );
			}
			else
				break;
		}

	}

	private boolean canAddOutput(ItemStack output)
	{
		ItemStack outputStack = this.getStackInSlot( 1 );
		return outputStack == null || (Platform.isSameItem( outputStack, output ) && outputStack.stackSize < outputStack.getMaxStackSize());
	}

	/**
	 * make sure you validate with canAddOutput prior to this.
	 * 
	 * @param output to be added output
	 */
	private void addOutput(ItemStack output)
	{
		ItemStack outputStack = this.getStackInSlot( 1 );
		if ( outputStack == null )
			this.setInventorySlotContents( 1, output.copy() );
		else
		{
			outputStack.stackSize++;
			this.setInventorySlotContents( 1, outputStack );
		}
	}

	private ItemStack getOutput()
	{
		switch ((CondenserOutput) this.cm.getSetting( Settings.CONDENSER_OUTPUT ))
		{
		case MATTER_BALLS:
			return AEApi.instance().materials().materialMatterBall.stack( 1 );
		case SINGULARITY:
			return AEApi.instance().materials().materialSingularity.stack( 1 );
		case TRASH:
		default:
		}
		return null;
	}

	public double getRequiredPower()
	{
		return ((CondenserOutput) this.cm.getSetting( Settings.CONDENSER_OUTPUT )).requiredPower;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if ( i == 0 )
		{
			if ( itemstack != null )
				this.addPower( itemstack.stackSize );
		}
		else
		{
			this.inv.setInventorySlotContents( 1, itemstack );
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i != 0;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return i == 0;
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return this.sides;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( slot == 0 )
		{
			ItemStack is = inv.getStackInSlot( 0 );
			if ( is != null )
			{
				this.addPower( is.stackSize );
				inv.setInventorySlotContents( 0, null );
			}
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if ( doFill )
			this.addPower( (resource == null ? 0.0 : (double) resource.amount) / 500.0 );

		return resource == null ? 0 : resource.amount;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return EMPTY;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		this.addPower( 0 );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.cm;
	}

}
