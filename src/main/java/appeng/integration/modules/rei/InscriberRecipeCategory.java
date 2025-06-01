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

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;

class InscriberRecipeCategory implements DisplayCategory<InscriberRecipeWrapper> {

    private static final int PADDING = 5;
    private static final int SLOT_INPUT_TOP = 0;
    private static final int SLOT_INPUT_MIDDLE = 1;
    private static final int SLOT_INPUT_BOTTOM = 2;

    static final CategoryIdentifier<InscriberRecipeWrapper> ID = CategoryIdentifier
            .of(AppEng.makeId("ae2.inscriber"));

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AEBlocks.INSCRIBER.stack());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.ae2.inscriber");
    }

    @Override
    public CategoryIdentifier<InscriberRecipeWrapper> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(InscriberRecipeWrapper recipeDisplay, Rectangle bounds) {
        ResourceLocation location = AppEng.makeId("textures/guis/inscriber.png");

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        var innerX = bounds.x + PADDING;
        var innerY = bounds.y + PADDING;
        widgets.add(Widgets.createTexturedWidget(location, innerX, innerY, 44, 15, 97, 64));

        List<EntryIngredient> ingredients = recipeDisplay.getInputEntries();
        EntryIngredient output = recipeDisplay.getOutputEntries().get(0);

        widgets.add(Widgets.createSlot(new Point(innerX + 1, innerY + 1)).disableBackground().markInput()
                .entries(ingredients.get(SLOT_INPUT_TOP)));
        widgets.add(Widgets.createSlot(new Point(innerX + 19, innerY + 24)).disableBackground().markInput()
                .entries(ingredients.get(SLOT_INPUT_MIDDLE)));
        widgets.add(Widgets.createSlot(new Point(innerX + 1, innerY + 47)).disableBackground().markInput()
                .entries(ingredients.get(SLOT_INPUT_BOTTOM)));
        widgets.add(Widgets.createSlot(new Point(innerX + 69, innerY + 25)).disableBackground().markOutput()
                .entries(output));

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 64 + 2 * PADDING;
    }

    @Override
    public int getDisplayWidth(InscriberRecipeWrapper display) {
        return 97 + 2 * PADDING;
    }
}
