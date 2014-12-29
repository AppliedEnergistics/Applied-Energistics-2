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

package appeng.util.prioitylist;

import java.util.ArrayList;
import java.util.List;

import appeng.api.storage.data.IAEStack;

public class MergedPriorityList<T extends IAEStack<T>> implements IPartitionList<T>
{

	final List<IPartitionList<T>> positive = new ArrayList<IPartitionList<T>>();
	final List<IPartitionList<T>> negative = new ArrayList<IPartitionList<T>>();

	public void addNewList(IPartitionList<T> list, boolean isWhitelist)
	{
		if ( isWhitelist )
			this.positive.add( list );
		else
			this.negative.add( list );
	}

	@Override
	public boolean isListed(T input)
	{
		for (IPartitionList<T> l : this.negative)
			if ( l.isListed( input ) )
				return false;

		if ( !this.positive.isEmpty() )
		{
			for (IPartitionList<T> l : this.positive)
				if ( l.isListed( input ) )
					return true;

			return false;
		}

		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return this.positive.isEmpty() && this.negative.isEmpty();
	}

	@Override
	public Iterable<T> getItems()
	{
		throw new RuntimeException( "Not Implemented" );
	}

}
