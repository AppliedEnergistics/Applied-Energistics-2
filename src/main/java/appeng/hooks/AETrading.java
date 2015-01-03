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

package appeng.hooks;

import java.util.Random;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

import appeng.api.AEApi;
import appeng.api.definitions.IMaterials;
import appeng.api.util.AEItemDefinition;


public class AETrading implements IVillageTradeHandler
{

	private void addToList(MerchantRecipeList l, ItemStack a, ItemStack b)
	{
		if ( a.stackSize < 1 )
			a.stackSize = 1;
		if ( b.stackSize < 1 )
			b.stackSize = 1;

		if ( a.stackSize > a.getMaxStackSize() )
			a.stackSize = a.getMaxStackSize();
		if ( b.stackSize > b.getMaxStackSize() )
			b.stackSize = b.getMaxStackSize();

		l.add( new MerchantRecipe( a, b ) );
	}

	private void addTrade(MerchantRecipeList list, ItemStack a, ItemStack b, Random rand, int conversion_Variance)
	{
		// Sell
		ItemStack From = a.copy();
		ItemStack To = b.copy();

		From.stackSize = 1 + (Math.abs( rand.nextInt() ) % (1 + conversion_Variance));
		To.stackSize = 1;

		this.addToList( list, From, To );
	}

	private void addMerchant(MerchantRecipeList list, ItemStack item, int emera, Random rand, int greed)
	{
		if ( item == null )
			return;

		// Sell
		ItemStack From = item.copy();
		ItemStack To = new ItemStack( Items.emerald );

		int multiplier = (Math.abs( rand.nextInt() ) % 6);
		emera += (Math.abs( rand.nextInt() ) % greed) - multiplier;
		int mood = rand.nextInt() % 2;

		From.stackSize = multiplier + mood;
		To.stackSize = multiplier * emera - mood;

		if ( To.stackSize < 0 )
		{
			From.stackSize -= To.stackSize;
			To.stackSize -= To.stackSize;
		}

		this.addToList( list, From, To );

		// Buy
		ItemStack reverseTo = From.copy();
		ItemStack reverseFrom = To.copy();

		reverseFrom.stackSize = (int) (reverseFrom.stackSize * (rand.nextFloat() * 3.0f + 1.0f));

		this.addToList( list, reverseFrom, reverseTo );
	}

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
	{
		final IMaterials materials = AEApi.instance().definitions().materials();

		for ( AEItemDefinition definition : materials.silicon().asSet() )
		{
			this.addMerchant( recipeList, definition.stack( 1 ), 1, random, 2 );
		}

		for ( AEItemDefinition definition : materials.certusQuartzCrystal().asSet() )
		{
			this.addMerchant( recipeList, definition.stack( 1 ), 2, random, 4 );
		}

		for ( AEItemDefinition definition : materials.certusQuartzDust().asSet() )
		{
			this.addMerchant( recipeList, definition.stack( 1 ), 1, random, 3 );
		}

		for ( AEItemDefinition dustDefinition : materials.certusQuartzDust().asSet() )
		{
			for ( AEItemDefinition crystalDefinition : materials.certusQuartzCrystal().asSet() )
			{
				this.addTrade( recipeList, dustDefinition.stack( 1 ), crystalDefinition.stack( 1 ), random, 2 );
			}
		}
	}

}
