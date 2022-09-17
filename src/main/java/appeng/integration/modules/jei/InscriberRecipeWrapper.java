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


import appeng.api.features.IInscriberRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class InscriberRecipeWrapper implements IRecipeWrapper {

    private final IInscriberRecipe recipe;

    public InscriberRecipeWrapper(IInscriberRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputSlots = new ArrayList<>(3);
        inputSlots.add(Collections.singletonList(this.recipe.getTopOptional().orElse(ItemStack.EMPTY)));
        inputSlots.add(this.recipe.getInputs());
        inputSlots.add(Collections.singletonList(this.recipe.getBottomOptional().orElse(ItemStack.EMPTY)));
        ingredients.setInputLists(ItemStack.class, inputSlots);

        ingredients.setOutput(ItemStack.class, this.recipe.getOutput());
    }
}
