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
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IMaterials;
import appeng.api.features.IInscriberRecipe;
import appengee3compat.core.AELog;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;
import net.minecraft.item.ItemStack;

import java.util.List;


public class RegisterInscriber
{
	public static void initRecipes()
	{
		int recipeCount = 0;

		for ( IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
		{
			final IDefinitions definitions = AEApi.instance().definitions();
			final IMaterials materials = definitions.materials();

			List<ItemStack> input = recipe.getInputs();

			for ( ItemStack top : recipe.getTopOptional().asSet() )
			{
				if ( !top.isItemEqual( materials.calcProcessorPress().maybeStack( 1 ).get() ) && !top.isItemEqual( materials.engProcessorPress().maybeStack( 1 ).get() ) && !top.isItemEqual( materials.logicProcessorPress().maybeStack( 1 ).get() ) && !top.isItemEqual( materials.siliconPress().maybeStack( 1 ).get() ) )
				{
					input.add( top );
				}
			}

			for ( ItemStack bottom : recipe.getBottomOptional().asSet() )
			{
				input.add( bottom );
			}

			AELog.debug( ">>> EE3 Recipe Register >>> Output: " + recipe.getOutput().toString() + " >>> Input(s): " + input.toString() );
			recipeCount++;
			RecipeRegistryProxy.addRecipe( recipe.getOutput(), input );
		}

		AELog.info( "Told EE3 about " + recipeCount + " inscriber recipes..." );
	}
}
