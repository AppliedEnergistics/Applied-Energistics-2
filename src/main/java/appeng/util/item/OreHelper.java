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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import appeng.api.storage.data.IAEItemStack;


public class OreHelper
{

	public static final OreHelper INSTANCE = new OreHelper();

	/**
	 * A local cache to speed up OreDictionary lookups.
	 */
	private final LoadingCache<String, List<ItemStack>> oreDictCache = CacheBuilder.newBuilder().build( new CacheLoader<String, List<ItemStack>>()
	{
		public List<ItemStack> load( String oreName )
		{
			return OreDictionary.getOres( oreName );
		}
	} );

	private final Map<ItemRef, OreReference> references = new HashMap<ItemRef, OreReference>();

	/**
	 * Test if the passed {@link ItemStack} is an ore.
	 *
	 * @param ItemStack the itemstack to test
	 *
	 * @return true if an ore entry exists, false otherwise
	 */
	public OreReference isOre( ItemStack ItemStack )
	{
		ItemRef ir = new ItemRef( ItemStack );

		if( !this.references.containsKey( ir ) )
		{
			final OreReference ref = new OreReference();
			final Collection<Integer> ores = ref.getOres();
			final Collection<String> set = ref.getEquivalents();

			Set<String> toAdd = new HashSet<String>();

			for( String ore : OreDictionary.getOreNames() )
			{
				// skip ore if it is a match already or null.
				if( ore == null || toAdd.contains( ore ) )
				{
					continue;
				}

				for( ItemStack oreItem : oreDictCache.getUnchecked( ore ) )
				{
					if( OreDictionary.itemMatches( oreItem, ItemStack, false ) )
					{
						toAdd.add( ore );
						break;
					}
				}
			}

			for( String ore : toAdd )
			{
				set.add( ore );
				ores.add( OreDictionary.getOreID( ore ) );
			}

			if( !set.isEmpty() )
				this.references.put( ir, ref );
			else
				this.references.put( ir, null );
		}

		return this.references.get( ir );
	}

	public boolean sameOre( AEItemStack aeItemStack, IAEItemStack is )
	{
		OreReference a = aeItemStack.def.isOre;
		OreReference b = aeItemStack.def.isOre;

		return this.sameOre( a, b );
	}

	public boolean sameOre( OreReference a, OreReference b )
	{
		if( a == null || b == null )
			return false;

		if( a == b )
			return true;

		Collection<Integer> bOres = b.getOres();
		for( Integer ore : a.getOres() )
		{
			if( bOres.contains( ore ) )
				return true;
		}

		return false;
	}

	public boolean sameOre( AEItemStack aeItemStack, ItemStack o )
	{
		OreReference a = aeItemStack.def.isOre;
		if( a == null )
			return false;

		for( String oreName : a.getEquivalents() )
		{
			for( ItemStack oreItem : oreDictCache.getUnchecked( oreName ) )
			{
				if( OreDictionary.itemMatches( oreItem, o, false ) )
					return true;
			}
		}

		return false;
	}

	public List<ItemStack> getCachedOres( String oreName )
	{
		return oreDictCache.getUnchecked( oreName );
	}

	private static class ItemRef
	{

		private final Item ref;
		private final int damage;
		private final int hash;
		ItemRef( ItemStack stack )
		{
			this.ref = stack.getItem();

			if( stack.getItem().isDamageable() )
				this.damage = 0; // IGNORED
			else
				this.damage = stack.getItemDamage(); // might be important...

			this.hash = this.ref.hashCode() ^ this.damage;
		}

		@Override
		public int hashCode()
		{
			return this.hash;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( obj == null )
				return false;
			if( this.getClass() != obj.getClass() )
				return false;
			ItemRef other = (ItemRef) obj;
			return this.damage == other.damage && this.ref == other.ref;
		}

		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append( "ItemRef [ref=" ).append( ref.getUnlocalizedName() ).append( ", damage=" ).append( damage ).append( ", hash=" ).append( hash ).append( "]" );
			return builder.toString();
		}
	}
}