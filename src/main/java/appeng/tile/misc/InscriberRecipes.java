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

package appeng.tile.misc;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import appeng.api.definitions.IComparableDefinition;
import appeng.api.features.InscriberProcessType;
import appeng.core.AppEng;
import appeng.core.api.definitions.ApiMaterials;
import appeng.items.materials.MaterialItem;
import appeng.recipes.handlers.InscriberRecipe;

/**
 * This class indexes all inscriber recipes to find valid inputs for the top and bottom optional slots. This speeds up
 * checks whether inputs for those two slots are valid.
 */
public final class InscriberRecipes {

    public static final ResourceLocation NAMEPLATE_RECIPE_ID = new ResourceLocation(AppEng.MOD_ID, "nameplate");

    private InscriberRecipes() {
    }

    /**
     * Returns an unmodifiable view of all registered inscriber recipes.
     */
    public static Iterable<InscriberRecipe> getRecipes(World world) {
        Collection<IRecipe<IInventory>> unfilteredRecipes = world.getRecipeManager().getRecipes(InscriberRecipe.TYPE)
                .values();
        return Iterables.filter(unfilteredRecipes, InscriberRecipe.class);
    }

    @Nullable
    public static InscriberRecipe findRecipe(World world, ItemStack input, ItemStack plateA, ItemStack plateB,
            boolean supportNamePress) {
        if (supportNamePress) {
            IComparableDefinition namePress = ApiMaterials.namePress();
            boolean isNameA = namePress.isSameAs(plateA);
            boolean isNameB = namePress.isSameAs(plateB);

            if (isNameA && isNameB || isNameA && plateB.isEmpty()) {
                return makeNamePressRecipe(input, plateA, plateB);
            } else if (plateA.isEmpty() && isNameB) {
                return makeNamePressRecipe(input, plateB, plateA);
            }
        }

        for (final InscriberRecipe recipe : getRecipes(world)) {
            // The recipe can be flipped at will
            final boolean matchA = recipe.getTopOptional().test(plateA) && recipe.getBottomOptional().test(plateB);
            final boolean matchB = recipe.getTopOptional().test(plateB) && recipe.getBottomOptional().test(plateA);

            if ((matchA || matchB) && recipe.getMiddleInput().test(input)) {
                return recipe;
            }
        }

        return null;
    }

    private static InscriberRecipe makeNamePressRecipe(ItemStack input, ItemStack plateA, ItemStack plateB) {
        String name = "";

        if (!plateA.isEmpty()) {
            final CompoundNBT tag = plateA.getOrCreateTag();
            name += tag.getString(MaterialItem.TAG_INSCRIBE_NAME);
        }

        if (!plateB.isEmpty()) {
            final CompoundNBT tag = plateB.getOrCreateTag();
            name += " " + tag.getString(MaterialItem.TAG_INSCRIBE_NAME);
        }

        final Ingredient startingItem = Ingredient.fromStacks(input.copy());
        final ItemStack renamedItem = input.copy();

        if (!name.isEmpty()) {
            renamedItem.setDisplayName(new StringTextComponent(name));
        } else {
            renamedItem.setDisplayName(null);
        }

        final InscriberProcessType type = InscriberProcessType.INSCRIBE;

        return new InscriberRecipe(NAMEPLATE_RECIPE_ID, "", startingItem, renamedItem,
                plateA.isEmpty() ? Ingredient.EMPTY : Ingredient.fromStacks(plateA),
                plateB.isEmpty() ? Ingredient.EMPTY : Ingredient.fromStacks(plateB), type);
    }

    /**
     * Checks if there is an inscriber recipe that supports the given combination of top/bottom presses. Both the given
     * combination and the reverse will be searched.
     */
    public static boolean isValidOptionalIngredientCombination(World world, ItemStack pressA, ItemStack pressB) {
        for (InscriberRecipe recipe : getRecipes(world)) {
            if (recipe.getTopOptional().test(pressA) && recipe.getBottomOptional().test(pressB)
                    || recipe.getTopOptional().test(pressB) && recipe.getBottomOptional().test(pressA)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if there is an inscriber recipe that would use the given item stack as an optional ingredient. Bottom and
     * top can be used interchangeably here, because the inscriber will flip the recipe if needed.
     */
    public static boolean isValidOptionalIngredient(World world, ItemStack is) {
        for (InscriberRecipe recipe : getRecipes(world)) {
            if (recipe.getTopOptional().test(is) || recipe.getBottomOptional().test(is)) {
                return true;
            }
        }

        return false;
    }

}
