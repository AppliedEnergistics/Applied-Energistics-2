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

import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.utils.CollectionUtils;

import appeng.recipes.handlers.GrinderOptionalResult;
import appeng.recipes.handlers.GrinderRecipe;

class GrinderRecipeWrapper implements TransferRecipeDisplay {

    private final GrinderRecipe recipe;
    private final List<List<EntryStack>> input;
    private final List<EntryStack> outputs;
    private final List<Double> outputChances;

    public GrinderRecipeWrapper(GrinderRecipe recipe) {
        this.recipe = recipe;
        this.input = CollectionUtils.map(recipe.getIngredients(),
                i -> CollectionUtils.map(i.getMatchingStacks(), EntryStack::create));

        List<EntryStack> outputs = new ArrayList<>();
        List<Double> outputChances = new ArrayList<>();
        outputs.add(EntryStack.create(recipe.getRecipeOutput()));
        outputChances.add(100.0); // Primary output is guaranteed

        for (GrinderOptionalResult optionalResult : recipe.getOptionalResults()) {
            outputs.add(EntryStack.create(optionalResult.getResult()));
            outputChances.add(optionalResult.getChance() * 100.0);
        }
        this.outputs = ImmutableList.copyOf(outputs);
        this.outputChances = ImmutableList.copyOf(outputChances);
    }

    @Override
    public List<List<EntryStack>> getInputEntries() {
        return input;
    }

    @Override
    public List<EntryStack> getOutputEntries() {
        return outputs;
    }

    public List<Double> getOutputChances() {
        return outputChances;
    }

    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return input;
    }

    @Override
    public ResourceLocation getRecipeCategory() {
        return GrinderRecipeCategory.UID;
    }

    @Override
    public Optional<ResourceLocation> getRecipeLocation() {
        return Optional.of(recipe.getId());
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo,
            Container container) {
        return input;
    }

}
