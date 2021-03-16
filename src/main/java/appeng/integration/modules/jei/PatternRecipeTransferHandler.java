/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020 Team Appliedenergistics, All rights reserved.
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

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import appeng.container.implementations.PatternTermContainer;

public class PatternRecipeTransferHandler extends RecipeTransferHandler<PatternTermContainer> {

    PatternRecipeTransferHandler(Class<PatternTermContainer> containerClass, IRecipeTransferHandlerHelper helper) {
        super(containerClass, helper);
    }

    @Override
    protected IRecipeTransferError doTransferRecipe(PatternTermContainer container, IRecipe<?> recipe,
            IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer) {
        if (container.isCraftingMode()
                && recipeLayout.getRecipeCategory().getUid() != VanillaRecipeCategoryUid.CRAFTING) {
            return this.helper
                    .createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.requires_processing_mode"));
        }

        if (recipe.getRecipeOutput().isEmpty()) {
            return this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.no_output"));
        }

        return null;
    }

    @Override
    protected boolean isCrafting() {
        return false;
    }

}
