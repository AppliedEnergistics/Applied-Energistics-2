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


import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import java.util.List;


/**
 * Anything resembling an network terminal with view cells can reuse this.
 * <p>
 * Note this applies only to terminals like the ME Terminal. It does not apply for more specialized terminals like the
 * Interface Terminal.
 *
 * @author AlgorithmX2
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractPartTerminal extends AbstractPartDisplay implements ITerminalHost, IConfigManagerHost, IViewCellStorage, IAEAppEngInventory
{

	private final IConfigManager cm = new ConfigManager( this );
	private final AppEngInternalInventory viewCell = new AppEngInternalInventory( this, 5 );

	public AbstractPartTerminal( final ItemStack is )
	{
		super( is );

		this.cm.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.cm.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		this.cm.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
		super.getDrops( drops, wrenched );

		for( final ItemStack is : this.viewCell )
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
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.cm.readFromNBT( data );
		this.viewCell.readFromNBT( data, "viewCell" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.cm.writeToNBT( data );
		this.viewCell.writeToNBT( data, "viewCell" );
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final Vec3 pos )
	{
		if( !super.onPartActivate( player, pos ) )
		{
			if( !player.isSneaking() )
			{
				if( Platform.isClient() )
				{
					return true;
				}

				Platform.openGUI( player, this.getHost().getTile(), this.getSide(), this.getGui( player ) );

				return true;
			}
		}
		return false;
	}

	public GuiBridge getGui( final EntityPlayer player )
	{
		return GuiBridge.GUI_ME;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		try
		{
			return this.getProxy().getStorage().getItemInventory();
		}
		catch( final GridAccessException e )
		{
			// err nope?
		}
		return null;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		try
		{
			return this.getProxy().getStorage().getFluidInventory();
		}
		catch( final GridAccessException e )
		{
			// err nope?
		}
		return null;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{

	}

	@Override
	public IInventory getViewCellStorage()
	{
		return this.viewCell;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{
		this.getHost().markForSave();
	}
}
