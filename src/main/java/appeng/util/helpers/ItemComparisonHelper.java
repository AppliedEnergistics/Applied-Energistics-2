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

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.config.FuzzyMode;
import appeng.util.item.OreHelper;
import appeng.util.item.OreReference;


/**
 * A helper class for comparing {@link Item}, {@link ItemStack} or NBT
 *
 */
public class ItemComparisonHelper
{

	/**
	 * Compare the two {@link ItemStack}s based on the same {@link Item} and damage value.
	 *
	 * In case of the item being damageable, only the {@link Item} will be considered.
	 * If not it will also compare both damage values.
	 *
	 * Ignores NBT.
	 *
	 * @return true, if both are equal.
	 */
	public boolean isEqualItemType( @Nonnull final ItemStack that, @Nonnull final ItemStack other )
	{
		if( !that.isEmpty() && !other.isEmpty() && that.getItem() == other.getItem() )
		{
			if( that.isItemStackDamageable() )
			{
				return true;
			}
			return that.getItemDamage() == other.getItemDamage();
		}
		return false;
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
		return ItemStack.areItemsEqual( is, filter ) && this.isNbtTagEqual( is.getTagCompound(), filter.getTagCompound() );
	}

	/**
	 * 
	 * Compares two {@link ItemStack} and their NBT tag for equality.
	 *
	 * This is slightly more permissive than {@link #isSameItem(ItemStack, ItemStack)} in regards to NBT tags.
	 * Mostly that it considers null and empty {@link NBTTagCompound} to be equal.
	 *
	 * @return true, if both are identical.
	 */
	public boolean isPermissivelySameItem( @Nonnull final ItemStack is, @Nonnull final ItemStack filter )
	{
		return ItemStack.areItemsEqual( is, filter ) && this.isNbtTagPermissivelyEqual( is.getTagCompound(), filter.getTagCompound() );
	}

	/**
	 * Similar to {@link ItemComparisonHelper#isEqualItem(ItemStack, ItemStack)},
	 * but it can further check, if both match the same {@link FuzzyMode}
	 * or are considered equal by the {@link OreDictionary}
	 *
	 * @param mode how to compare the two {@link ItemStack}s
	 * @return true, if both are matching the mode or considered equal by the {@link OreDictionary}
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

		/*
		 * if ( a.itemID != 0 && b.itemID != 0 && a.isItemStackDamageable() && ! a.getHasSubtypes() && a.itemID ==
		 * b.itemID ) { return (a.getItemDamage() > 0) == (b.getItemDamage() > 0); }
		 */

		// test damageable items..
		if( a.getItem() != Items.AIR && b.getItem() != Items.AIR && a.getItem().isDamageable() && a.getItem() == b.getItem() )
		{
			try
			{
				if( mode == FuzzyMode.IGNORE_ALL )
				{
					return true;
				}
				else if( mode == FuzzyMode.PERCENT_99 )
				{
					final Item ai = a.getItem();
					final Item bi = b.getItem();

					return ( ai.getDurabilityForDisplay( a ) > 1 ) == ( bi.getDurabilityForDisplay( b ) > 1 );
				}
				else
				{
					final Item ai = a.getItem();
					final Item bi = b.getItem();

					final float percentDamagedOfA = 1.0f - (float) ai.getDurabilityForDisplay( a );
					final float percentDamagedOfB = 1.0f - (float) bi.getDurabilityForDisplay( b );

					return ( percentDamagedOfA > mode.breakPoint ) == ( percentDamagedOfB > mode.breakPoint );
				}
			}
			catch( final Throwable e )
			{
				if( mode == FuzzyMode.IGNORE_ALL )
				{
					return true;
				}
				else if( mode == FuzzyMode.PERCENT_99 )
				{
					return ( a.getItemDamage() > 1 ) == ( b.getItemDamage() > 1 );
				}
				else
				{
					final float percentDamagedOfA = (float) a.getItemDamage() / (float) a.getMaxDamage();
					final float percentDamagedOfB = (float) b.getItemDamage() / (float) b.getMaxDamage();

					return ( percentDamagedOfA > mode.breakPoint ) == ( percentDamagedOfB > mode.breakPoint );
				}
			}
		}

		final OreReference aOR = OreHelper.INSTANCE.getOre( a ).orElse( null );
		final OreReference bOR = OreHelper.INSTANCE.getOre( b ).orElse( null );

		if( OreHelper.INSTANCE.sameOre( aOR, bOR ) )
		{
			return true;
		}

		/*
		 * // test ore dictionary.. int OreID = getOreID( a ); if ( OreID != -1 ) return OreID == getOreID( b );
		 * if ( Mode != FuzzyMode.IGNORE_ALL ) { if ( a.hasTagCompound() && !isShared( a.getTagCompound() ) ) { a =
		 * Platform.getSharedItemStack( AEItemStack.create( a ) ); }
		 * if ( b.hasTagCompound() && !isShared( b.getTagCompound() ) ) { b = Platform.getSharedItemStack(
		 * AEItemStack.create( b ) ); }
		 * // test regular items with damage values and what not... if ( isShared( a.getTagCompound() ) && isShared(
		 * b.getTagCompound() ) && a.itemID == b.itemID ) { return ((AppEngSharedNBTTagCompound)
		 * a.getTagCompound()).compareFuzzyWithRegistry( (AppEngSharedNBTTagCompound) b.getTagCompound() ); } }
		 */

		return a.isItemEqual( b );
	}

	/**
	 * recursive test for NBT Equality, this was faster then trying to compare / generate hashes, its also more reliable
	 * then the vanilla version which likes to fail when NBT Compound data changes order, it is pretty expensive
	 * performance wise, so try an use shared tag compounds as long as the system remains in AE.
	 */
	public boolean isNbtTagEqual( final NBTBase left, final NBTBase right )
	{
		if( left == right )
		{
			return true;
		}
		if( left != null )
		{
			return left.equals( right );
		}
		return false;
	}

	private boolean isNbtTagPermissivelyEqual( final NBTBase left, final NBTBase right )
	{
		if( left == right )
		{
			return true;
		}

		if( ( left == null && right == null ) || ( left != null && left.hasNoTags() && right == null ) || ( right != null && right
				.hasNoTags() && left == null ) || ( left != null && left.hasNoTags() && right != null && right.hasNoTags() ) )
		{
			return true;
		}

		if( ( left == null && right != null ) || ( left != null && right == null ) )
		{
			return false;
		}

		return isNbtTagEqual( left, right );
	}
}
