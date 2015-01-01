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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import appeng.api.features.IGrinderEntry;
import appeng.api.features.IGrinderRegistry;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.registries.entries.AppEngGrinderRecipe;
import appeng.recipes.ores.IOreListener;
import appeng.recipes.ores.OreDictionaryHandler;
import appeng.util.Platform;

public class GrinderRecipeManager implements IGrinderRegistry, IOreListener
{

	public final List<IGrinderEntry> RecipeList;

	private ItemStack copy(ItemStack is)
	{
		if ( is != null )
			return is.copy();
		return null;
	}

	public GrinderRecipeManager() {
		this.RecipeList = new ArrayList<IGrinderEntry>();

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
		return this.RecipeList;
	}

	private void injectRecipe(AppEngGrinderRecipe appEngGrinderRecipe)
	{
		for (IGrinderEntry gr : this.RecipeList)
			if ( Platform.isSameItemPrecise( gr.getInput(), appEngGrinderRecipe.getInput() ) )
				return;

		this.RecipeList.add( appEngGrinderRecipe );
	}

	@Override
	public void addRecipe(ItemStack in, ItemStack out, int cost)
	{
		if ( in == null || out == null )
		{
			this.log( "Invalid Grinder Recipe Specified." );
			return;
		}

		this.log( "Allow Grinding of " + Platform.getItemDisplayName( in ) + " to " + Platform.getItemDisplayName( out ) + " for " + cost );
		this.injectRecipe( new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), cost ) );
	}

	@Override
	public void addRecipe(ItemStack in, ItemStack out, ItemStack optional, float chance, int cost)
	{
		if ( in == null || (optional == null && out == null) )
		{
			this.log( "Invalid Grinder Recipe Specified." );
			return;
		}

		this.log( "Allow Grinding of " + Platform.getItemDisplayName( in ) + " to " + Platform.getItemDisplayName( out ) + " with optional "
				+ Platform.getItemDisplayName( optional ) + " for " + cost );
		this.injectRecipe( new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), this.copy( optional ), chance, cost ) );
	}

	@Override
	public void addRecipe(ItemStack in, ItemStack out, ItemStack optional, float chance, ItemStack optional2, float chance2, int cost)
	{
		if ( in == null || (optional == null && out == null && optional2 == null) )
		{
			this.log( "Invalid Grinder Recipe Specified." );
			return;
		}

		this.log( "Allow Grinding of " + Platform.getItemDisplayName( in ) + " to " + Platform.getItemDisplayName( out ) + " with optional "
				+ Platform.getItemDisplayName( optional ) + " for " + cost );
		this.injectRecipe( new AppEngGrinderRecipe( this.copy( in ), this.copy( out ), this.copy( optional ), chance, cost ) );
	}

	@Override
	public IGrinderEntry getRecipeForInput(ItemStack input)
	{
		this.log( "Looking up recipe for " + Platform.getItemDisplayName( input ) );
		if ( input != null )
		{
			for (IGrinderEntry r : this.RecipeList)
			{
				if ( Platform.isSameItem( input, r.getInput() ) )
				{
					this.log( "Recipe for " + input.getUnlocalizedName() + " found " + Platform.getItemDisplayName( r.getOutput() ) );
					return r;
				}
			}

			this.log( "Could not find recipe for " + Platform.getItemDisplayName( input ) );
		}

		return null;
	}

	public void log(String o)
	{
		AELog.grinder( o );
	}

	private int getDustToOreRatio(String name)
	{
		if ( name.equals( "Obsidian" ) )
			return 1;
		if ( name.equals( "Charcoal" ) )
			return 1;
		if ( name.equals( "Coal" ) )
			return 1;
		return 2;
	}

	public final Map<ItemStack, String> Ores = new HashMap<ItemStack, String>();
	public final Map<ItemStack, String> Ingots = new HashMap<ItemStack, String>();
	public final Map<String, ItemStack> Dusts = new HashMap<String, ItemStack>();

	private void addOre(String name, ItemStack item)
	{
		if ( item == null )
			return;
		this.log( "Adding Ore - " + name + " : " + Platform.getItemDisplayName( item ) );

		this.Ores.put( item, name );

		if ( this.Dusts.containsKey( name ) )
		{
			ItemStack is = this.Dusts.get( name ).copy();
			int ratio = this.getDustToOreRatio( name );
			if ( ratio > 1 )
			{
				ItemStack extra = is.copy();
				extra.stackSize = ratio - 1;
				this.addRecipe( item, is, extra, (float) (AEConfig.instance.oreDoublePercentage / 100.0), 8 );
			}
			else
				this.addRecipe( item, is, 8 );
		}
	}

	private void addIngot(String name, ItemStack item)
	{
		if ( item == null )
			return;
		this.log( "Adding Ingot - " + name + " : " + Platform.getItemDisplayName( item ) );

		this.Ingots.put( item, name );

		if ( this.Dusts.containsKey( name ) )
		{
			this.addRecipe( item, this.Dusts.get( name ), 4 );
		}
	}

	private void addDust(String name, ItemStack item)
	{
		if ( item == null )
			return;
		if ( this.Dusts.containsKey( name ) )
		{
			this.log( "Rejecting Dust - " + name + " : " + Platform.getItemDisplayName( item ) );
			return;
		}

		this.log( "Adding Dust - " + name + " : " + Platform.getItemDisplayName( item ) );

		this.Dusts.put( name, item );

		for (Entry<ItemStack, String> d : this.Ores.entrySet())
			if ( name.equals( d.getValue() ) )
			{
				ItemStack is = item.copy();
				is.stackSize = 1;
				int ratio = this.getDustToOreRatio( name );
				if ( ratio > 1 )
				{
					ItemStack extra = is.copy();
					extra.stackSize = ratio - 1;
					this.addRecipe( d.getKey(), is, extra, (float) (AEConfig.instance.oreDoublePercentage / 100.0), 8 );
				}
				else
					this.addRecipe( d.getKey(), is, 8 );
			}

		for (Entry<ItemStack, String> d : this.Ingots.entrySet())
			if ( name.equals( d.getValue() ) )
				this.addRecipe( d.getKey(), item, 4 );
	}

	@Override
	public void oreRegistered(String name, ItemStack item)
	{
		if ( name.startsWith( "ore" ) || name.startsWith( "crystal" ) || name.startsWith( "gem" ) || name.startsWith( "ingot" ) || name.startsWith( "dust" ) )
		{
			for (String ore : AEConfig.instance.grinderOres)
			{
				if ( name.equals( "ore" + ore ) )
				{
					this.addOre( ore, item );
				}
				else if ( name.equals( "crystal" + ore ) || name.equals( "ingot" + ore ) || name.equals( "gem" + ore ) )
				{
					this.addIngot( ore, item );
				}
				else if ( name.equals( "dust" + ore ) )
				{
					this.addDust( ore, item );
				}
			}
		}
	}
}
