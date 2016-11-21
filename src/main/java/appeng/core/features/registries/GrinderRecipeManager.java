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

package appeng.core.features.registries;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.features.IGrinderRecipe;
import appeng.api.features.IGrinderRegistry;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.registries.entries.AppEngGrinderRecipe;
import appeng.recipes.ores.IOreListener;
import appeng.recipes.ores.OreDictionaryHandler;
import appeng.util.Platform;


public final class GrinderRecipeManager implements IGrinderRegistry, IOreListener
{
	private final Map<CacheKey, IGrinderRecipe> recipes;
	private final Map<ItemStack, String> ores;
	private final Map<ItemStack, String> ingots;
	private final Map<String, ItemStack> dusts;
	private final Map<String, Integer> dustToOreRatio;

	public GrinderRecipeManager()
	{
		this.recipes = Maps.newHashMap();
		this.ores = Maps.newHashMap();
		this.ingots = Maps.newHashMap();
		this.dusts = Maps.newHashMap();
		this.dustToOreRatio = Maps.newHashMap();

		this.addDustRatio( "Obsidian", 1 );
		this.addDustRatio( "Charcoal", 1 );
		this.addDustRatio( "Coal", 1 );

		this.addOre( "Coal", new ItemStack( Items.COAL ) );
		this.addOre( "Charcoal", new ItemStack( Items.COAL, 1, 1 ) );

		this.addOre( "NetherQuartz", new ItemStack( Blocks.QUARTZ_ORE ) );
		this.addIngot( "NetherQuartz", new ItemStack( Items.QUARTZ ) );

		this.addOre( "Gold", new ItemStack( Blocks.GOLD_ORE ) );
		this.addIngot( "Gold", new ItemStack( Items.GOLD_INGOT ) );

		this.addOre( "Iron", new ItemStack( Blocks.IRON_ORE ) );
		this.addIngot( "Iron", new ItemStack( Items.IRON_INGOT ) );

		this.addOre( "Obsidian", new ItemStack( Blocks.OBSIDIAN ) );

		this.addIngot( "Ender", new ItemStack( Items.ENDER_PEARL ) );
		this.addIngot( "EnderPearl", new ItemStack( Items.ENDER_PEARL ) );

		this.addIngot( "Wheat", new ItemStack( Items.WHEAT ) );

		OreDictionaryHandler.INSTANCE.observe( this );
	}

	@Override
	public Collection<IGrinderRecipe> getRecipes()
	{
		return Collections.unmodifiableCollection( this.recipes.values() );
	}

	@Override
	public void addRecipe( final ItemStack in, final ItemStack out, final int cost )
	{
		Preconditions.checkNotNull( in, "Null is not accepted as input itemstack." );
		Preconditions.checkNotNull( out, "Null is not accepted as output itemstack." );
		Preconditions.checkArgument( cost > 0, "Turns must be > 0" );

		this.log( "Allow Grinding of '%1$s' to '%2$s' for %3$d turn", Platform.getItemDisplayName( in ), Platform.getItemDisplayName( out ), cost );

		this.injectRecipe( new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), cost ) );
	}

	@Override
	public void addRecipe( final ItemStack in, final ItemStack out, final ItemStack optional, final float chance, final int cost )
	{
		Preconditions.checkNotNull( in, "Null is not accepted as input itemstack." );
		Preconditions.checkNotNull( out, "Null is not accepted as output itemstack." );
		Preconditions.checkNotNull( optional, "Null is not accepted as optional itemstack." );
		Preconditions.checkArgument( chance >= 0.0 && chance <= 1.0, "chance must be within 0.0 - 1.0." );
		Preconditions.checkArgument( cost > 0, "Turns must be > 0" );

		this.log( "Allow Grinding of '%1$s' to '%2$s' with optional '%3$s' @ %4$.2f for %5$d", Platform.getItemDisplayName( in ),
				Platform.getItemDisplayName( out ), Platform.getItemDisplayName( optional ), chance, cost );

		this.injectRecipe( new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), this.copy( optional ), chance, cost ) );
	}

	@Override
	public void addRecipe( final ItemStack in, final ItemStack out, final ItemStack optional1, final float chance1, final ItemStack optional2, final float chance2, final int cost )
	{
		Preconditions.checkNotNull( in, "Null is not accepted as input itemstack." );
		Preconditions.checkNotNull( out, "Null is not accepted as output itemstack." );
		Preconditions.checkNotNull( optional1, "Null is not accepted as optional itemstack." );
		Preconditions.checkArgument( chance1 >= 0.0 && chance1 <= 1.0, "chance must be within 0.0 - 1.0." );
		Preconditions.checkNotNull( optional2, "Null is not accepted as optional2 itemstack." );
		Preconditions.checkArgument( chance2 >= 0.0 && chance2 <= 1.0, "chance2 must be within 0.0 - 1.0." );
		Preconditions.checkArgument( cost > 0, "Turns must be > 0" );

		this.log( "Allow Grinding of '%1$s' to '%2$s' with optional '%3$s' @ %4$.2f and optional2 '%5$s' @ %6$.2f for %7$d", Platform.getItemDisplayName( in ),
				Platform.getItemDisplayName( out ), Platform.getItemDisplayName( optional1 ), chance1, Platform.getItemDisplayName( optional2 ), chance2,
				cost );

		this.injectRecipe(
				new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), this.copy( optional1 ), this.copy( optional2 ), chance1, chance2, cost ) );
	}

	@Override
	public boolean removeRecipe( IGrinderRecipe recipe )
	{
		Preconditions.checkNotNull( recipe, "Cannot remove null as recipe." );

		final CacheKey key = new CacheKey( recipe.getInput() );
		final IGrinderRecipe removedRecipe = this.recipes.remove( key );

		this.log( "Removed Grinding of '%1%s'", Platform.getItemDisplayName( recipe.getInput() ) );

		return removedRecipe != null;
	}

	@Override
	public IGrinderRecipe getRecipeForInput( final ItemStack input )
	{
		this.log( "Looking up recipe for '%1$s'", Platform.getItemDisplayName( input ) );

		if( input == null )
		{
			return null;
		}

		final IGrinderRecipe recipe = this.recipes.get( new CacheKey( input ) );

		this.log( "Recipe for '%1$s' found '%2$s'", input.getUnlocalizedName(), Platform.getItemDisplayName( recipe.getOutput() ) );

		return recipe;
	}

	@Override
	public void addDustRatio( String oredictName, int ratio )
	{
		Preconditions.checkNotNull( oredictName );
		Preconditions.checkArgument( ratio > 0 );

		this.log( "Added ratio for '%1$s' of %2$d", oredictName, ratio );

		this.dustToOreRatio.put( oredictName, ratio );
	}

	@Override
	public boolean removeDustRatio( String oredictName )
	{
		Preconditions.checkNotNull( oredictName );

		this.log( "Removed ratio for '%1$s'", oredictName );

		return this.dustToOreRatio.remove( oredictName ) != null;
	}

	@Override
	public void oreRegistered( final String name, final ItemStack item )
	{
		if( name.startsWith( "ore" ) || name.startsWith( "crystal" ) || name.startsWith( "gem" ) || name.startsWith( "ingot" ) || name.startsWith( "dust" ) )
		{
			for( final String ore : AEConfig.instance().getGrinderOres() )
			{
				if( name.equals( "ore" + ore ) )
				{
					this.addOre( ore, item );
				}
				else if( name.equals( "crystal" + ore ) || name.equals( "ingot" + ore ) || name.equals( "gem" + ore ) )
				{
					this.addIngot( ore, item );
				}
				else if( name.equals( "dust" + ore ) )
				{
					this.addDust( ore, item );
				}
			}
		}
	}

	private void injectRecipe( final AppEngGrinderRecipe appEngGrinderRecipe )
	{
		final CacheKey cacheKey = new CacheKey( appEngGrinderRecipe.getInput() );

		if( this.recipes.containsKey( cacheKey ) )
		{
			this.log( "Tried to add duplicate recipe for '%1$s'", Platform.getItemDisplayName( appEngGrinderRecipe.getInput() ) );
			return;
		}

		this.recipes.put( cacheKey, appEngGrinderRecipe );
	}

	private ItemStack copy( final ItemStack is )
	{
		if( is != null )
		{
			return is.copy();
		}
		return null;
	}

	private int getDustToOreRatio( final String name )
	{
		return this.dustToOreRatio.getOrDefault( name, 2 );
	}

	private void addOre( final String name, final ItemStack item )
	{
		if( item == null )
		{
			return;
		}
		this.log( "Adding Ore: '%1$s'", Platform.getItemDisplayName( item ) );

		this.ores.put( item, name );

		if( this.dusts.containsKey( name ) )
		{
			final ItemStack is = this.dusts.get( name ).copy();
			final int ratio = this.getDustToOreRatio( name );
			if( ratio > 1 )
			{
				final ItemStack extra = is.copy();
				extra.stackSize = ratio - 1;
				this.addRecipe( item, is, extra, (float) ( AEConfig.instance().getOreDoublePercentage() / 100.0 ), 8 );
			}
			else
			{
				this.addRecipe( item, is, 8 );
			}
		}
	}

	private void addIngot( final String name, final ItemStack item )
	{
		if( item == null )
		{
			return;
		}
		this.log( "Adding Ingot: '%1$s'", Platform.getItemDisplayName( item ) );

		this.ingots.put( item, name );

		if( this.dusts.containsKey( name ) )
		{
			this.addRecipe( item, this.dusts.get( name ), 4 );
		}
	}

	private void addDust( final String name, final ItemStack item )
	{
		if( item == null )
		{
			return;
		}
		if( this.dusts.containsKey( name ) )
		{
			this.log( "Rejecting Dust: '%1$s'", Platform.getItemDisplayName( item ) );
			return;
		}

		this.log( "Adding Dust: '%1$s'", Platform.getItemDisplayName( item ) );

		this.dusts.put( name, item );

		for( final Entry<ItemStack, String> d : this.ores.entrySet() )
		{
			if( name.equals( d.getValue() ) )
			{
				final ItemStack is = item.copy();
				is.stackSize = 1;
				final int ratio = this.getDustToOreRatio( name );
				if( ratio > 1 )
				{
					final ItemStack extra = is.copy();
					extra.stackSize = ratio - 1;
					this.addRecipe( d.getKey(), is, extra, (float) ( AEConfig.instance().getOreDoublePercentage() / 100.0 ), 8 );
				}
				else
				{
					this.addRecipe( d.getKey(), is, 8 );
				}
			}
		}

		for( final Entry<ItemStack, String> d : this.ingots.entrySet() )
		{
			if( name.equals( d.getValue() ) )
			{
				this.addRecipe( d.getKey(), item, 4 );
			}
		}
	}

	private void log( final String o, Object... params )
	{
		AELog.grinder( o, params );
	}

	private static class CacheKey
	{
		private final Item item;
		private final int damage;

		public CacheKey( ItemStack input )
		{
			Preconditions.checkNotNull( input );
			Preconditions.checkNotNull( input.getItem() );

			this.item = input.getItem();
			this.damage = input.getItemDamage();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + damage;
			result = prime * result + ( ( item == null ) ? 0 : item.hashCode() );
			return result;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj )
			{
				return true;
			}
			if( obj == null || getClass() != obj.getClass() )
			{
				return false;
			}

			CacheKey other = (CacheKey) obj;

			if( damage != other.damage )
			{
				return false;
			}

			if( item == null )
			{
				if( other.item != null )
					return false;
			}
			else if( item != other.item )
			{
				return false;
			}

			return true;
		}

	}
}
