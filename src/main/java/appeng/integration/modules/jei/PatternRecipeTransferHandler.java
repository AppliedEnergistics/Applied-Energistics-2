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

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;

public class PatternRecipeTransferHandler<R extends Recipe<?>>
        extends RecipeTransferHandler<PatternEncodingTermMenu, R> {

    PatternRecipeTransferHandler(Class<PatternEncodingTermMenu> containerClass, Class<R> recipeClass,
            IRecipeTransferHandlerHelper helper) {
        super(containerClass, recipeClass, helper);
    }

    @Override
    protected IRecipeTransferError doTransferRecipe(PatternEncodingTermMenu menu, R recipe,
            IRecipeLayout recipeLayout, Player player, boolean maxTransfer) {
        if (menu.getMode() != EncodingMode.PROCESSING && !(recipe instanceof CraftingRecipe)) {
            return this.helper.createUserErrorWithTooltip(
                    new TranslatableComponent("jei.ae2.requires_processing_mode"));
        }
        return null;
    }

}
