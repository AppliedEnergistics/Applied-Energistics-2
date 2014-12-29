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

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileVibrationChamber extends AENetworkInvTile implements IGridTickable
{

	final double powerPerTick = 5;

	final int sides[] = new int[] { 0 };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );

	public int burnSpeed = 100;
	public double burnTime = 0;
	public double maxBurnTime = 0;

	// client side..
	public boolean isOn;

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileVibrationChamber(ByteBuf data)
	{
		boolean wasOn = this.isOn;
		this.isOn = data.readBoolean();
		return wasOn != this.isOn; // TESR doesn't need updates!
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileVibrationChamber(ByteBuf data)
	{
		data.writeBoolean( this.burnTime > 0 );
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileVibrationChamber(NBTTagCompound data)
	{
		data.setDouble( "burnTime", this.burnTime );
		data.setDouble( "maxBurnTime", this.maxBurnTime );
		data.setInteger( "burnSpeed", this.burnSpeed );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileVibrationChamber(NBTTagCompound data)
	{
		this.burnTime = data.getDouble( "burnTime" );
		this.maxBurnTime = data.getDouble( "maxBurnTime" );
		this.burnSpeed = data.getInteger( "burnSpeed" );
	}

	public TileVibrationChamber() {
		this.gridProxy.setIdlePowerUsage( 0 );
		this.gridProxy.setFlags();
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( this.burnTime <= 0 )
		{
			if ( this.canEatFuel() )
			{
				try
				{
					this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
				}
				catch (GridAccessException e)
				{
					// wake up!
				}
			}
		}
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return this.sides;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return TileEntityFurnace.getItemBurnTime( itemstack ) > 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		if ( this.burnTime <= 0 )
			this.eatFuel();

		return new TickingRequest( TickRates.VibrationChamber.min, TickRates.VibrationChamber.max, this.burnTime <= 0, false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( this.burnTime <= 0 )
		{
			this.eatFuel();

			if ( this.burnTime > 0 )
				return TickRateModulation.URGENT;

			this.burnSpeed = 100;
			return TickRateModulation.SLEEP;
		}

		this.burnSpeed = Math.max( 20, Math.min( this.burnSpeed, 200 ) );
		double dilation = this.burnSpeed / 100.0;

		double timePassed = TicksSinceLastCall * dilation;
		this.burnTime -= timePassed;
		if ( this.burnTime < 0 )
		{
			timePassed += this.burnTime;
			this.burnTime = 0;
		}

		try
		{
			IEnergyGrid grid = this.gridProxy.getEnergy();
			double newPower = timePassed * this.powerPerTick;
			double overFlow = grid.injectPower( newPower, Actionable.SIMULATE );

			// burn the over flow.
			grid.injectPower( Math.max( 0.0, newPower - overFlow ), Actionable.MODULATE );

			if ( overFlow > 0 )
				this.burnSpeed -= TicksSinceLastCall;
			else
				this.burnSpeed += TicksSinceLastCall;

			this.burnSpeed = Math.max( 20, Math.min( this.burnSpeed, 200 ) );
			return overFlow > 0 ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
		}
		catch (GridAccessException e)
		{
			this.burnSpeed -= TicksSinceLastCall;
			this.burnSpeed = Math.max( 20, Math.min( this.burnSpeed, 200 ) );
			return TickRateModulation.SLOWER;
		}
	}

	private boolean canEatFuel()
	{
		ItemStack is = this.getStackInSlot( 0 );
		if ( is != null )
		{
			int newBurnTime = TileEntityFurnace.getItemBurnTime( is );
			if ( newBurnTime > 0 && is.stackSize > 0 )
				return true;
		}
		return false;
	}

	private void eatFuel()
	{
		ItemStack is = this.getStackInSlot( 0 );
		if ( is != null )
		{
			int newBurnTime = TileEntityFurnace.getItemBurnTime( is );
			if ( newBurnTime > 0 && is.stackSize > 0 )
			{
				this.burnTime += newBurnTime;
				this.maxBurnTime = this.burnTime;
				is.stackSize--;
				if ( is.stackSize <= 0 )
				{
					ItemStack container = null;

					if ( is.getItem().hasContainerItem( is ) )
						container = is.getItem().getContainerItem( is );

					this.setInventorySlotContents( 0, container );
				}
				else
					this.setInventorySlotContents( 0, is );
			}
		}

		if ( this.burnTime > 0 )
		{
			try
			{
				this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// gah!
			}
		}

		if ( (!this.isOn && this.burnTime > 0) || (this.isOn && this.burnTime <= 0) )
		{
			this.isOn = this.burnTime > 0;
			this.markForUpdate();
		}
	}
}
