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

import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.utils.CollectionUtils;

import appeng.recipes.handlers.InscriberRecipe;

class InscriberRecipeWrapper implements TransferRecipeDisplay {

    private final InscriberRecipe recipe;
    private final List<EntryStack> middleInput;
    private final List<EntryStack> topOptional;
    private final List<EntryStack> bottomOptional;
    private final EntryStack output;

    public InscriberRecipeWrapper(InscriberRecipe recipe) {
        this.recipe = recipe;
        this.topOptional = CollectionUtils.map(recipe.getTopOptional().getMatchingStacks(), EntryStack::create);
        this.middleInput = CollectionUtils.map(recipe.getMiddleInput().getMatchingStacks(), EntryStack::create);
        this.bottomOptional = CollectionUtils.map(recipe.getBottomOptional().getMatchingStacks(),
                EntryStack::create);
        this.output = EntryStack.create(recipe.getRecipeOutput());
    }

    @Override
    public List<List<EntryStack>> getInputEntries() {
        return ImmutableList.of(topOptional, middleInput, bottomOptional);
    }

    @Override
    public List<EntryStack> getOutputEntries() {
        return ImmutableList.of(output);
    }

    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return getInputEntries();
    }

    @Override
    public ResourceLocation getRecipeCategory() {
        return InscriberRecipeCategory.UID;
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
        return 3;
    }

    @Override
    public List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo,
            Container container) {
        return getInputEntries();
    }

}
