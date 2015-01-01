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

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.storage.data.IAEItemStack;

public class OreHelper
{

	public static final OreHelper INSTANCE = new OreHelper();

	static class ItemRef
	{

		ItemRef(ItemStack stack)
		{
			this.ref = stack.getItem();

			if ( stack.getItem().isDamageable() )
				this.damage = 0; // IGNORED
			else
				this.damage = stack.getItemDamage(); // might be important...

			this.hash = this.ref.hashCode() ^ this.damage;
		}

		final Item ref;
		final int damage;
		final int hash;

		@Override
		public boolean equals(Object obj)
		{
			if ( obj == null )
				return false;
			if ( this.getClass() != obj.getClass() )
				return false;
			ItemRef other = (ItemRef) obj;
			return this.damage == other.damage && this.ref == other.ref;
		}

		@Override
		public int hashCode()
		{
			return this.hash;
		}

	}

	static class OreResult
	{

		public OreReference oreValue = null;

	}

	final HashMap<ItemRef, OreResult> references = new HashMap<ItemRef, OreResult>();

	public OreReference isOre(ItemStack ItemStack)
	{
		ItemRef ir = new ItemRef( ItemStack );
		OreResult or = this.references.get( ir );

		if ( or == null )
		{
			or = new OreResult();
			this.references.put( ir, or );

			OreReference ref = new OreReference();
			Collection<Integer> ores = ref.getOres();
			Collection<ItemStack> set = ref.getEquivalents();

			for (String ore : OreDictionary.getOreNames())
			{
				boolean add = false;

				for (ItemStack oreItem : OreDictionary.getOres( ore ))
				{
					if ( OreDictionary.itemMatches( oreItem, ItemStack, false ) )
					{
						add = true;
						break;
					}
				}

				if ( add )
				{
					for (ItemStack oreItem : OreDictionary.getOres( ore ))
						set.add( oreItem.copy() );

					ores.add( OreDictionary.getOreID( ore ) );
				}
			}

			if ( !set.isEmpty() )
				or.oreValue = ref;
		}

		return or.oreValue;
	}

	public boolean sameOre(AEItemStack aeItemStack, IAEItemStack is)
	{
		OreReference a = aeItemStack.def.isOre;
		OreReference b = aeItemStack.def.isOre;

		if ( a == null || b == null )
			return false;

		if ( a == b )
			return true;

		Collection<Integer> bOres = b.getOres();
		for (Integer ore : a.getOres())
		{
			if ( bOres.contains( ore ) )
				return true;
		}

		return false;
	}

	public boolean sameOre(OreReference a, OreReference b)
	{
		if ( a == null || b == null )
			return false;

		if ( a == b )
			return true;

		Collection<Integer> bOres = b.getOres();
		for (Integer ore : a.getOres())
		{
			if ( bOres.contains( ore ) )
				return true;
		}

		return false;
	}

	public boolean sameOre(AEItemStack aeItemStack, ItemStack o)
	{
		OreReference a = aeItemStack.def.isOre;
		if ( a == null )
			return false;

		for (ItemStack oreItem : a.getEquivalents())
		{
			if ( OreDictionary.itemMatches( oreItem, o, false ) )
				return true;
		}

		return false;
	}
}