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

package appeng.integration.modules.jei;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import appeng.recipes.handlers.InscriberRecipe;

class InscriberRecipeWrapper implements Display {
    private final InscriberRecipe recipe;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public InscriberRecipeWrapper(InscriberRecipe recipe) {
        this.recipe = recipe;
        this.inputs = ImmutableList.of(
                EntryIngredients.ofIngredient(recipe.getTopOptional()),
                EntryIngredients.ofIngredient(recipe.getMiddleInput()),
                EntryIngredients.ofIngredient(recipe.getBottomOptional()));
        this.outputs = ImmutableList.of(EntryIngredients.of(recipe.getResultItem()));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return InscriberRecipeCategory.ID;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(recipe.getId());
    }
}
