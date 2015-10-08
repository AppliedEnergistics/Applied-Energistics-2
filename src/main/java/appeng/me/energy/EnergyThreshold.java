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

package appeng.me.energy;


import appeng.api.networking.energy.IEnergyWatcher;
import appeng.util.ItemSorters;


public class EnergyThreshold implements Comparable<EnergyThreshold>
{

	private final double Limit;
	private final IEnergyWatcher watcher;
	private final int hash;

	public EnergyThreshold( final double lim, final IEnergyWatcher wat )
	{
		this.Limit = lim;
		this.watcher = wat;

		if( this.getWatcher() != null )
		{
			this.hash = this.getWatcher().hashCode() ^ ( (Double) lim ).hashCode();
		}
		else
		{
			this.hash = ( (Double) lim ).hashCode();
		}
	}

	@Override
	public int hashCode()
	{
		return this.hash;
	}

	@Override
	public int compareTo( final EnergyThreshold o )
	{
		return ItemSorters.compareDouble( this.getLimit(), o.getLimit() );
	}

	double getLimit()
	{
		return this.Limit;
	}

	public IEnergyWatcher getWatcher()
	{
		return this.watcher;
	}
}
