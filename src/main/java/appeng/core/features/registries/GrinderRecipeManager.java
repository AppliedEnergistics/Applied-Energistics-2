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


import appeng.api.features.IGrinderEntry;
import appeng.api.features.IGrinderRegistry;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.registries.entries.AppEngGrinderRecipe;
import appeng.recipes.ores.IOreListener;
import appeng.recipes.ores.OreDictionaryHandler;
import appeng.util.Platform;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public final class GrinderRecipeManager implements IGrinderRegistry, IOreListener
{
	private final List<IGrinderEntry> recipes;
	private final Map<ItemStack, String> ores;
	private final Map<ItemStack, String> ingots;
	private final Map<String, ItemStack> dusts;

	public GrinderRecipeManager()
	{
		this.recipes = new ArrayList<IGrinderEntry>();
		this.ores = new HashMap<ItemStack, String>();
		this.ingots = new HashMap<ItemStack, String>();
		this.dusts = new HashMap<String, ItemStack>();

		this.addOre( "Coal", new ItemStack( Items.coal ) );
		this.addOre( "Charcoal", new ItemStack( Items.coal, 1, 1 ) );

		this.addOre( "NetherQuartz", new ItemStack( Blocks.quartz_ore ) );
		this.addIngot( "NetherQuartz", new ItemStack( Items.quartz ) );

		this.addOre( "Gold", new ItemStack( Blocks.gold_ore ) );
		this.addIngot( "Gold", new ItemStack( Items.gold_ingot ) );

		this.addOre( "Iron", new ItemStack( Blocks.iron_ore ) );
		this.addIngot( "Iron", new ItemStack( Items.iron_ingot ) );

		this.addOre( "Obsidian", new ItemStack( Blocks.obsidian ) );

		this.addIngot( "Ender", new ItemStack( Items.ender_pearl ) );
		this.addIngot( "EnderPearl", new ItemStack( Items.ender_pearl ) );

		this.addIngot( "Wheat", new ItemStack( Items.wheat ) );

		OreDictionaryHandler.INSTANCE.observe( this );
	}

	@Override
	public List<IGrinderEntry> getRecipes()
	{
		this.log( "API - getRecipes" );
		return this.recipes;
	}

	@Override
	public void addRecipe( final ItemStack in, final ItemStack out, final int cost )
	{
		if( in == null || out == null )
		{
			this.log( "Invalid Grinder Recipe Specified." );
			return;
		}

		this.log( "Allow Grinding of " + Platform.getItemDisplayName( in ) + " to " + Platform.getItemDisplayName( out ) + " for " + cost );
		this.injectRecipe( new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), cost ) );
	}

	@Override
	public void addRecipe( final ItemStack in, final ItemStack out, final ItemStack optional, final float chance, final int cost )
	{
		if( in == null || ( optional == null && out == null ) )
		{
			this.log( "Invalid Grinder Recipe Specified." );
			return;
		}

		this.log( "Allow Grinding of " + Platform.getItemDisplayName( in ) + " to " + Platform.getItemDisplayName( out ) + " with optional " + Platform.getItemDisplayName( optional ) + " for " + cost );
		this.injectRecipe( new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), this.copy( optional ), chance, cost ) );
	}

	@Override
	public void addRecipe( final ItemStack in, final ItemStack out, final ItemStack optional, final float chance, final ItemStack optional2, final float chance2, final int cost )
	{
		if( in == null || ( optional == null && out == null && optional2 == null ) )
		{
			this.log( "Invalid Grinder Recipe Specified." );
			return;
		}

		this.log( "Allow Grinding of " + Platform.getItemDisplayName( in ) + " to " + Platform.getItemDisplayName( out ) + " with optional " + Platform.getItemDisplayName( optional ) + " for " + cost );
		this.injectRecipe( new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), this.copy( optional ), this.copy( optional2 ), chance, chance2, cost ) );
	}

	private void injectRecipe( final AppEngGrinderRecipe appEngGrinderRecipe )
	{
		for( final IGrinderEntry gr : this.recipes )
		{
			if( Platform.isSameItemPrecise( gr.getInput(), appEngGrinderRecipe.getInput() ) )
			{
				return;
			}
		}

		this.recipes.add( appEngGrinderRecipe );
	}

	private ItemStack copy( final ItemStack is )
	{
		if( is != null )
		{
			return is.copy();
		}
		return null;
	}

	@Override
	public IGrinderEntry getRecipeForInput( final ItemStack input )
	{
		this.log( "Looking up recipe for " + Platform.getItemDisplayName( input ) );
		if( input != null )
		{
			for( final IGrinderEntry r : this.recipes )
			{
				if( Platform.isSameItem( input, r.getInput() ) )
				{
					this.log( "Recipe for " + input.getUnlocalizedName() + " found " + Platform.getItemDisplayName( r.getOutput() ) );
					return r;
				}
			}

			this.log( "Could not find recipe for " + Platform.getItemDisplayName( input ) );
		}

		return null;
	}

	private void log( final String o )
	{
		AELog.grinder( o );
	}

	private int getDustToOreRatio( final String name )
	{
		if( name.equals( "Obsidian" ) )
		{
			return 1;
		}
		if( name.equals( "Charcoal" ) )
		{
			return 1;
		}
		if( name.equals( "Coal" ) )
		{
			return 1;
		}
		return 2;
	}

	private void addOre( final String name, final ItemStack item )
	{
		if( item == null )
		{
			return;
		}
		this.log( "Adding Ore - " + name + " : " + Platform.getItemDisplayName( item ) );

		this.ores.put( item, name );

		if( this.dusts.containsKey( name ) )
		{
			final ItemStack is = this.dusts.get( name ).copy();
			final int ratio = this.getDustToOreRatio( name );
			if( ratio > 1 )
			{
				final ItemStack extra = is.copy();
				extra.stackSize = ratio - 1;
				this.addRecipe( item, is, extra, (float) ( AEConfig.instance.oreDoublePercentage / 100.0 ), 8 );
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
		this.log( "Adding Ingot - " + name + " : " + Platform.getItemDisplayName( item ) );

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
			this.log( "Rejecting Dust - " + name + " : " + Platform.getItemDisplayName( item ) );
			return;
		}

		this.log( "Adding Dust - " + name + " : " + Platform.getItemDisplayName( item ) );

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
					this.addRecipe( d.getKey(), is, extra, (float) ( AEConfig.instance.oreDoublePercentage / 100.0 ), 8 );
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

	@Override
	public void oreRegistered( final String name, final ItemStack item )
	{
		if( name.startsWith( "ore" ) || name.startsWith( "crystal" ) || name.startsWith( "gem" ) || name.startsWith( "ingot" ) || name.startsWith( "dust" ) )
		{
			for( final String ore : AEConfig.instance.grinderOres )
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
}
