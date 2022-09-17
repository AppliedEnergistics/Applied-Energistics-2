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


import appeng.core.AppEng;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;


class InscriberRecipeCategory implements IRecipeCategory<InscriberRecipeWrapper> {

    private static final int SLOT_INPUT_TOP = 0;
    private static final int SLOT_INPUT_MIDDLE = 1;
    private static final int SLOT_INPUT_BOTTOM = 2;
    private static final int SLOT_OUTPUT = 3;

    static final String UID = "appliedenergistics2.inscriber";

    private final IDrawable background;

    private final String localizedName;

    private final IDrawableAnimated progress;

    public InscriberRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/inscriber.png");
        this.background = guiHelper.createDrawable(location, 44, 15, 97, 64);
        this.localizedName = I18n.format("tile.appliedenergistics2.inscriber.name");

        IDrawableStatic progressDrawable = guiHelper.createDrawable(location, 135, 177, 6, 18, 24, 0, 91, 0);
        this.progress = guiHelper.createAnimatedDrawable(progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return this.localizedName;
    }

    /**
     * Return the name of the mod associated with this recipe category.
     * Used for the recipe category tab's tooltip.
     *
     * @since JEI 4.5.0
     */
    @Override
    public String getModName() {
        return AppEng.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        this.progress.draw(minecraft);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, InscriberRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(SLOT_INPUT_TOP, true, 0, 0);
        itemStacks.init(SLOT_INPUT_MIDDLE, true, 18, 23);
        itemStacks.init(SLOT_INPUT_BOTTOM, true, 0, 46);
        itemStacks.init(SLOT_OUTPUT, false, 68, 24);

        itemStacks.set(ingredients);
    }
}
