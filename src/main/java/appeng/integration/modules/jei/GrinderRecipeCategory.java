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
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;


class GrinderRecipeCategory implements IRecipeCategory<GrinderRecipeWrapper>, IRecipeCategoryRegistration {

    public static final String UID = "appliedenergistics2.grinder";

    private final String localizedName;

    private final IDrawable background;

    public GrinderRecipeCategory(IGuiHelper guiHelper) {
        this.localizedName = I18n.format("tile.appliedenergistics2.grindstone.name");

        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/grinder.png");
        this.background = guiHelper.createDrawable(location, 11, 16, 154, 70);
    }

    @Override
    public String getModName() {
        return AppEng.MOD_NAME;
    }

    @Override
    public String getUid() {
        return GrinderRecipeCategory.UID;
    }

    @Override
    public String getTitle() {
        return this.localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GrinderRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(0, true, 0, 0);
        itemStacks.init(1, false, 100, 46);
        itemStacks.init(2, false, 118, 46);
        itemStacks.init(3, false, 136, 46);

        itemStacks.set(ingredients);
    }

    @Override
    public void addRecipeCategories(IRecipeCategory... recipeCategories) {

    }

    @Override
    public IJeiHelpers getJeiHelpers() {
        return null;
    }
}
