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
import appeng.api.features.IGrinderEntry;
import appengee3compat.core.AELog;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;
import net.minecraft.item.ItemStack;

import java.util.Arrays;


public class RegisterGrinder
{
	public static void initRecipes()
	{
		int recipeCount = 0;

		for ( IGrinderEntry recipe : AEApi.instance().registries().grinder().getRecipes() )
		{
			AELog.debug( ">>> EE3 Recipe Register >>> Output: " + recipe.getOutput() + " >>> Input(s): " + recipe.getInput().toString() );
			recipeCount++;
			RecipeRegistryProxy.addRecipe( recipe.getOutput(), Arrays.asList( new ItemStack[] { recipe.getInput() } ) );
		}

		AELog.info( "Told EE3 about " + recipeCount + " grinder recipes..." );
	}
}
