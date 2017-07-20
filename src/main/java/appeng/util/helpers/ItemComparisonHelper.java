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


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.config.FuzzyMode;
import appeng.core.AELog;
import appeng.util.item.AESharedNBT;
import appeng.util.item.OreHelper;
import appeng.util.item.OreReference;


/**
 * A helper class for comparing {@link Item}, {@link ItemStack} or NBT
 *
 */
public class ItemComparisonHelper
{

	private static Field tagList;

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
	public boolean isEqualItemType( final ItemStack that, final ItemStack other )
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
	 * A wrapper around {@link ItemStack#isItemEqual(ItemStack)}.
	 *
	 * The benefit is to compare two null item stacks, without any additional null checks.
	 *
	 * Ignores NBT.
	 *
	 * @return true, if both are equal.
	 */
	public boolean isEqualItem( @Nonnull final ItemStack left, @Nonnull final ItemStack right )
	{
		return this.isItemEqual( left, right );
	}

	/**
	 * A slightly different method from ItemStack.java to skip the isEmpty() check. This allows you to check for
	 * identical empty spots..
	 */
	public boolean isItemEqual( ItemStack left, ItemStack right )
	{
		return left.getItem() == right.getItem() && left.getItemDamage() == right.getItemDamage();
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
		return this.isEqualItem( is, filter ) && this.hasSameNbtTag( is, filter );
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

		final OreReference aOR = OreHelper.INSTANCE.isOre( a );
		final OreReference bOR = OreHelper.INSTANCE.isOre( b );

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
		// same type?
		final byte id = left.getId();
		if( id == right.getId() )
		{
			switch( id )
			{
				case 10:
				{
					final NBTTagCompound ctA = (NBTTagCompound) left;
					final NBTTagCompound ctB = (NBTTagCompound) right;

					final Set<String> cA = ctA.getKeySet();
					final Set<String> cB = ctB.getKeySet();

					if( cA.size() != cB.size() )
					{
						return false;
					}

					for( final String name : cA )
					{
						final NBTBase tag = ctA.getTag( name );
						final NBTBase aTag = ctB.getTag( name );
						if( aTag == null )
						{
							return false;
						}

						if( !this.isNbtTagEqual( tag, aTag ) )
						{
							return false;
						}
					}

					return true;
				}

				case 9: // ) // A instanceof NBTTagList )
				{
					final NBTTagList lA = (NBTTagList) left;
					final NBTTagList lB = (NBTTagList) right;
					if( lA.tagCount() != lB.tagCount() )
					{
						return false;
					}

					final List<NBTBase> tag = this.tagList( lA );
					final List<NBTBase> aTag = this.tagList( lB );
					if( tag.size() != aTag.size() )
					{
						return false;
					}

					for( int x = 0; x < tag.size(); x++ )
					{
						if( aTag.get( x ) == null )
						{
							return false;
						}

						if( !this.isNbtTagEqual( tag.get( x ), aTag.get( x ) ) )
						{
							return false;
						}
					}

					return true;
				}

				case 1: // NBTTagByte
					return ( (NBTPrimitive) left ).getByte() == ( (NBTPrimitive) right ).getByte();

				case 4: // NBTTagLong
					return ( (NBTPrimitive) left ).getLong() == ( (NBTPrimitive) right ).getLong();

				case 8: // NBTTagString
					return ( (NBTTagString) left ).getString().equals( ( (NBTTagString) right ).getString() );

				case 6: // NBTTagDouble
					return ( (NBTPrimitive) left ).getDouble() == ( (NBTPrimitive) right ).getDouble();

				case 5: // NBTTagFloat
					return ( (NBTPrimitive) left ).getFloat() == ( (NBTPrimitive) right ).getFloat();

				case 3: // NBTTagInt
					return ( (NBTPrimitive) left ).getInt() == ( (NBTPrimitive) right ).getInt();

				default:
					return left.equals( right );
			}
		}

		return false;
	}

	/**
	 * Unordered hash of NBT Data, used to work thought huge piles fast, but ignores the order just in case MC
	 * decided to change it... WHICH IS BAD...
	 */
	public int createUnorderedNbtHash( final NBTBase nbt )
	{
		// same type?
		int hash = 0;
		final byte id = nbt.getId();
		hash += id;
		switch( id )
		{
			case 10:
			{
				final NBTTagCompound ctA = (NBTTagCompound) nbt;

				final Set<String> cA = ctA.getKeySet();

				for( final String name : cA )
				{
					hash += name.hashCode() ^ this.createUnorderedNbtHash( ctA.getTag( name ) );
				}

				return hash;
			}

			case 9: // ) // A instanceof NBTTagList )
			{
				final NBTTagList lA = (NBTTagList) nbt;
				hash += 9 * lA.tagCount();

				final List<NBTBase> l = this.tagList( lA );
				for( int x = 0; x < l.size(); x++ )
				{
					hash += ( (Integer) x ).hashCode() ^ this.createUnorderedNbtHash( l.get( x ) );
				}

				return hash;
			}

			case 1: // NBTTagByte
				return hash + ( (NBTPrimitive) nbt ).getByte();

			case 4: // NBTTagLong
				return hash + (int) ( (NBTPrimitive) nbt ).getLong();

			case 8: // NBTTagString
				return hash + ( (NBTTagString) nbt ).getString().hashCode();

			case 6: // NBTTagDouble
				return hash + (int) ( (NBTPrimitive) nbt ).getDouble();

			case 5: // NBTTagFloat
				return hash + (int) ( (NBTPrimitive) nbt ).getFloat();

			case 3: // NBTTagInt
				return hash + ( (NBTPrimitive) nbt ).getInt();

			default:
				return hash;
		}
	}

	/**
	 * Lots of silliness to try and account for weird tag related junk, basically requires that two tags have at least
	 * something in their tags before it wastes its time comparing them.
	 */
	private boolean hasSameNbtTag( final ItemStack a, final ItemStack b )
	{
		if( a.isEmpty() && b.isEmpty() )
		{
			return true;
		}
		if( a.isEmpty() || b.isEmpty() )
		{
			return false;
		}
		if( a == b )
		{
			return true;
		}

		final NBTTagCompound ta = a.getTagCompound();
		final NBTTagCompound tb = b.getTagCompound();
		if( ta == tb )
		{
			return true;
		}

		if( ( ta == null && tb == null ) || ( ta != null && ta.hasNoTags() && tb == null ) || ( tb != null && tb
				.hasNoTags() && ta == null ) || ( ta != null && ta.hasNoTags() && tb != null && tb.hasNoTags() ) )
		{
			return true;
		}

		if( ( ta == null && tb != null ) || ( ta != null && tb == null ) )
		{
			return false;
		}

		// if both tags are shared this is easy...
		if( AESharedNBT.isShared( ta ) && AESharedNBT.isShared( tb ) )
		{
			return ta == tb;
		}

		return this.isNbtTagEqual( ta, tb );
	}

	private List<NBTBase> tagList( final NBTTagList lB )
	{
		if( tagList == null )
		{
			try
			{
				tagList = lB.getClass().getDeclaredField( "tagList" );
			}
			catch( final Throwable t )
			{
				try
				{
					tagList = lB.getClass().getDeclaredField( "field_74747_a" );
				}
				catch( final Throwable z )
				{
					AELog.debug( t );
					AELog.debug( z );
				}
			}
		}

		try
		{
			tagList.setAccessible( true );
			return (List<NBTBase>) tagList.get( lB );
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}

		return new ArrayList<>();
	}

}
