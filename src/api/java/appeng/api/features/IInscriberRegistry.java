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

package appeng.api.features;


import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;


/**
 * Lets you manipulate Inscriber Recipes, by adding or editing existing ones.
 *
 * @author thatsIch
 * @version rv5
 * @since rv2
 */
public interface IInscriberRegistry
{
	/**
	 * Extensible way to create an inscriber recipe.
	 *
	 * @return builder for inscriber recipes
	 */
	@Nonnull
	IInscriberRecipeBuilder builder();

	/**
	 * An immutable copy of currently registered recipes.
	 *
	 * Use the provided methods to actually modify the inscriber recipes.
	 *
	 * @see IInscriberRegistry#addRecipe(IInscriberRecipe)
	 * @see IInscriberRegistry#removeRecipe(IInscriberRecipe)
	 *
	 * @return currentlyRegisteredRecipes
	 */
	@Nonnull
	Collection<IInscriberRecipe> getRecipes();

	/**
	 * Optional items which are used in the top or bottom slot.
	 *
	 * @return set of all optional items
	 */
	@Nonnull
	Set<ItemStack> getOptionals();

	/**
	 * Get all registered items which are valid inputs.
	 *
	 * @return set of all input items
	 */
	@Nonnull
	Set<ItemStack> getInputs();

	/**
	 * add a new recipe the easy way, duplicates will not be added.
	 * Added recipes will be automatically added to the optionals and inputs.
	 *
	 * @param recipe new recipe
	 * 
	 * @return true, when successfully added
	 *
	 * @throws IllegalArgumentException if null is added
	 */
	boolean addRecipe( IInscriberRecipe recipe );

	/**
	 * Removes all equal recipes from the registry.
	 * 
	 * @param toBeRemovedRecipe to be removed recipe, can be null, makes just no sense.
	 * 
	 * @return true, when successfully removed
	 */
	boolean removeRecipe( IInscriberRecipe toBeRemovedRecipe );

}
