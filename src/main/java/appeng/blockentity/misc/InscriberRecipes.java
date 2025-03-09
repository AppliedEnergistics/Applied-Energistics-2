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

package appeng.blockentity.misc;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import appeng.api.ids.AEComponents;
import appeng.core.definitions.AEItems;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;

/**
 * This class indexes all inscriber recipes to find valid inputs for the top and bottom optional slots. This speeds up
 * checks whether inputs for those two slots are valid.
 */
public final class InscriberRecipes {

    private InscriberRecipes() {
    }

    /**
     * Returns an unmodifiable view of all registered inscriber recipes.
     */
    public static Iterable<RecipeHolder<InscriberRecipe>> getRecipes(ServerLevel level) {
        return level.recipeAccess().recipeMap().byType(AERecipeTypes.INSCRIBER);
    }

    @Nullable
    public static InscriberRecipe findRecipe(ServerLevel level, ItemStack input, ItemStack plateA, ItemStack plateB,
            boolean supportNamePress) {
        if (supportNamePress) {
            boolean isNameA = AEItems.NAME_PRESS.is(plateA);
            boolean isNameB = AEItems.NAME_PRESS.is(plateB);

            if (isNameA && isNameB || isNameA && plateB.isEmpty()) {
                return makeNamePressRecipe(input, plateA, plateB);
            } else if (plateA.isEmpty() && isNameB) {
                return makeNamePressRecipe(input, plateB, plateA);
            }
        }

        for (var holder : getRecipes(level)) {
            var recipe = holder.value();
            // The recipe can be flipped at will
            final boolean matchA = testIngredient(recipe.getTopOptional(), plateA)
                    && testIngredient(recipe.getBottomOptional(), plateB);
            final boolean matchB = testIngredient(recipe.getTopOptional(), plateB)
                    && testIngredient(recipe.getBottomOptional(), plateA);

            if ((matchA || matchB) && recipe.getMiddleInput().test(input)) {
                return recipe;
            }
        }

        return null;
    }

    private static InscriberRecipe makeNamePressRecipe(ItemStack input, ItemStack plateA, ItemStack plateB) {
        Component name = null;

        if (!plateA.isEmpty()) {
            var plateAName = plateA.get(AEComponents.NAME_PRESS_NAME);
            if (plateAName != null) {
                name = plateAName;
            }
        }

        if (!plateB.isEmpty()) {
            var plateBName = plateB.get(AEComponents.NAME_PRESS_NAME);
            if (plateBName != null) {
                if (name == null) {
                    name = plateBName;
                } else {
                    name = name.copy().append(" ").append(plateBName);
                }
            }
        }

        var startingItem = Ingredient.of(input.getItem());
        var renamedItem = input.copyWithCount(1);

        if (name != null) {
            renamedItem.set(DataComponents.CUSTOM_NAME, name);
        } else {
            renamedItem.remove(DataComponents.CUSTOM_NAME);
        }

        final InscriberProcessType type = InscriberProcessType.INSCRIBE;

        return new InscriberRecipe(startingItem, renamedItem,
                plateA.isEmpty() ? Optional.empty() : Optional.of(Ingredient.of(plateA.getItem())),
                plateB.isEmpty() ? Optional.empty() : Optional.of(Ingredient.of(plateB.getItem())), type);
    }

    /**
     * Checks if there is an inscriber recipe that supports the given combination of top/bottom presses. Both the given
     * combination and the reverse will be searched.
     */
    public static boolean isValidOptionalIngredientCombination(ServerLevel level, ItemStack pressA, ItemStack pressB) {
        for (var holder : getRecipes(level)) {
            var recipe = holder.value();
            if (testIngredient(recipe.getTopOptional(), pressA) && testIngredient(recipe.getBottomOptional(), pressB)
                    || testIngredient(recipe.getTopOptional(), pressB)
                            && testIngredient(recipe.getBottomOptional(), pressA)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if there is an inscriber recipe that would use the given item stack as an optional ingredient. Bottom and
     * top can be used interchangeably here, because the inscriber will flip the recipe if needed.
     */
    public static boolean isValidOptionalIngredient(ServerLevel level, ItemStack is) {
        for (var holder : getRecipes(level)) {
            var recipe = holder.value();
            if (testIngredient(recipe.getTopOptional(), is) || testIngredient(recipe.getBottomOptional(), is)) {
                return true;
            }
        }

        return false;
    }

    private static boolean testIngredient(Optional<Ingredient> ingredient, ItemStack stack) {
        return ingredient.map(value -> value.test(stack)).orElseGet(stack::isEmpty);
    }

}
