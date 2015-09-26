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

package appeng.container.implementations;


import javax.annotation.Nonnull;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.util.ItemSorters;


public class CraftingCPURecord implements Comparable<CraftingCPURecord>
{

	public final String myName;
	final ICraftingCPU cpu;
	final long size;
	final int processors;

	public CraftingCPURecord( final long size, final int coProcessors, final ICraftingCPU server )
	{
		this.size = size;
		this.processors = coProcessors;
		this.cpu = server;
		this.myName = server.getName();
	}

	@Override
	public int compareTo( @Nonnull final CraftingCPURecord o )
	{
		final int a = ItemSorters.compareLong( o.processors, this.processors );
		if( a != 0 )
		{
			return a;
		}
		return ItemSorters.compareLong( o.size, this.size );
	}
}