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

package appeng.util.inv;


import appeng.api.config.FuzzyMode;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import appeng.util.InventoryAdaptor;
import appeng.util.iterators.NullIterator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Iterator;


public class AdaptorBCPipe extends InventoryAdaptor
{
	private final IBuildCraftTransport buildCraft;
	private final TileEntity i;
	private final ForgeDirection d;

	public AdaptorBCPipe( final TileEntity s, final ForgeDirection dd )
	{
		this.buildCraft = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
		{
			if( this.buildCraft.isPipe( s, dd ) )
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
	public ItemStack removeItems( final int amount, final ItemStack filter, final IInventoryDestination destination )
	{
		return null;
	}

	@Override
	public ItemStack simulateRemove( final int amount, final ItemStack filter, final IInventoryDestination destination )
	{
		return null;
	}

	@Override
	public ItemStack removeSimilarItems( final int amount, final ItemStack filter, final FuzzyMode fuzzyMode, final IInventoryDestination destination )
	{
		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove( final int amount, final ItemStack filter, final FuzzyMode fuzzyMode, final IInventoryDestination destination )
	{
		return null;
	}

	@Override
	public ItemStack addItems( final ItemStack toBeAdded )
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

		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) && this.buildCraft.addItemsToPipe( this.i, toBeAdded, this.d ) )
		{
			return null;
		}
		return toBeAdded;
	}

	@Override
	public ItemStack simulateAdd( final ItemStack toBeSimulated )
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
