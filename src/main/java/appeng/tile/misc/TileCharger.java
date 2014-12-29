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

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
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

public class TileCharger extends AENetworkPowerTile implements ICrankable
{

	final int sides[] = new int[] { 0 };
	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 1 );
	int tickTickTimer = 0;

	int lastUpdate = 0;
	boolean requiresUpdate = false;

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileCharger(ByteBuf data)
	{
		try
		{
			IAEItemStack item = AEItemStack.loadItemStackFromPacket( data );
			ItemStack is = item.getItemStack();
			this.inv.setInventorySlotContents( 0, is );
		}
		catch (Throwable t)
		{
			this.inv.setInventorySlotContents( 0, null );
		}
		return false; // TESR doesn't need updates!
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileCharger(ByteBuf data) throws IOException
	{
		AEItemStack is = AEItemStack.create( this.getStackInSlot( 0 ) );
		if ( is != null )
			is.writeToPacket( data );
	}

	@TileEvent(TileEventType.TICK)
	public void Tick_TileCharger()
	{
		if ( this.lastUpdate > 60 && this.requiresUpdate )
		{
			this.requiresUpdate = false;
			this.markForUpdate();
			this.lastUpdate = 0;
		}
		this.lastUpdate++;

		this.tickTickTimer++;
		if ( this.tickTickTimer < 20 )
			return;
		this.tickTickTimer = 0;

		ItemStack myItem = this.getStackInSlot( 0 );

		// charge from the network!
		if ( this.internalCurrentPower < 1499 )
		{
			try
			{
				this.injectExternalPower( PowerUnits.AE,
						this.gridProxy.getEnergy().extractAEPower( Math.min( 150.0, 1500.0 - this.internalCurrentPower ), Actionable.MODULATE, PowerMultiplier.ONE ) );
				this.tickTickTimer = 20; // keep ticking...
			}
			catch (GridAccessException e)
			{
				// continue!
			}
		}

		if ( myItem == null )
			return;

		if ( this.internalCurrentPower > 149 && Platform.isChargeable( myItem ) )
		{
			IAEItemPowerStorage ps = (IAEItemPowerStorage) myItem.getItem();
			if ( ps.getAEMaxPower( myItem ) > ps.getAECurrentPower( myItem ) )
			{
				double oldPower = this.internalCurrentPower;

				double adjustment = ps.injectAEPower( myItem, this.extractAEPower( 150.0, Actionable.MODULATE, PowerMultiplier.CONFIG ) );
				this.internalCurrentPower += adjustment;
				if ( oldPower > this.internalCurrentPower )
					this.requiresUpdate = true;
				this.tickTickTimer = 20; // keep ticking...
			}
		}
		else if ( this.internalCurrentPower > 1499 && AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack( myItem ) )
		{
			if ( Platform.getRandomFloat() > 0.8f ) // simulate wait
			{
				this.extractAEPower( this.internalMaxPower, Actionable.MODULATE, PowerMultiplier.CONFIG );// 1500
				this.setInventorySlotContents( 0, AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( myItem.stackSize ) );
			}
		}
	}

	public TileCharger() {
		this.gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		this.gridProxy.setFlags();
		this.internalMaxPower = 1500;
		this.gridProxy.setIdlePowerUsage( 0 );
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		this.gridProxy.setValidSides( EnumSet.of( this.getUp(), this.getUp().getOpposite() ) );
		this.setPowerSides( EnumSet.of( this.getUp(), this.getUp().getOpposite() ) );
	}

	@Override
	public boolean canTurn()
	{
		return this.internalCurrentPower < this.internalMaxPower;
	}

	@Override
	public void applyTurn()
	{
		this.injectExternalPower( PowerUnits.AE, 150 );

		ItemStack myItem = this.getStackInSlot( 0 );
		if ( this.internalCurrentPower > 1499 && AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack( myItem ) )
		{
			this.extractAEPower( this.internalMaxPower, Actionable.MODULATE, PowerMultiplier.CONFIG );// 1500
			this.setInventorySlotContents( 0, AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( myItem.stackSize ) );
		}
	}

	@Override
	public boolean canCrankAttach(ForgeDirection directionToCrank)
	{
		return this.getUp().equals( directionToCrank ) || this.getUp().getOpposite().equals( directionToCrank );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		this.markForUpdate();
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection whichSide)
	{
		return this.sides;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return Platform.isChargeable( itemstack ) || AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack( itemstack );
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		if ( Platform.isChargeable( itemstack ) )
		{
			IAEItemPowerStorage ips = (IAEItemPowerStorage) itemstack.getItem();
			if ( ips.getAECurrentPower( itemstack ) >= ips.getAEMaxPower( itemstack ) )
				return true;
		}

		return AEApi.instance().materials().materialCertusQuartzCrystalCharged.sameAsStack( itemstack );
	}

	public void activate(EntityPlayer player)
	{
		if ( !Platform.hasPermissions( new DimensionalCoord( this ), player ) )
			return;

		ItemStack myItem = this.getStackInSlot( 0 );
		if ( myItem == null )
		{
			ItemStack held = player.inventory.getCurrentItem();
			if ( AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack( held ) || Platform.isChargeable( held ) )
			{
				held = player.inventory.decrStackSize( player.inventory.currentItem, 1 );
				this.setInventorySlotContents( 0, held );
			}
		}
		else
		{
			List<ItemStack> drops = new ArrayList<ItemStack>();
			drops.add( myItem );
			this.setInventorySlotContents( 0, null );
			Platform.spawnDrops( this.worldObj, this.xCoord + this.getForward().offsetX, this.yCoord + this.getForward().offsetY, this.zCoord + this.getForward().offsetZ, drops );
		}
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

}
