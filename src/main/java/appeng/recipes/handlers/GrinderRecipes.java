/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.recipes.handlers;

import javax.annotation.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Recipe;

public final class GrinderRecipes {

    private GrinderRecipes() {
    }

    /**
     * Search all available Grinder recipes for a recipe matching the given input or null;
     */
    @Nullable
    public static GrinderRecipe findForInput(Level world, ItemStack input) {
        for (Recipe<Container> recipe : world.getRecipeManager().byType(GrinderRecipe.TYPE).values()) {
            GrinderRecipe grinderRecipe = (GrinderRecipe) recipe;
            if (grinderRecipe.getIngredient().test(input) && input.getCount() >= grinderRecipe.getIngredientCount()) {
                return grinderRecipe;
            }
        }
        return null;
    }

    /**
     * Checks if the given item stack is an ingredient in any grinder recipe, disregarding its current size.
     */
    public static boolean isValidIngredient(Level world, ItemStack stack) {
        for (Recipe<Container> recipe : world.getRecipeManager().byType(GrinderRecipe.TYPE).values()) {
            GrinderRecipe grinderRecipe = (GrinderRecipe) recipe;
            if (grinderRecipe.getIngredient().test(stack)) {
                return true;
            }
        }
        return false;
    }
}
