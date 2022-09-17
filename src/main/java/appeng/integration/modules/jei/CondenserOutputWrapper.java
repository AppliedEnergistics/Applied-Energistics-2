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


import appeng.api.config.CondenserOutput;
import com.google.common.base.Splitter;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.HoverChecker;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;


class CondenserOutputWrapper implements IRecipeWrapper {
    private final ItemStack outputItem;

    private final CondenserOutput condenserOutput;

    private final HoverChecker buttonHoverChecker;

    private final IDrawable buttonIcon;

    CondenserOutputWrapper(CondenserOutput condenserOutput, ItemStack outputItem, IDrawable buttonIcon) {
        this.condenserOutput = condenserOutput;
        this.outputItem = outputItem;
        this.buttonIcon = buttonIcon;
        this.buttonHoverChecker = new HoverChecker(28, 28 + 16, 78, 78 + 16, 0);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setOutput(ItemStack.class, this.outputItem);
    }

    public CondenserOutput getCondenserOutput() {
        return this.condenserOutput;
    }

    @Nullable
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (this.buttonHoverChecker.checkHover(mouseX, mouseY)) {
            String key;

            switch (this.condenserOutput) {
                case MATTER_BALLS:
                    key = "gui.tooltips.appliedenergistics2.MatterBalls";
                    break;
                case SINGULARITY:
                    key = "gui.tooltips.appliedenergistics2.Singularity";
                    break;
                default:
                    return Collections.emptyList();
            }

            return Splitter.on("\\n").splitToList(I18n.format(key, this.condenserOutput.requiredPower));
        }
        return Collections.emptyList();
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        this.buttonIcon.draw(minecraft);
    }
}
