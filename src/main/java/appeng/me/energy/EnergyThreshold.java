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

package appeng.me.energy;


import appeng.api.networking.energy.IEnergyWatcher;
import appeng.util.ItemSorters;


public final class EnergyThreshold implements Comparable<EnergyThreshold>
{

	public final double Limit;
	public final IEnergyWatcher watcher;
	final int hash;

	public EnergyThreshold( double lim, IEnergyWatcher wat )
	{
		this.Limit = lim;
		this.watcher = wat;

		if( this.watcher != null )
		{
			this.hash = this.watcher.hashCode() ^ ( (Double) lim ).hashCode();
		}
		else
		{
			this.hash = ( (Double) lim ).hashCode();
		}
	}

	@Override
	public final int hashCode()
	{
		return this.hash;
	}

	@Override
	public final int compareTo( EnergyThreshold o )
	{
		return ItemSorters.compareDouble( this.Limit, o.Limit );
	}
}
