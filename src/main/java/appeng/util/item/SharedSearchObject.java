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

package appeng.util.item;


import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import appeng.util.Platform;


public class SharedSearchObject
{

	final int def;
	final int hash;
	public AESharedNBT shared;
	NBTTagCompound compound;

	public SharedSearchObject( Item itemID, int damageValue, NBTTagCompound tagCompound )
	{
		this.def = ( damageValue << Platform.DEF_OFFSET ) | Item.itemRegistry.getIDForObject( itemID );
		this.hash = Platform.NBTOrderlessHash( tagCompound );
		this.compound = tagCompound;
	}

	@Override
	public int hashCode()
	{
		return this.def ^ this.hash;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( obj == null )
			return false;
		if( this.getClass() != obj.getClass() )
			return false;
		SharedSearchObject other = (SharedSearchObject) obj;
		if( this.def == other.def && this.hash == other.hash )
		{
			return Platform.NBTEqualityTest( this.compound, other.compound );
		}
		return false;
	}
}
