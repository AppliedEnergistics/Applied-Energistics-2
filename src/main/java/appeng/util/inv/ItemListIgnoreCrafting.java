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


import java.util.Collection;
import java.util.Iterator;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;


public final class ItemListIgnoreCrafting<T extends IAEStack> implements IItemList<T>
{

	final IItemList<T> target;

	public ItemListIgnoreCrafting( IItemList<T> cla )
	{
		this.target = cla;
	}

	@Override
	public final void add( T option )
	{
		if( option != null && option.isCraftable() )
		{
			option = (T) option.copy();
			option.setCraftable( false );
		}

		this.target.add( option );
	}

	@Override
	public final T findPrecise( T i )
	{
		return this.target.findPrecise( i );
	}

	@Override
	public final Collection<T> findFuzzy( T input, FuzzyMode fuzzy )
	{
		return this.target.findFuzzy( input, fuzzy );
	}

	@Override
	public final boolean isEmpty()
	{
		return this.target.isEmpty();
	}

	@Override
	public final void addStorage( T option )
	{
		this.target.addStorage( option );
	}

	@Override
	public final void addCrafting( T option )
	{
		// nothing.
	}

	@Override
	public final void addRequestable( T option )
	{
		this.target.addRequestable( option );
	}

	@Override
	public final T getFirstItem()
	{
		return this.target.getFirstItem();
	}

	@Override
	public final int size()
	{
		return this.target.size();
	}

	@Override
	public final Iterator<T> iterator()
	{
		return this.target.iterator();
	}

	@Override
	public final void resetStatus()
	{
		this.target.resetStatus();
	}
}
