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
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.LanguageMap;
import appeng.core.Api;
import appeng.core.AppEng;

class InscriberRecipeCategory implements RecipeCategory<InscriberRecipeWrapper> {

    private static final int SLOT_INPUT_TOP = 0;
    private static final int SLOT_INPUT_MIDDLE = 1;
    private static final int SLOT_INPUT_BOTTOM = 2;
    private static final int SLOT_OUTPUT = 3;

    static final ResourceLocation UID = new ResourceLocation(AppEng.MOD_ID, "appliedenergistics2.inscriber");

    private final String localizedName;

    private final EntryStack icon;

    public InscriberRecipeCategory() {
        this.localizedName = LanguageMap.getInstance().method_4679("block.appliedenergistics2.inscriber");
        this.icon = EntryStack.create(Api.INSTANCE.definitions().blocks().inscriber().stack(1));
    }

    @Override
    public ResourceLocation getIdentifier() {
        return UID;
    }

    @Override
    public String getCategoryName() {
        return localizedName;
    }

    @Override
    public EntryStack getLogo() {
        return icon;
    }

    @Override
    public List<Widget> setupDisplay(InscriberRecipeWrapper recipeDisplay, Rectangle bounds) {
        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/inscriber.png");

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createTexturedWidget(location, bounds.x, bounds.y, 44, 15, 97, 64));

        List<List<EntryStack>> ingredients = recipeDisplay.getInputEntries();
        EntryStack output = recipeDisplay.getOutputEntries().get(0);

        widgets.add(Widgets.createSlot(new Point(bounds.x + 1, bounds.y + 1)).disableBackground().markInput()
                .entries(ingredients.get(SLOT_INPUT_TOP)));
        widgets.add(Widgets.createSlot(new Point(bounds.x + 19, bounds.y + 24)).disableBackground().markInput()
                .entries(ingredients.get(SLOT_INPUT_MIDDLE)));
        widgets.add(Widgets.createSlot(new Point(bounds.x + 1, bounds.y + 47)).disableBackground().markInput()
                .entries(ingredients.get(SLOT_INPUT_BOTTOM)));
        widgets.add(Widgets.createSlot(new Point(bounds.x + 69, bounds.y + 25)).disableBackground().markOutput()
                .entry(output));

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 64;
    }

    @Override
    public int getDisplayWidth(InscriberRecipeWrapper display) {
        return 97;
    }
}
