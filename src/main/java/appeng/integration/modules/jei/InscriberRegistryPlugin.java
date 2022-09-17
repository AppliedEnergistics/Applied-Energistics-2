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


import appeng.api.AEApi;
import appeng.api.features.IInscriberRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;


/**
 * Exposes the inscriber registry recipes to JEI.
 */
class InscriberRegistryPlugin implements IRecipeRegistryPlugin {

    private final IInscriberRegistry inscriber = AEApi.instance().registries().inscriber();

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        if (!(focus.getValue() instanceof ItemStack)) {
            return Collections.emptyList();
        }

        if (focus.getMode() == IFocus.Mode.INPUT) {
            ItemStack input = (ItemStack) focus.getValue();
            for (ItemStack validInput : this.inscriber.getInputs()) {

            }
        }

        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        return null;
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        return null;
    }
}
