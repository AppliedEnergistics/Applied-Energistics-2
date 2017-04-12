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


import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
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
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;


public class TileVibrationChamber extends AENetworkInvTile implements IGridTickable
{
	private static final int FUEL_SLOT_INDEX = 0;
	private static final double POWER_PER_TICK = 5;
	private static final int[] ACCESSIBLE_SLOTS = { FUEL_SLOT_INDEX };
	private static final int MAX_BURN_SPEED = 200;
	private static final double DILATION_SCALING = 100.0;
	private static final int MIN_BURN_SPEED = 20;
	private final IInventory inv = new AppEngInternalInventory( this, 1 );

	private int burnSpeed = 100;
	private double burnTime = 0;
	private double maxBurnTime = 0;

	// client side..
	public boolean isOn;

	public TileVibrationChamber()
	{
		this.getProxy().setIdlePowerUsage( 0 );
		this.getProxy().setFlags();
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.COVERED;
	}

	@Reflected
	@TileEvent( TileEventType.NETWORK_READ )
	public boolean hasUpdate( final ByteBuf data )
	{
		final boolean wasOn = this.isOn;

		this.isOn = data.readBoolean();

		return wasOn != this.isOn; // TESR doesn't need updates!
	}

	@Reflected
	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToNetwork( final ByteBuf data )
	{
		data.writeBoolean( this.getBurnTime() > 0 );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileVibrationChamber( final NBTTagCompound data )
	{
		data.setDouble( "burnTime", this.getBurnTime() );
		data.setDouble( "maxBurnTime", this.getMaxBurnTime() );
		data.setInteger( "burnSpeed", this.getBurnSpeed() );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileVibrationChamber( final NBTTagCompound data )
	{
		this.setBurnTime( data.getDouble( "burnTime" ) );
		this.setMaxBurnTime( data.getDouble( "maxBurnTime" ) );
		this.setBurnSpeed( data.getInteger( "burnSpeed" ) );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return TileEntityFurnace.getItemBurnTime( itemstack ) > 0;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( this.getBurnTime() <= 0 )
		{
			if( this.canEatFuel() )
			{
				try
				{
					this.getProxy().getTick().wakeDevice( this.getProxy().getNode() );
				}
				catch( final GridAccessException e )
				{
					// wake up!
				}
			}
		}
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final int side )
	{
		return extractedItem.getItem() == Items.bucket;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection side )
	{
		return ACCESSIBLE_SLOTS;
	}

	private boolean canEatFuel()
	{
		final ItemStack is = this.getStackInSlot( FUEL_SLOT_INDEX );
		if( is != null )
		{
			final int newBurnTime = TileEntityFurnace.getItemBurnTime( is );
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
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		if( this.getBurnTime() <= 0 )
		{
			this.eatFuel();
		}

		return new TickingRequest( TickRates.VibrationChamber.getMin(), TickRates.VibrationChamber.getMax(), this.getBurnTime() <= 0, false );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		if( this.getBurnTime() <= 0 )
		{
			this.eatFuel();

			if( this.getBurnTime() > 0 )
			{
				return TickRateModulation.URGENT;
			}

			this.setBurnSpeed( 100 );
			return TickRateModulation.SLEEP;
		}

		this.setBurnSpeed( Math.max( MIN_BURN_SPEED, Math.min( this.getBurnSpeed(), MAX_BURN_SPEED ) ) );
		final double dilation = this.getBurnSpeed() / DILATION_SCALING;

		double timePassed = ticksSinceLastCall * dilation;
		this.setBurnTime( this.getBurnTime() - timePassed );
		if( this.getBurnTime() < 0 )
		{
			timePassed += this.getBurnTime();
			this.setBurnTime( 0 );
		}

		try
		{
			final IEnergyGrid grid = this.getProxy().getEnergy();
			final double newPower = timePassed * POWER_PER_TICK;
			final double overFlow = grid.injectPower( newPower, Actionable.SIMULATE );

			// burn the over flow.
			grid.injectPower( Math.max( 0.0, newPower - overFlow ), Actionable.MODULATE );

			if( overFlow > 0 )
			{
				this.setBurnSpeed( this.getBurnSpeed() - ticksSinceLastCall );
			}
			else
			{
				this.setBurnSpeed( this.getBurnSpeed() + ticksSinceLastCall );
			}

			this.setBurnSpeed( Math.max( MIN_BURN_SPEED, Math.min( this.getBurnSpeed(), MAX_BURN_SPEED ) ) );
			return overFlow > 0 ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
		}
		catch( final GridAccessException e )
		{
			this.setBurnSpeed( this.getBurnSpeed() - ticksSinceLastCall );
			this.setBurnSpeed( Math.max( MIN_BURN_SPEED, Math.min( this.getBurnSpeed(), MAX_BURN_SPEED ) ) );
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
				this.setBurnTime( this.getBurnTime() + newBurnTime );
				this.setMaxBurnTime( this.getBurnTime() );
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

		if( this.getBurnTime() > 0 )
		{
			try
			{
				this.getProxy().getTick().wakeDevice( this.getProxy().getNode() );
			}
			catch( final GridAccessException e )
			{
				// gah!
			}
		}

		// state change
		if( ( !this.isOn && this.getBurnTime() > 0 ) || ( this.isOn && this.getBurnTime() <= 0 ) )
		{
			this.isOn = this.getBurnTime() > 0;
			this.markForUpdate();

			if( this.hasWorldObj() )
			{
				Platform.notifyBlocksOfNeighbors( this.worldObj, this.xCoord, this.yCoord, this.zCoord );
			}
		}
	}

	public int getBurnSpeed()
	{
		return this.burnSpeed;
	}

	private void setBurnSpeed( final int burnSpeed )
	{
		this.burnSpeed = burnSpeed;
	}

	public double getMaxBurnTime()
	{
		return this.maxBurnTime;
	}

	private void setMaxBurnTime( final double maxBurnTime )
	{
		this.maxBurnTime = maxBurnTime;
	}

	public double getBurnTime()
	{
		return this.burnTime;
	}

	private void setBurnTime( final double burnTime )
	{
		this.burnTime = burnTime;
	}
}
