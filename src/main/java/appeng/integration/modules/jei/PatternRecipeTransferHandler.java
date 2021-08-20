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

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.plugin.common.DefaultPlugin;

import appeng.menu.me.items.PatternTermMenu;

public class PatternRecipeTransferHandler extends RecipeTransferHandler<PatternTermMenu> {

    public PatternRecipeTransferHandler() {
        super(PatternTermMenu.class);
    }

    protected Result doTransferRecipe(PatternTermMenu container, Display recipe, Context context) {

        if (container.isCraftingMode() && recipe.getCategoryIdentifier() != DefaultPlugin.CRAFTING) {
            return Result.createFailed(new TranslatableComponent("jei.appliedenergistics2.requires_processing_mode"));
        }

        if (recipe.getOutputEntries().isEmpty()) {
            return Result.createFailed(new TranslatableComponent("jei.appliedenergistics2.no_output"));
        }

        return null;
    }

}
