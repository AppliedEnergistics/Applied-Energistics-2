/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.me.storage;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;


public final class CellInventoryHandler extends MEInventoryHandler<IAEItemStack> implements ICellInventoryHandler
{

	CellInventoryHandler( IMEInventory c )
	{
		super( c, StorageChannel.ITEMS );

		ICellInventory ci = this.getCellInv();
		if( ci != null )
		{
			IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

			IInventory upgrades = ci.getUpgradesInventory();
			IInventory config = ci.getConfigInventory();
			FuzzyMode fzMode = ci.getFuzzyMode();

			boolean hasInverter = false;
			boolean hasFuzzy = false;

			for( int x = 0; x < upgrades.getSizeInventory(); x++ )
			{
				ItemStack is = upgrades.getStackInSlot( x );
				if( is != null && is.getItem() instanceof IUpgradeModule )
				{
					Upgrades u = ( (IUpgradeModule) is.getItem() ).getType( is );
					if( u != null )
					{
						switch( u )
						{
							case FUZZY:
								hasFuzzy = true;
								break;
							case INVERTER:
								hasInverter = true;
								break;
							default:
						}
					}
				}
			}

			for( int x = 0; x < config.getSizeInventory(); x++ )
			{
				ItemStack is = config.getStackInSlot( x );
				if( is != null )
				{
					priorityList.add( AEItemStack.create( is ) );
				}
			}

			this.setWhitelist( hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST );

			if( !priorityList.isEmpty() )
			{
				if( hasFuzzy )
				{
					this.setPartitionList( new FuzzyPriorityList<IAEItemStack>( priorityList, fzMode ) );
				}
				else
				{
					this.setPartitionList( new PrecisePriorityList<IAEItemStack>( priorityList ) );
				}
			}
		}
	}

	@Override
	public final ICellInventory getCellInv()
	{
		Object o = this.internal;

		if( o instanceof MEPassThrough )
		{
			o = ( (MEPassThrough) o ).getInternal();
		}

		return (ICellInventory) ( o instanceof ICellInventory ? o : null );
	}

	@Override
	public final boolean isPreformatted()
	{
		return !this.getPartitionList().isEmpty();
	}

	@Override
	public final boolean isFuzzy()
	{
		return this.getPartitionList() instanceof FuzzyPriorityList;
	}

	@Override
	public final IncludeExclude getIncludeExcludeMode()
	{
		return this.getWhitelist();
	}

	NBTTagCompound openNbtData()
	{
		return Platform.openNbtData( this.getCellInv().getItemStack() );
	}

	public final int getStatusForCell()
	{
		int val = this.getCellInv().getStatusForCell();

		if( val == 1 && this.isPreformatted() )
		{
			val = 2;
		}

		return val;
	}
}
