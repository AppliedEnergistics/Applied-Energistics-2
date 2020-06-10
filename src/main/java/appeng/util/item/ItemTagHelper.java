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
import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Collections;


public class ItemTagHelper
{

	public static final ItemTagHelper INSTANCE = new ItemTagHelper();

	/**
	 * Test if the passed {@link ItemStack} and tag list have any tags in common.
	 * Can be used if you need to fuzzy find an item in an inventory
	 * just get the tags for that item and pass them as "atags"
	 * if you ant you can also do an equals check before calling this method
	 *
	 * @param atags tag list
	 * @param bs item to compare
	 *
	 * @return true if they share any item tag
	 */
	public boolean isSimilarItem( final Collection<ResourceLocation> atags, final Item b )
	{
		return !Collections.disjoint(atags, ItemTags.getCollection().getOwningTags( b ));
	}

	/**
	 * Test if the passed {@link ItemStack}s have any tags in common.
	 *
	 * @param as item to compare
	 * @param bs item to compare
	 *
	 * @return true if they share any item tag
	 */
	public boolean isSimilarItem( final ItemStack as, final ItemStack bs )
	{
		// yay early exit!
		if( as.equals(bs))
		{
			return true;
		}

		Item a = as.getItem();
		Item b = bs.getItem();

		TagCollection<Item> tagCollection = ItemTags.getCollection();

		return !Collections.disjoint(tagCollection.getOwningTags( a ), tagCollection.getOwningTags( b ));
	}

	public boolean isSimilarItem( final AEItemStack aeItemStack, final IAEItemStack is )
	{
		return this.isSimilarItem( aeItemStack.getDefinition(), is.getDefinition() );
	}

	public Collection<ResourceLocation> getItemTags( final ItemStack is )
	{
		return ItemTags.getCollection().getOwningTags( is.getItem() );
	}

	public Collection<ResourceLocation> getItemTags( final AEItemStack aeItemStack )
	{
		return this.getItemTags( aeItemStack.getDefinition() );
	}
}