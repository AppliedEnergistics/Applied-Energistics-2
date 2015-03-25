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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;


public class OreReference
{

	private final List<String> otherOptions = new LinkedList<String>();
	private final Set<Integer> ores = new HashSet<Integer>();
	private List<IAEItemStack> aeOtherOptions = null;

	public Collection<String> getEquivalents()
	{
		return this.otherOptions;
	}

	public List<IAEItemStack> getAEEquivalents()
	{
		if( this.aeOtherOptions == null )
		{
			this.aeOtherOptions = new ArrayList<IAEItemStack>( this.otherOptions.size() );

			// SUMMON AE STACKS!
			for( String oreName : this.otherOptions )
			{
				for( ItemStack is : OreHelper.INSTANCE.getCachedOres( oreName ) )
				{
					if( is.getItem() != null )
					{
						this.aeOtherOptions.add( AEItemStack.create( is ) );
					}
				}
			}
		}

		return this.aeOtherOptions;
	}

	public Collection<Integer> getOres()
	{
		return this.ores;
	}
}
