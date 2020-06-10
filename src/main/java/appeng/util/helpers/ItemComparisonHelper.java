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

package appeng.util.helpers;


import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;

import appeng.api.config.FuzzyMode;
import appeng.util.item.ItemTagHelper;


/**
 * A helper class for comparing {@link Item}, {@link ItemStack} or NBT
 *
 */
public class ItemComparisonHelper
{

	/**
	 * Compare the two {@link ItemStack}s based on the same {@link Item} and damage value.
	 *
	 * Item damage will be ignored since no mod should have subtypes based on damage.
	 * (I cant wait for the first mod to ignore this)
	 *
	 * Ignores NBT and Damage.
	 *
	 * @return true, if both are equal.
	 */
	public boolean isEqualItemType( @Nonnull final ItemStack that, @Nonnull final ItemStack other )
	{
		return !that.isEmpty() && !other.isEmpty() && that.getItem() == other.getItem();
	}

	/**
	 * Compares two {@link ItemStack} and their NBT tag for equality.
	 *
	 * Use this when a precise check is required and the same item is required.
	 * Not just something with different NBT tags.
	 *
	 * @return true, if both are identical.
	 */
	public boolean isSameItem( @Nonnull final ItemStack is, @Nonnull final ItemStack filter )
	{
		return ItemStack.areItemsEqual( is, filter ) && this.isNbtTagEqual( is.getTag(), filter.getTag() );
	}

	/**
	 * Similar to {@link ItemComparisonHelper#isEqualItemType(ItemStack, ItemStack)},
	 * but it can further check, if both match the same {@link FuzzyMode}
	 * or are considered equal by the {@link ItemTags}
	 *
	 * @param mode how to compare the two {@link ItemStack}s
	 * @return true, if both are matching the mode or considered equal by the {@link ItemTags}
	 */
	public boolean isFuzzyEqualItem( final ItemStack a, final ItemStack b, final FuzzyMode mode )
	{
		if( a.isEmpty() && b.isEmpty() )
		{
			return true;
		}

		if( a.isEmpty() || b.isEmpty() )
		{
			return false;
		}

		// same item type ==> same item
		if( a.getItem() == b.getItem())
		{
			return true;
		}

		if( ItemTagHelper.INSTANCE.isSimilarItem(a, b) )
		{
			return true;
		}

		return a.isItemEqual( b );
	}

	/**
	 * recursive test for NBT Equality, this was faster then trying to compare / generate hashes, its also more reliable
	 * then the vanilla version which likes to fail when NBT Compound data changes order, it is pretty expensive
	 * performance wise, so try an use shared tag compounds as long as the system remains in AE.
	 *
	 * TODO check where needed and if it can be removed based on the assumption that same item reference means same item
	 */
	public boolean isNbtTagEqual( final CompoundNBT left, final CompoundNBT right )
	{
		if( left == right )
		{
			return true;
		}

		final boolean isLeftEmpty = left == null || left.isEmpty();
		final boolean isRightEmpty = right == null || right.isEmpty();

		if( isLeftEmpty && isRightEmpty )
		{
			return true;
		}

		if( isLeftEmpty != isRightEmpty )
		{
			return false;
		}

		// left is not null here bcs:
		// 1. if left and right is empty the first if statement after isLeftEmpty would return
		// 2. if left is empty and right is not the 2nd if statement after isLeftEmpty would return
		return left.equals( right );

	}
}
