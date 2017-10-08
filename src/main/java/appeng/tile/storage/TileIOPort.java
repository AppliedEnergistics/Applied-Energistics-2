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


import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

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
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.automation.BlockUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.AEItemFilters;


public class TileIOPort extends AENetworkInvTile implements IUpgradeableHost, IConfigManagerHost, IGridTickable
{
	private final ConfigManager manager;

	private final AppEngInternalInventory inputCells = new AppEngInternalInventory( this, 6 );
	private final AppEngInternalInventory outputCells = new AppEngInternalInventory( this, 6 );
	private final IItemHandler combinedInventory = new WrapperChainedItemHandler( this.inputCells, this.outputCells );

	private final IItemHandler inputCellsExt = new WrapperFilteredItemHandler( this.inputCells, AEItemFilters.INSERT_ONLY );
	private final IItemHandler outputCellsExt = new WrapperFilteredItemHandler( this.outputCells, AEItemFilters.EXTRACT_ONLY );

	private final UpgradeInventory upgrades;
	private final IActionSource mySrc;
	private YesNo lastRedstoneState;
	private ItemStack currentCell;
	private IMEInventory<IAEFluidStack> cachedFluid;
	private IMEInventory<IAEItemStack> cachedItem;

	public TileIOPort()
	{
		this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );
		this.manager = new ConfigManager( this );
		this.manager.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.manager.registerSetting( Settings.FULLNESS_MODE, FullnessMode.EMPTY );
		this.manager.registerSetting( Settings.OPERATION_MODE, OperationMode.EMPTY );
		this.mySrc = new MachineSource( this );
		this.lastRedstoneState = YesNo.UNDECIDED;

		final Block ioPortBlock = AEApi.instance().definitions().blocks().iOPort().maybeBlock().get();
		this.upgrades = new BlockUpgradeInventory( ioPortBlock, this, 3 );
	}

	@Override
	public NBTTagCompound writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.manager.writeToNBT( data );
		this.upgrades.writeToNBT( data, "upgrades" );
		data.setInteger( "lastRedstoneState", this.lastRedstoneState.ordinal() );
		return data;
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.manager.readFromNBT( data );
		this.upgrades.readFromNBT( data, "upgrades" );
		if( data.hasKey( "lastRedstoneState" ) )
		{
			this.lastRedstoneState = YesNo.values()[data.getInteger( "lastRedstoneState" )];
		}
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	private void updateTask()
	{
		try
		{
			if( this.hasWork() )
			{
				this.getProxy().getTick().wakeDevice( this.getProxy().getNode() );
			}
			else
			{
				this.getProxy().getTick().sleepDevice( this.getProxy().getNode() );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	public void updateRedstoneState()
	{
		final YesNo currentState = this.world.isBlockIndirectlyGettingPowered( this.pos ) != 0 ? YesNo.YES : YesNo.NO;
		if( this.lastRedstoneState != currentState )
		{
			this.lastRedstoneState = currentState;
			this.updateTask();
		}
	}

	private boolean getRedstoneState()
	{
		if( this.lastRedstoneState == YesNo.UNDECIDED )
		{
			this.updateRedstoneState();
		}

		return this.lastRedstoneState == YesNo.YES;
	}

	private boolean isEnabled()
	{
		if( this.getInstalledUpgrades( Upgrades.REDSTONE ) == 0 )
		{
			return true;
		}

		final RedstoneMode rs = (RedstoneMode) this.manager.getSetting( Settings.REDSTONE_CONTROLLED );
		if( rs == RedstoneMode.HIGH_SIGNAL )
		{
			return this.getRedstoneState();
		}
		return !this.getRedstoneState();
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.manager;
	}

	@Override
	public IItemHandler getInventoryByName( final String name )
	{
		if( name.equals( "upgrades" ) )
		{
			return this.upgrades;
		}

		if( name.equals( "cells" ) )
		{
			return this.combinedInventory;
		}

		return null;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
		this.updateTask();
	}

	private boolean hasWork()
	{
		if( this.isEnabled() )
		{
			return !ItemHandlerUtil.isEmpty( this.inputCells );
		}

		return false;
	}

	@Override
	public IItemHandler getInternalInventory()
	{
		return this.combinedInventory;
	}

	@Override
	public void onChangeInventory( final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( this.inputCells == inv )
		{
			this.updateTask();
		}
	}

	@Override
	protected IItemHandler getItemHandlerForSide( final EnumFacing facing )
	{
		if( facing == this.getUp() || facing == this.getUp().getOpposite() )
		{
			return this.inputCellsExt;
		}
		else
		{
			return this.outputCellsExt;
		}
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.IOPort.getMin(), TickRates.IOPort.getMax(), this.hasWork(), false );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		if( !this.getProxy().isActive() )
		{
			return TickRateModulation.IDLE;
		}

		long ItemsToMove = 256;

		switch( this.getInstalledUpgrades( Upgrades.SPEED ) )
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
			final IMEInventory<IAEItemStack> itemNet = this.getProxy().getStorage().getInventory(
					AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );
			final IMEInventory<IAEFluidStack> fluidNet = this.getProxy().getStorage().getInventory(
					AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
			final IEnergySource energy = this.getProxy().getEnergy();
			for( int x = 0; x < 6; x++ )
			{
				final ItemStack is = this.inputCells.getStackInSlot( x );
				if( !is.isEmpty() )
				{
					if( ItemsToMove > 0 )
					{
						final IMEInventory<IAEItemStack> itemInv = this.getInv( is, AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );
						final IMEInventory<IAEFluidStack> fluidInv = this.getInv( is,
								AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );

						if( this.manager.getSetting( Settings.OPERATION_MODE ) == OperationMode.EMPTY )
						{
							if( itemInv != null )
							{
								ItemsToMove = this.transferContents( energy, itemInv, itemNet, ItemsToMove,
										AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );
							}
							if( fluidInv != null )
							{
								ItemsToMove = this.transferContents( energy, fluidInv, fluidNet, ItemsToMove,
										AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
							}
						}
						else
						{
							if( itemInv != null )
							{
								ItemsToMove = this.transferContents( energy, itemNet, itemInv, ItemsToMove,
										AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );
							}
							if( fluidInv != null )
							{
								ItemsToMove = this.transferContents( energy, fluidNet, fluidInv, ItemsToMove,
										AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
							}
						}

						if( ItemsToMove > 0 && this.shouldMove( itemInv, fluidInv ) && !this.moveSlot( x ) )
						{
							return TickRateModulation.IDLE;
						}

						return TickRateModulation.URGENT;
					}
					else
					{
						return TickRateModulation.URGENT;
					}
				}
			}
		}
		catch( final GridAccessException e )
		{
			return TickRateModulation.IDLE;
		}

		// nothing left to do...
		return TickRateModulation.SLEEP;
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return this.upgrades.getInstalledUpgrades( u );
	}

	private IMEInventory getInv( final ItemStack is, final IStorageChannel chan )
	{
		if( this.currentCell != is )
		{
			this.currentCell = is;
			this.cachedFluid = AEApi.instance().registries().cell().getCellInventory( is, null,
					AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
			this.cachedItem = AEApi.instance().registries().cell().getCellInventory( is, null,
					AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );
		}

		if( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) == chan )
		{
			return this.cachedItem;
		}

		return this.cachedFluid;
	}

	private long transferContents( final IEnergySource energy, final IMEInventory src, final IMEInventory destination, long itemsToMove, final IStorageChannel chan )
	{
		final IItemList<? extends IAEStack> myList;
		if( src instanceof IMEMonitor )
		{
			myList = ( (IMEMonitor) src ).getStorageList();
		}
		else
		{
			myList = src.getAvailableItems( src.getChannel().createList() );
		}

		boolean didStuff;

		do
		{
			didStuff = false;

			for( final IAEStack s : myList )
			{
				final long totalStackSize = s.getStackSize();
				if( totalStackSize > 0 )
				{
					final IAEStack stack = destination.injectItems( s, Actionable.SIMULATE, this.mySrc );

					long possible = 0;
					if( stack == null )
					{
						possible = totalStackSize;
					}
					else
					{
						possible = totalStackSize - stack.getStackSize();
					}

					if( possible > 0 )
					{
						possible = Math.min( possible, itemsToMove );
						s.setStackSize( possible );

						final IAEStack extracted = src.extractItems( s, Actionable.MODULATE, this.mySrc );
						if( extracted != null )
						{
							possible = extracted.getStackSize();
							final IAEStack failed = Platform.poweredInsert( energy, destination, extracted, this.mySrc );

							if( failed != null )
							{
								possible -= failed.getStackSize();
								src.injectItems( failed, Actionable.MODULATE, this.mySrc );
							}

							if( possible > 0 )
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
		while( itemsToMove > 0 && didStuff );

		return itemsToMove;
	}

	private boolean shouldMove( final IMEInventory<IAEItemStack> itemInv, final IMEInventory<IAEFluidStack> fluidInv )
	{
		final FullnessMode fm = (FullnessMode) this.manager.getSetting( Settings.FULLNESS_MODE );

		if( itemInv != null && fluidInv != null )
		{
			return this.matches( fm, itemInv ) && this.matches( fm, fluidInv );
		}
		else if( itemInv != null )
		{
			return this.matches( fm, itemInv );
		}
		else if( fluidInv != null )
		{
			return this.matches( fm, fluidInv );
		}

		return true;
	}

	private boolean moveSlot( final int x )
	{
		final InventoryAdaptor ad = new AdaptorItemHandler( this.outputCells );
		if( ad.addItems( this.inputCells.getStackInSlot( x ) ).isEmpty() )
		{
			this.inputCells.setStackInSlot( x, ItemStack.EMPTY );
			return true;
		}
		return false;
	}

	private boolean matches( final FullnessMode fm, final IMEInventory src )
	{
		if( fm == FullnessMode.HALF )
		{
			return true;
		}

		final IItemList<? extends IAEStack> myList;

		if( src instanceof IMEMonitor )
		{
			myList = ( (IMEMonitor) src ).getStorageList();
		}
		else
		{
			myList = src.getAvailableItems( src.getChannel().createList() );
		}

		if( fm == FullnessMode.EMPTY )
		{
			return myList.isEmpty();
		}

		final IAEStack test = myList.getFirstItem();
		if( test != null )
		{
			test.setStackSize( 1 );
			return src.injectItems( test, Actionable.SIMULATE, this.mySrc ) != null;
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
	public void getDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		super.getDrops( w, pos, drops );

		for( int upgradeIndex = 0; upgradeIndex < this.upgrades.getSlots(); upgradeIndex++ )
		{
			final ItemStack stackInSlot = this.upgrades.getStackInSlot( upgradeIndex );

			if( !stackInSlot.isEmpty() )
			{
				drops.add( stackInSlot );
			}
		}
	}
}
