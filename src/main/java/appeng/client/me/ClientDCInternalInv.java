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

package appeng.client.me;


import javax.annotation.Nonnull;

import net.minecraft.util.StatCollector;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ItemSorters;


public class ClientDCInternalInv implements Comparable<ClientDCInternalInv>
{

	public final String unlocalizedName;
	public final AppEngInternalInventory inv;
	public final long id;
	public final long sortBy;

	public ClientDCInternalInv( int size, long id, long sortBy, String unlocalizedName )
	{
		this.inv = new AppEngInternalInventory( null, size );
		this.unlocalizedName = unlocalizedName;
		this.id = id;
		this.sortBy = sortBy;
	}

	public String getName()
	{
		String s = StatCollector.translateToLocal( this.unlocalizedName + ".name" );
		if( s.equals( this.unlocalizedName + ".name" ) )
		{
			return StatCollector.translateToLocal( this.unlocalizedName );
		}
		return s;
	}

	@Override
	public int compareTo( @Nonnull ClientDCInternalInv o )
	{
		return ItemSorters.compareLong( this.sortBy, o.sortBy );
	}
}