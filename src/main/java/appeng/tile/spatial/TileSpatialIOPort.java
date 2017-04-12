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

package appeng.tile.spatial;


import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.YesNo;
import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkSpatialEvent;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.hooks.TickHandler;
import appeng.me.cache.SpatialPylonCache;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;


public class TileSpatialIOPort extends AENetworkInvTile implements IWorldCallable<Void>
{

	private final int[] sides = { 0, 1 };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 2 );
	private YesNo lastRedstoneState = YesNo.UNDECIDED;

	public TileSpatialIOPort()
	{
		this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileSpatialIOPort( final NBTTagCompound data )
	{
		data.setInteger( "lastRedstoneState", this.lastRedstoneState.ordinal() );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileSpatialIOPort( final NBTTagCompound data )
	{
		if( data.hasKey( "lastRedstoneState" ) )
		{
			this.lastRedstoneState = YesNo.values()[data.getInteger( "lastRedstoneState" )];
		}
	}

	public boolean getRedstoneState()
	{
		if( this.lastRedstoneState == YesNo.UNDECIDED )
		{
			this.updateRedstoneState();
		}

		return this.lastRedstoneState == YesNo.YES;
	}

	public void updateRedstoneState()
	{
		final YesNo currentState = this.worldObj.isBlockIndirectlyGettingPowered( this.xCoord, this.yCoord, this.zCoord ) ? YesNo.YES : YesNo.NO;
		if( this.lastRedstoneState != currentState )
		{
			this.lastRedstoneState = currentState;
			if( this.lastRedstoneState == YesNo.YES )
			{
				this.triggerTransition();
			}
		}
	}

	private void triggerTransition()
	{
		if( Platform.isServer() )
		{
			final ItemStack cell = this.getStackInSlot( 0 );
			if( this.isSpatialCell( cell ) )
			{
				TickHandler.INSTANCE.addCallable( null, this );// this needs to be cross world synced.
			}
		}
	}

	private boolean isSpatialCell( final ItemStack cell )
	{
		if( cell != null && cell.getItem() instanceof ISpatialStorageCell )
		{
			final ISpatialStorageCell sc = (ISpatialStorageCell) cell.getItem();
			return sc != null && sc.isSpatialStorage( cell );
		}
		return false;
	}

	@Override
	public Void call( final World world ) throws Exception
	{
		final ItemStack cell = this.getStackInSlot( 0 );
		if( this.isSpatialCell( cell ) && this.getStackInSlot( 1 ) == null )
		{
			final IGrid gi = this.getProxy().getGrid();
			final IEnergyGrid energy = this.getProxy().getEnergy();

			final ISpatialStorageCell sc = (ISpatialStorageCell) cell.getItem();

			final SpatialPylonCache spc = gi.getCache( ISpatialCache.class );
			if( spc.hasRegion() && spc.isValidRegion() )
			{
				final double req = spc.requiredPower();
				final double pr = energy.extractAEPower( req, Actionable.SIMULATE, PowerMultiplier.CONFIG );
				if( Math.abs( pr - req ) < req * 0.001 )
				{
					final MENetworkEvent res = gi.postEvent( new MENetworkSpatialEvent( this, req ) );
					if( !res.isCanceled() )
					{
						final TransitionResult tr = sc.doSpatialTransition( cell, this.worldObj, spc.getMin(), spc.getMax(), true );
						if( tr.success )
						{
							energy.extractAEPower( req, Actionable.MODULATE, PowerMultiplier.CONFIG );
							this.setInventorySlotContents( 0, null );
							this.setInventorySlotContents( 1, cell );
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return ( i == 0 && this.isSpatialCell( itemstack ) );
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{

	}

	@Override
	public boolean canInsertItem( final int slotIndex, final ItemStack insertingItem, final int side )
	{
		return this.isItemValidForSlot( slotIndex, insertingItem );
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final int side )
	{
		return slotIndex == 1;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection side )
	{
		return this.sides;
	}
}
