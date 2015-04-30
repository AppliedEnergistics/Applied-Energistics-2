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

package appeng.parts.reporting;


import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;


public class PartTerminal extends PartMonitor implements ITerminalHost, IConfigManagerHost, IViewCellStorage, IAEAppEngInventory
{

	final IConfigManager cm = new ConfigManager( this );
	final AppEngInternalInventory viewCell = new AppEngInternalInventory( this, 5 );

	public PartTerminal( ItemStack is )
	{
		super( is, true );

		this.frontBright = CableBusTextures.PartTerminal_Bright;
		this.frontColored = CableBusTextures.PartTerminal_Colored;
		this.frontDark = CableBusTextures.PartTerminal_Dark;
		// frontSolid = CableBusTextures.PartTerminal_Solid;

		this.cm.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.cm.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		this.cm.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );
	}

	@Override
	public void getDrops( List<ItemStack> drops, boolean wrenched )
	{
		super.getDrops( drops, wrenched );

		for( ItemStack is : this.viewCell )
		{
			if( is != null )
			{
				drops.add( is );
			}
		}
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.cm;
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.cm.readFromNBT( data );
		this.viewCell.readFromNBT( data, "viewCell" );
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.cm.writeToNBT( data );
		this.viewCell.writeToNBT( data, "viewCell" );
	}

	@Override
	public boolean onPartActivate( EntityPlayer player, Vec3 pos )
	{
		if( !super.onPartActivate( player, pos ) )
		{
			if( !player.isSneaking() )
			{
				if( Platform.isClient() )
				{
					return true;
				}

				Platform.openGUI( player, this.getHost().getTile(), this.side, this.getGui( player ) );

				return true;
			}
		}
		return false;
	}

	public GuiBridge getGui( EntityPlayer player )
	{
		return GuiBridge.GUI_ME;
	}

	@Override
	public IMEMonitor getItemInventory()
	{
		try
		{
			return this.proxy.getStorage().getItemInventory();
		}
		catch( GridAccessException e )
		{
			// err nope?
		}
		return null;
	}

	@Override
	public IMEMonitor getFluidInventory()
	{
		try
		{
			return this.proxy.getStorage().getFluidInventory();
		}
		catch( GridAccessException e )
		{
			// err nope?
		}
		return null;
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{

	}

	@Override
	public IInventory getViewCellStorage()
	{
		return this.viewCell;
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{
		this.host.markForSave();
	}
}
