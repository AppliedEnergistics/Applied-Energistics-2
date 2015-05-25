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

package appengee3compat.recipes;


import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IMaterials;
import appengee3compat.core.AELog;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;


public class RegisterFurnace
{
	public static void initRecipes()
	{
		int recipeCount = 2;

		final IDefinitions definitions = AEApi.instance().definitions();
		final IMaterials materials = definitions.materials();
		final IBlocks blocks = definitions.blocks();

		addRecipe( materials.silicon().maybeStack( 1 ).get(), Arrays.asList( new ItemStack[] { materials.certusQuartzDust().maybeStack( 1 ).get() } ) );
		recipeCount++;
		addRecipe( new ItemStack( blocks.skyStone().maybeItem().get(), 1, 1 ), Arrays.asList( new ItemStack[] { blocks.skyStone().maybeStack( 1 ).get() } ) );
		recipeCount++;

		AELog.info( "Told EE3 about " + recipeCount + " furnace recipes..." );
	}

	private static void addRecipe( ItemStack output, List<ItemStack> input )
	{
		AELog.debug( ">>> EE3 Recipe Register >>> Output: " + output.toString() + " >>> Input(s): " + input.toString() );
		RecipeRegistryProxy.addRecipe( output, input );
	}
}
