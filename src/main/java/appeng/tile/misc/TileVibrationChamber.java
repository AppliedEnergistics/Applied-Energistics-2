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
import net.minecraft.util.EnumFacing;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.settings.TickRates;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;


public class TileVibrationChamber extends AENetworkInvTile implements IGridTickable
{
	private static final int FUEL_SLOT_INDEX = 0;
	private static final double POWER_PER_TICK = 5;
	private static final int[] ACCESSIBLE_SLOTS = new int[] { FUEL_SLOT_INDEX };
	private static final int MAX_BURN_SPEED = 200;
	private static final double DILATION_SCALING = 100.0;
	private static final int MIN_BURN_SPEED = 20;
	private final IInventory inv = new AppEngInternalInventory( this, 1 );

	public int burnSpeed = 100;
	public double burnTime = 0;
	public double maxBurnTime = 0;

	// client side..
	public boolean isOn;

	public TileVibrationChamber()
	{
		this.gridProxy.setIdlePowerUsage( 0 );
		this.gridProxy.setFlags();
	}

	@Override
	public AECableType getCableConnectionType( AEPartLocation dir )
	{
		return AECableType.COVERED;
	}

	@Reflected
	@TileEvent( TileEventType.NETWORK_READ )
	public boolean hasUpdate( ByteBuf data )
	{
		final boolean wasOn = this.isOn;

		this.isOn = data.readBoolean();

		return wasOn != this.isOn; // TESR doesn't need updates!
	}

	@Reflected
	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToNetwork( ByteBuf data )
	{
		data.writeBoolean( this.burnTime > 0 );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileVibrationChamber( NBTTagCompound data )
	{
		data.setDouble( "burnTime", this.burnTime );
		data.setDouble( "maxBurnTime", this.maxBurnTime );
		data.setInteger( "burnSpeed", this.burnSpeed );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileVibrationChamber( NBTTagCompound data )
	{
		this.burnTime = data.getDouble( "burnTime" );
		this.maxBurnTime = data.getDouble( "maxBurnTime" );
		this.burnSpeed = data.getInteger( "burnSpeed" );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return TileEntityFurnace.getItemBurnTime( itemstack ) > 0;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added )
	{
		if( this.burnTime <= 0 )
		{
			if( this.canEatFuel() )
			{
				try
				{
					this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
				}
				catch( GridAccessException e )
				{
					// wake up!
				}
			}
		}
	}

	@Override
	public boolean canExtractItem( int slotIndex, ItemStack extractedItem, EnumFacing side )
	{
		return false;
	}

	@Override
	public int[] getAccessibleSlotsBySide( EnumFacing side )
	{
		return ACCESSIBLE_SLOTS;
	}

	private boolean canEatFuel()
	{
		ItemStack is = this.getStackInSlot( FUEL_SLOT_INDEX );
		if( is != null )
		{
			int newBurnTime = TileEntityFurnace.getItemBurnTime( is );
			if( newBurnTime > 0 && is.stackSize > 0 )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		if( this.burnTime <= 0 )
		{
			this.eatFuel();
		}

		return new TickingRequest( TickRates.VibrationChamber.min, TickRates.VibrationChamber.max, this.burnTime <= 0, false );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int ticksSinceLastCall )
	{
		if( this.burnTime <= 0 )
		{
			this.eatFuel();

			if( this.burnTime > 0 )
			{
				return TickRateModulation.URGENT;
			}

			this.burnSpeed = 100;
			return TickRateModulation.SLEEP;
		}

		this.burnSpeed = Math.max( MIN_BURN_SPEED, Math.min( this.burnSpeed, MAX_BURN_SPEED ) );
		double dilation = this.burnSpeed / DILATION_SCALING;

		double timePassed = ticksSinceLastCall * dilation;
		this.burnTime -= timePassed;
		if( this.burnTime < 0 )
		{
			timePassed += this.burnTime;
			this.burnTime = 0;
		}

		try
		{
			IEnergyGrid grid = this.gridProxy.getEnergy();
			double newPower = timePassed * POWER_PER_TICK;
			double overFlow = grid.injectPower( newPower, Actionable.SIMULATE );

			// burn the over flow.
			grid.injectPower( Math.max( 0.0, newPower - overFlow ), Actionable.MODULATE );

			if( overFlow > 0 )
			{
				this.burnSpeed -= ticksSinceLastCall;
			}
			else
			{
				this.burnSpeed += ticksSinceLastCall;
			}

			this.burnSpeed = Math.max( MIN_BURN_SPEED, Math.min( this.burnSpeed, MAX_BURN_SPEED ) );
			return overFlow > 0 ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
		}
		catch( GridAccessException e )
		{
			this.burnSpeed -= ticksSinceLastCall;
			this.burnSpeed = Math.max( MIN_BURN_SPEED, Math.min( this.burnSpeed, MAX_BURN_SPEED ) );
			return TickRateModulation.SLOWER;
		}
	}

	private void eatFuel()
	{
		final ItemStack is = this.getStackInSlot( FUEL_SLOT_INDEX );
		if( is != null )
		{
			final int newBurnTime = TileEntityFurnace.getItemBurnTime( is );
			if( newBurnTime > 0 && is.stackSize > 0 )
			{
				this.burnTime += newBurnTime;
				this.maxBurnTime = this.burnTime;
				is.stackSize--;
				if( is.stackSize <= 0 )
				{
					ItemStack container = null;

					if( is.getItem() != null && is.getItem().hasContainerItem( is ) )
					{
						container = is.getItem().getContainerItem( is );
					}

					this.setInventorySlotContents( 0, container );
				}
				else
				{
					this.setInventorySlotContents( 0, is );
				}

				this.markDirty();
			}
		}

		if( this.burnTime > 0 )
		{
			try
			{
				this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
			}
			catch( GridAccessException e )
			{
				// gah!
			}
		}

		// state change
		if( ( !this.isOn && this.burnTime > 0 ) || ( this.isOn && this.burnTime <= 0 ) )
		{
			this.isOn = this.burnTime > 0;
			this.markForUpdate();

			if ( this.hasWorldObj() )
			{
				Platform.notifyBlocksOfNeighbors( this.worldObj, pos );
			}
		}
	}
}
