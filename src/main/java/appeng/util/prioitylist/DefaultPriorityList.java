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


public class DefaultPriorityList<T extends IAEStack<T>> implements IPartitionList<T>
{

	final static List NULL_LIST = new ArrayList();

	@Override
	public boolean isListed( T input )
	{
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public Iterable<T> getItems()
	{
		return NULL_LIST;
	}
}
