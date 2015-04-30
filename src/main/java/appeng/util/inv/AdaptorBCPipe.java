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

package appeng.util.inv;


import java.util.Iterator;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.FuzzyMode;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBC;
import appeng.util.InventoryAdaptor;
import appeng.util.iterators.NullIterator;


public class AdaptorBCPipe extends InventoryAdaptor
{

	private final IBC bc;
	private final TileEntity i;
	private final ForgeDirection d;

	public AdaptorBCPipe( TileEntity s, ForgeDirection dd )
	{
		this.bc = (IBC) AppEng.instance.getIntegration( IntegrationType.BC );
		if( this.bc != null )
		{
			if( this.bc.isPipe( s, dd ) )
			{
				this.i = s;
				this.d = dd;
				return;
			}
		}
		this.i = null;
		this.d = null;
	}

	@Override
	public ItemStack removeItems( int amount, ItemStack filter, IInventoryDestination destination )
	{
		return null;
	}

	@Override
	public ItemStack simulateRemove( int amount, ItemStack filter, IInventoryDestination destination )
	{
		return null;
	}

	@Override
	public ItemStack removeSimilarItems( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove( int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination )
	{
		return null;
	}

	@Override
	public ItemStack addItems( ItemStack toBeAdded )
	{
		if( this.i == null )
		{
			return toBeAdded;
		}
		if( toBeAdded == null )
		{
			return null;
		}
		if( toBeAdded.stackSize == 0 )
		{
			return null;
		}

		if( this.bc.addItemsToPipe( this.i, toBeAdded, this.d ) )
		{
			return null;
		}
		return toBeAdded;
	}

	@Override
	public ItemStack simulateAdd( ItemStack toBeSimulated )
	{
		if( this.i == null )
		{
			return toBeSimulated;
		}
		return null;
	}

	@Override
	public boolean containsItems()
	{
		return false;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new NullIterator<ItemSlot>();
	}
}
