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


import appeng.api.config.CopyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.util.IConfigManager;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;


public class TileCellWorkbench extends AEBaseTile implements IUpgradeableHost, IAEAppEngInventory, IConfigManagerHost
{

	private final AppEngInternalInventory cell = new AppEngInternalInventory( this, 1 );
	private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 63 );
	private final ConfigManager manager = new ConfigManager( this );

	private IInventory cacheUpgrades = null;
	private IInventory cacheConfig = null;
	private boolean locked = false;

	public TileCellWorkbench()
	{
		this.manager.registerSetting( Settings.COPY_MODE, CopyMode.CLEAR_ON_REMOVE );
		this.cell.setEnableClientEvents( true );
	}

	public IInventory getCellUpgradeInventory()
	{
		if( this.cacheUpgrades == null )
		{
			final ICellWorkbenchItem cell = this.getCell();
			if( cell == null )
			{
				return null;
			}

			final ItemStack is = this.cell.getStackInSlot( 0 );
			if( is == null )
			{
				return null;
			}

			final IInventory inv = cell.getUpgradesInventory( is );
			if( inv == null )
			{
				return null;
			}

			return this.cacheUpgrades = inv;
		}
		return this.cacheUpgrades;
	}

	public ICellWorkbenchItem getCell()
	{
		if( this.cell.getStackInSlot( 0 ) == null )
		{
			return null;
		}

		if( this.cell.getStackInSlot( 0 ).getItem() instanceof ICellWorkbenchItem )
		{
			return ( (ICellWorkbenchItem) this.cell.getStackInSlot( 0 ).getItem() );
		}

		return null;
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileCellWorkbench( final NBTTagCompound data )
	{
		this.cell.writeToNBT( data, "cell" );
		this.config.writeToNBT( data, "config" );
		this.manager.writeToNBT( data );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileCellWorkbench( final NBTTagCompound data )
	{
		this.cell.readFromNBT( data, "cell" );
		this.config.readFromNBT( data, "config" );
		this.manager.readFromNBT( data );
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "config" ) )
		{
			return this.config;
		}

		if( name.equals( "cell" ) )
		{
			return this.cell;
		}

		return null;
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return 0;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{
		if( inv == this.cell && !this.locked )
		{
			this.locked = true;

			this.cacheUpgrades = null;
			this.cacheConfig = null;

			final IInventory configInventory = this.getCellConfigInventory();
			if( configInventory != null )
			{
				boolean cellHasConfig = false;
				for( int x = 0; x < configInventory.getSizeInventory(); x++ )
				{
					if( configInventory.getStackInSlot( x ) != null )
					{
						cellHasConfig = true;
						break;
					}
				}

				if( cellHasConfig )
				{
					for( int x = 0; x < this.config.getSizeInventory(); x++ )
					{
						this.config.setInventorySlotContents( x, configInventory.getStackInSlot( x ) );
					}
				}
				else
				{
					for( int x = 0; x < this.config.getSizeInventory(); x++ )
					{
						configInventory.setInventorySlotContents( x, this.config.getStackInSlot( x ) );
					}

					configInventory.markDirty();
				}
			}
			else if( this.manager.getSetting( Settings.COPY_MODE ) == CopyMode.CLEAR_ON_REMOVE )
			{
				for( int x = 0; x < this.config.getSizeInventory(); x++ )
				{
					this.config.setInventorySlotContents( x, null );
				}

				this.markDirty();
			}

			this.locked = false;
		}
		else if( inv == this.config && !this.locked )
		{
			final IInventory c = this.getCellConfigInventory();
			if( c != null )
			{
				for( int x = 0; x < this.config.getSizeInventory(); x++ )
				{
					c.setInventorySlotContents( x, this.config.getStackInSlot( x ) );
				}

				c.markDirty();
			}
		}
	}

	private IInventory getCellConfigInventory()
	{
		if( this.cacheConfig == null )
		{
			final ICellWorkbenchItem cell = this.getCell();
			if( cell == null )
			{
				return null;
			}

			final ItemStack is = this.cell.getStackInSlot( 0 );
			if( is == null )
			{
				return null;
			}

			final IInventory inv = cell.getConfigInventory( is );
			if( inv == null )
			{
				return null;
			}

			this.cacheConfig = inv;
		}
		return this.cacheConfig;
	}

	@Override
	public void getDrops( final World w, final int x, final int y, final int z, final List<ItemStack> drops )
	{
		super.getDrops( w, x, y, z, drops );

		if( this.cell.getStackInSlot( 0 ) != null )
		{
			drops.add( this.cell.getStackInSlot( 0 ) );
		}
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.manager;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
		// nothing here..
	}
}
