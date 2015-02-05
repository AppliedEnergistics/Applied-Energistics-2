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

package appeng.tile.storage;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.WrapperInventoryRange;

public class TileIOPort extends AENetworkInvTile implements IUpgradeableHost, IConfigManagerHost, IGridTickable
{

	final ConfigManager cm = new ConfigManager( this );

	final int[] input = { 0, 1, 2, 3, 4, 5 };
	final int[] output = { 6, 7, 8, 9, 10, 11 };

	final int[] outputSlots = { 6, 7, 8, 9, 10, 11 };

	final AppEngInternalInventory cells = new AppEngInternalInventory( this, 12 );
	final UpgradeInventory upgrades = new UpgradeInventory( AEApi.instance().definitions().blocks().iOPort().get().block(), this, 3 );

	final BaseActionSource mySrc = new MachineSource( this );

	YesNo lastRedstoneState = YesNo.UNDECIDED;

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileIOPort(NBTTagCompound data)
	{
		this.cm.writeToNBT( data );
		this.cells.writeToNBT( data, "cells" );
		this.upgrades.writeToNBT( data, "upgrades" );
		data.setInteger( "lastRedstoneState", this.lastRedstoneState.ordinal() );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileIOPort(NBTTagCompound data)
	{
		this.cm.readFromNBT( data );
		this.cells.readFromNBT( data, "cells" );
		this.upgrades.readFromNBT( data, "upgrades" );
		if ( data.hasKey( "lastRedstoneState" ) )
			this.lastRedstoneState = YesNo.values()[data.getInteger( "lastRedstoneState" )];
	}

	public TileIOPort() {
		this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		this.cm.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.cm.registerSetting( Settings.FULLNESS_MODE, FullnessMode.EMPTY );
		this.cm.registerSetting( Settings.OPERATION_MODE, OperationMode.EMPTY );
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
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
		return this.cells;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( this.cells == inv )
		{
			this.updateTask();
		}
	}

	private void updateTask()
	{
		try
		{
			if ( this.hasWork() )
				this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
			else
				this.gridProxy.getTick().sleepDevice( this.gridProxy.getNode() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	public void updateRedstoneState()
	{
		YesNo currentState = this.worldObj.isBlockIndirectlyGettingPowered( this.xCoord, this.yCoord, this.zCoord ) ? YesNo.YES : YesNo.NO;
		if ( this.lastRedstoneState != currentState )
		{
			this.lastRedstoneState = currentState;
			this.updateTask();
		}
	}

	public boolean getRedstoneState()
	{
		if ( this.lastRedstoneState == YesNo.UNDECIDED )
			this.updateRedstoneState();

		return this.lastRedstoneState == YesNo.YES;
	}

	private boolean isEnabled()
	{
		if ( this.getInstalledUpgrades( Upgrades.REDSTONE ) == 0 )
			return true;

		RedstoneMode rs = (RedstoneMode) this.cm.getSetting( Settings.REDSTONE_CONTROLLED );
		if ( rs == RedstoneMode.HIGH_SIGNAL )
			return this.getRedstoneState();
		return !this.getRedstoneState();
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection d)
	{
		if ( d == ForgeDirection.UP || d == ForgeDirection.DOWN )
			return this.input;

		return this.output;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.cm;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "upgrades" ) )
			return this.upgrades;

		if ( name.equals( "cells" ) )
			return this.cells;

		return null;
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return this.upgrades.getInstalledUpgrades( u );
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
		this.updateTask();
	}

	boolean hasWork()
	{
		if ( this.isEnabled() )
		{
			for (int x = 0; x < 6; x++)
				if ( this.cells.getStackInSlot( x ) != null )
					return true;
		}

		return false;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.IOPort.min, TickRates.IOPort.max, this.hasWork(), false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( !this.gridProxy.isActive() )
			return TickRateModulation.IDLE;

		long ItemsToMove = 256;

		switch (this.getInstalledUpgrades( Upgrades.SPEED ))
		{
		case 1:
			ItemsToMove *= 2;
			break;
		case 2:
			ItemsToMove *= 4;
			break;
		case 3:
			ItemsToMove *= 8;
			break;
		}

		try
		{
			IMEInventory<IAEItemStack> itemNet = this.gridProxy.getStorage().getItemInventory();
			IMEInventory<IAEFluidStack> fluidNet = this.gridProxy.getStorage().getFluidInventory();
			IEnergySource energy = this.gridProxy.getEnergy();
			for (int x = 0; x < 6; x++)
			{
				ItemStack is = this.cells.getStackInSlot( x );
				if ( is != null )
				{
					if ( ItemsToMove > 0 )
					{
						IMEInventory<IAEItemStack> itemInv = this.getInv( is, StorageChannel.ITEMS );
						IMEInventory<IAEFluidStack> fluidInv = this.getInv( is, StorageChannel.FLUIDS );

						if ( this.cm.getSetting( Settings.OPERATION_MODE ) == OperationMode.EMPTY )
						{
							if ( itemInv != null )
								ItemsToMove = this.transferContents( energy, itemInv, itemNet, ItemsToMove, StorageChannel.ITEMS );
							if ( fluidInv != null )
								ItemsToMove = this.transferContents( energy, fluidInv, fluidNet, ItemsToMove, StorageChannel.FLUIDS );
						}
						else
						{
							if ( itemInv != null )
								ItemsToMove = this.transferContents( energy, itemNet, itemInv, ItemsToMove, StorageChannel.ITEMS );
							if ( fluidInv != null )
								ItemsToMove = this.transferContents( energy, fluidNet, fluidInv, ItemsToMove, StorageChannel.FLUIDS );
						}

						if ( ItemsToMove > 0 && this.shouldMove( itemInv, fluidInv ) && !this.moveSlot( x ) )
							return TickRateModulation.IDLE;

						return TickRateModulation.URGENT;
					}
					else
						return TickRateModulation.URGENT;
				}
			}

		}
		catch (GridAccessException e)
		{
			return TickRateModulation.IDLE;
		}

		// nothing left to do...
		return TickRateModulation.SLEEP;
	}

	private boolean shouldMove(IMEInventory<IAEItemStack> itemInv, IMEInventory<IAEFluidStack> fluidInv)
	{
		FullnessMode fm = (FullnessMode) this.cm.getSetting( Settings.FULLNESS_MODE );

		if ( itemInv != null && fluidInv != null )
			return this.matches( fm, itemInv ) && this.matches( fm, fluidInv );
		else if ( itemInv != null )
			return this.matches( fm, itemInv );
		else if ( fluidInv != null )
			return this.matches( fm, fluidInv );

		return true;
	}

	private boolean matches(FullnessMode fm, IMEInventory src)
	{
		if ( fm == FullnessMode.HALF )
			return true;

		IItemList<? extends IAEStack> myList;

		if ( src instanceof IMEMonitor )
			myList = ((IMEMonitor) src).getStorageList();
		else
			myList = src.getAvailableItems( src.getChannel().createList() );

		if ( fm == FullnessMode.EMPTY )
			return myList.isEmpty();

		IAEStack test = myList.getFirstItem();
		if ( test != null )
		{
			test.setStackSize( 1 );
			return src.injectItems( test, Actionable.SIMULATE, this.mySrc ) != null;
		}
		return false;
	}

	ItemStack currentCell;
	IMEInventory<IAEFluidStack> cachedFluid;
	IMEInventory<IAEItemStack> cachedItem;

	private IMEInventory getInv(ItemStack is, StorageChannel chan)
	{
		if ( this.currentCell != is )
		{
			this.currentCell = is;
			this.cachedFluid = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.FLUIDS );
			this.cachedItem = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.ITEMS );
		}

		if ( StorageChannel.ITEMS == chan )
			return this.cachedItem;

		return this.cachedFluid;
	}

	private long transferContents(IEnergySource energy, IMEInventory src, IMEInventory destination, long itemsToMove, StorageChannel chan)
	{
		IItemList<? extends IAEStack> myList;
		if ( src instanceof IMEMonitor )
			myList = ((IMEMonitor) src).getStorageList();
		else
			myList = src.getAvailableItems( src.getChannel().createList() );

		boolean didStuff;

		do
		{
			didStuff = false;

			for (IAEStack s : myList)
			{
				long totalStackSize = s.getStackSize();
				if ( totalStackSize > 0 )
				{
					IAEStack stack = destination.injectItems( s, Actionable.SIMULATE, this.mySrc );

					long possible = 0;
					if ( stack == null )
						possible = totalStackSize;
					else
						possible = totalStackSize - stack.getStackSize();

					if ( possible > 0 )
					{
						possible = Math.min( possible, itemsToMove );
						s.setStackSize( possible );

						IAEStack extracted = src.extractItems( s, Actionable.MODULATE, this.mySrc );
						if ( extracted != null )
						{
							possible = extracted.getStackSize();
							IAEStack failed = Platform.poweredInsert( energy, destination, extracted, this.mySrc );

							if ( failed != null )
							{
								possible -= failed.getStackSize();
								src.injectItems( failed, Actionable.MODULATE, this.mySrc );
							}

							if ( possible > 0 )
							{
								itemsToMove -= possible;
								didStuff = true;
							}

							break;
						}
					}
				}
			}
		}
		while (itemsToMove > 0 && didStuff);

		return itemsToMove;
	}

	private boolean moveSlot(int x)
	{
		WrapperInventoryRange wir = new WrapperInventoryRange( this, this.outputSlots, true );
		ItemStack result = InventoryAdaptor.getAdaptor( wir, ForgeDirection.UNKNOWN ).addItems( this.getStackInSlot( x ) );

		if ( result == null )
		{
			this.setInventorySlotContents( x, null );
			return true;
		}

		return false;
	}

	/**
	 * Adds the items in the upgrade slots to the drop list.
	 *
	 * @param w world
	 * @param x x pos of tile entity
	 * @param y y pos of tile entity
	 * @param z z pos of tile entity
	 * @param drops drops of tile entity
	 */
	@Override
	public void getDrops( World w, int x, int y, int z, ArrayList<ItemStack> drops )
	{
		super.getDrops( w, x, y, z, drops );

		for (int upgradeIndex = 0; upgradeIndex < this.upgrades.getSizeInventory(); upgradeIndex++)
		{
			ItemStack stackInSlot = this.upgrades.getStackInSlot(upgradeIndex);

			if ( stackInSlot != null )
			{
				drops.add( stackInSlot );
			}
		}
	}
}
