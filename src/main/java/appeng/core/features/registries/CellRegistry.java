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

package appeng.core.features.registries;


import appeng.api.storage.*;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class CellRegistry implements ICellRegistry
{

	private final List<ICellHandler> handlers;

	public CellRegistry()
	{
		this.handlers = new ArrayList<ICellHandler>();
	}

	@Override
	public void addCellHandler( final ICellHandler h )
	{
		if( h != null )
		{
			this.handlers.add( h );
		}
	}

	@Override
	public boolean isCellHandled( final ItemStack is )
	{
		if( is == null )
		{
			return false;
		}
		for( final ICellHandler ch : this.handlers )
		{
			if( ch.isCell( is ) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public ICellHandler getHandler( final ItemStack is )
	{
		if( is == null )
		{
			return null;
		}
		for( final ICellHandler ch : this.handlers )
		{
			if( ch.isCell( is ) )
			{
				return ch;
			}
		}
		return null;
	}

	@Override
	public IMEInventoryHandler getCellInventory( final ItemStack is, final ISaveProvider container, final StorageChannel chan )
	{
		if( is == null )
		{
			return null;
		}
		for( final ICellHandler ch : this.handlers )
		{
			if( ch.isCell( is ) )
			{
				return ch.getCellInventory( is, container, chan );
			}
		}
		return null;
	}
}
