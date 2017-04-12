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


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.tiles.ICrankable;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class TileCharger extends AENetworkPowerTile implements ICrankable
{

	private final int[] sides = { 0 };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );
	private int tickTickTimer = 0;

	private int lastUpdate = 0;
	private boolean requiresUpdate = false;

	public TileCharger()
	{
		this.getProxy().setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		this.getProxy().setFlags();
		this.setInternalMaxPower( 1500 );
		this.getProxy().setIdlePowerUsage( 0 );
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.COVERED;
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileCharger( final ByteBuf data )
	{
		try
		{
			final IAEItemStack item = AEItemStack.loadItemStackFromPacket( data );
			final ItemStack is = item.getItemStack();
			this.inv.setInventorySlotContents( 0, is );
		}
		catch( final Throwable t )
		{
			this.inv.setInventorySlotContents( 0, null );
		}
		return false; // TESR doesn't need updates!
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileCharger( final ByteBuf data ) throws IOException
	{
		final AEItemStack is = AEItemStack.create( this.getStackInSlot( 0 ) );
		if( is != null )
		{
			is.writeToPacket( data );
		}
	}

	@TileEvent( TileEventType.TICK )
	public void Tick_TileCharger()
	{
		if( this.lastUpdate > 60 && this.requiresUpdate )
		{
			this.requiresUpdate = false;
			this.markForUpdate();
			this.lastUpdate = 0;
		}
		this.lastUpdate++;

		this.tickTickTimer++;
		if( this.tickTickTimer < 20 )
		{
			return;
		}
		this.tickTickTimer = 0;

		final ItemStack myItem = this.getStackInSlot( 0 );

		// charge from the network!
		if( this.getInternalCurrentPower() < 1499 )
		{
			try
			{
				this.injectExternalPower( PowerUnits.AE, this.getProxy().getEnergy().extractAEPower( Math.min( 150.0, 1500.0 - this.getInternalCurrentPower() ), Actionable.MODULATE, PowerMultiplier.ONE ) );
				this.tickTickTimer = 20; // keep ticking...
			}
			catch( final GridAccessException e )
			{
				// continue!
			}
		}

		if( myItem == null )
		{
			return;
		}

		final IMaterials materials = AEApi.instance().definitions().materials();

		if( this.getInternalCurrentPower() > 149 && Platform.isChargeable( myItem ) )
		{
			final IAEItemPowerStorage ps = (IAEItemPowerStorage) myItem.getItem();
			if( ps.getAEMaxPower( myItem ) > ps.getAECurrentPower( myItem ) )
			{
				final double oldPower = this.getInternalCurrentPower();

				final double adjustment = ps.injectAEPower( myItem, this.extractAEPower( 150.0, Actionable.MODULATE, PowerMultiplier.CONFIG ) );
				this.setInternalCurrentPower( this.getInternalCurrentPower() + adjustment );
				if( oldPower > this.getInternalCurrentPower() )
				{
					this.requiresUpdate = true;
				}
				this.tickTickTimer = 20; // keep ticking...
			}
		}
		else if( this.getInternalCurrentPower() > 1499 && materials.certusQuartzCrystal().isSameAs( myItem ) )
		{
			if( Platform.getRandomFloat() > 0.8f ) // simulate wait
			{
				this.extractAEPower( this.getInternalMaxPower(), Actionable.MODULATE, PowerMultiplier.CONFIG );// 1500

				for( final ItemStack charged : materials.certusQuartzCrystalCharged().maybeStack( myItem.stackSize ).asSet() )
				{
					this.setInventorySlotContents( 0, charged );
				}
			}
		}
	}

	@Override
	public void setOrientation( final ForgeDirection inForward, final ForgeDirection inUp )
	{
		super.setOrientation( inForward, inUp );
		this.getProxy().setValidSides( EnumSet.of( this.getUp(), this.getUp().getOpposite() ) );
		this.setPowerSides( EnumSet.of( this.getUp(), this.getUp().getOpposite() ) );
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

	@Override
	public boolean canTurn()
	{
		return this.getInternalCurrentPower() < this.getInternalMaxPower();
	}

	@Override
	public void applyTurn()
	{
		this.injectExternalPower( PowerUnits.AE, 150 );

		final ItemStack myItem = this.getStackInSlot( 0 );
		if( this.getInternalCurrentPower() > 1499 )
		{
			final IMaterials materials = AEApi.instance().definitions().materials();

			if( materials.certusQuartzCrystal().isSameAs( myItem ) )
			{
				this.extractAEPower( this.getInternalMaxPower(), Actionable.MODULATE, PowerMultiplier.CONFIG );// 1500

				for( final ItemStack charged : materials.certusQuartzCrystalCharged().maybeStack( myItem.stackSize ).asSet() )
				{
					this.setInventorySlotContents( 0, charged );
				}
			}
		}
	}

	@Override
	public boolean canCrankAttach( final ForgeDirection directionToCrank )
	{
		return this.getUp() == directionToCrank || this.getUp().getOpposite() == directionToCrank;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		final IItemDefinition cert = AEApi.instance().definitions().materials().certusQuartzCrystal();

		return Platform.isChargeable( itemstack ) || cert.isSameAs( itemstack );
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		this.markForUpdate();
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final int side )
	{
		if( Platform.isChargeable( extractedItem ) )
		{
			final IAEItemPowerStorage ips = (IAEItemPowerStorage) extractedItem.getItem();
			if( ips.getAECurrentPower( extractedItem ) >= ips.getAEMaxPower( extractedItem ) )
			{
				return true;
			}
		}

		return AEApi.instance().definitions().materials().certusQuartzCrystalCharged().isSameAs( extractedItem );
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection whichSide )
	{
		return this.sides;
	}

	public void activate( final EntityPlayer player )
	{
		if( !Platform.hasPermissions( new DimensionalCoord( this ), player ) )
		{
			return;
		}

		final ItemStack myItem = this.getStackInSlot( 0 );
		if( myItem == null )
		{
			ItemStack held = player.inventory.getCurrentItem();

			if( AEApi.instance().definitions().materials().certusQuartzCrystal().isSameAs( held ) || Platform.isChargeable( held ) )
			{
				held = player.inventory.decrStackSize( player.inventory.currentItem, 1 );
				this.setInventorySlotContents( 0, held );
			}
		}
		else
		{
			final List<ItemStack> drops = new ArrayList<ItemStack>();
			drops.add( myItem );
			this.setInventorySlotContents( 0, null );
			Platform.spawnDrops( this.worldObj, this.xCoord + this.getForward().offsetX, this.yCoord + this.getForward().offsetY, this.zCoord + this.getForward().offsetZ, drops );
		}
	}
}
