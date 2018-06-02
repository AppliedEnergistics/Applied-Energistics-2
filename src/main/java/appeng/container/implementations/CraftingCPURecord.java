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


public class CraftingCPURecord implements Comparable<CraftingCPURecord>
{

	private final String myName;
	private final ICraftingCPU cpu;
	private final long size;
	private final int processors;

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
		final int a = Long.compare( o.getProcessors(), this.getProcessors() );
		if( a != 0 )
		{
			return a;
		}
		return Long.compare( o.getSize(), this.getSize() );
	}

	ICraftingCPU getCpu()
	{
		return this.cpu;
	}

	String getName()
	{
		return this.myName;
	}

	int getProcessors()
	{
		return this.processors;
	}

	long getSize()
	{
		return this.size;
	}
}
