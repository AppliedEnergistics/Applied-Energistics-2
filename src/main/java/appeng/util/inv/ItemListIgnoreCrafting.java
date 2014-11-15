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

import java.util.Collection;
import java.util.Iterator;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public class ItemListIgnoreCrafting<T extends IAEStack> implements IItemList<T>
{

	final IItemList<T> target;

	public ItemListIgnoreCrafting(IItemList<T> cla) {
		target = cla;
	}

	@Override
	public void add(T option)
	{
		if ( option != null && option.isCraftable() )
		{
			option = (T) option.copy();
			option.setCraftable( false );
		}

		target.add( option );
	}

	@Override
	public void addCrafting(T option)
	{
		// nothing.
	}

	@Override
	public T findPrecise(T i)
	{
		return target.findPrecise( i );
	}

	@Override
	public Collection<T> findFuzzy(T input, FuzzyMode fuzzy)
	{
		return target.findFuzzy( input, fuzzy );
	}

	@Override
	public boolean isEmpty()
	{
		return target.isEmpty();
	}

	@Override
	public void addStorage(T option)
	{
		target.addStorage( option );
	}

	@Override
	public void addRequestable(T option)
	{
		target.addRequestable( option );
	}

	@Override
	public T getFirstItem()
	{
		return target.getFirstItem();
	}

	@Override
	public int size()
	{
		return target.size();
	}

	@Override
	public Iterator<T> iterator()
	{
		return target.iterator();
	}

	@Override
	public void resetStatus()
	{
		target.resetStatus();
	}
}
