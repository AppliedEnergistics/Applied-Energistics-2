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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.recipes.handlers.GrinderOptionalResult;
import appeng.recipes.handlers.GrinderRecipe;

class GrinderRecipeWrapper implements Display {

    private final GrinderRecipe recipe;
    private final List<EntryIngredient> input;
    private final List<EntryIngredient> outputs;
    private final List<Double> outputChances;

    public GrinderRecipeWrapper(GrinderRecipe recipe) {
        this.recipe = recipe;
        this.input = CollectionUtils.map(recipe.getIngredients(),
                i -> EntryIngredient.of(CollectionUtils.map(i.getItems(), EntryStacks::of)));

        List<EntryIngredient> outputs = new ArrayList<>(1 + recipe.getOptionalResults().size());
        List<Double> outputChances = new ArrayList<>();
        outputs.add(EntryIngredients.of(recipe.getResultItem()));
        outputChances.add(100.0); // Primary output is guaranteed

        for (GrinderOptionalResult optionalResult : recipe.getOptionalResults()) {
            outputs.add(EntryIngredients.of(optionalResult.getResult()));
            outputChances.add(optionalResult.getChance() * 100.0);
        }
        this.outputs = ImmutableList.copyOf(outputs);
        this.outputChances = ImmutableList.copyOf(outputChances);
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return input;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    public List<Double> getOutputChances() {
        return outputChances;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return GrinderRecipeCategory.ID;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(recipe.getId());
    }
}
