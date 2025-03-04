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

package appeng.integration.modules.rei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import appeng.recipes.handlers.InscriberRecipe;

class InscriberRecipeDisplay implements Display {
    private final RecipeHolder<InscriberRecipe> holder;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public InscriberRecipeDisplay(RecipeHolder<InscriberRecipe> holder) {
        this.holder = holder;
        var recipe = holder.value();
        this.inputs = new ArrayList<>();
        recipe.getTopOptional().map(EntryIngredients::ofIngredient).ifPresent(this.inputs::add);
        EntryIngredients.ofIngredient(recipe.getMiddleInput());
        recipe.getBottomOptional().map(EntryIngredients::ofIngredient).ifPresent(this.inputs::add);
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
        return Optional.of(holder.id().location());
    }
}
