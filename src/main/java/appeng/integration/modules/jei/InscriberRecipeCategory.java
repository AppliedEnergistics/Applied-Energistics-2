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

import net.minecraft.client.resources.I18n;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.*;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.Api;
import appeng.core.AppEng;
import appeng.recipes.handlers.InscriberRecipe;

class InscriberRecipeCategory implements IRecipeCategory<InscriberRecipe> {

    private static final int SLOT_INPUT_TOP = 0;
    private static final int SLOT_INPUT_MIDDLE = 1;
    private static final int SLOT_INPUT_BOTTOM = 2;
    private static final int SLOT_OUTPUT = 3;

    static final ResourceLocation UID = new ResourceLocation(AppEng.MOD_ID, "appliedenergistics2.inscriber");

    private final IDrawable background;

    private final String localizedName;

    private final IDrawableAnimated progress;

    private final IDrawable icon;

    public InscriberRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/inscriber.png");
        this.background = guiHelper.createDrawable(location, 44, 15, 97, 64);
        this.localizedName = I18n.format("block.appliedenergistics2.inscriber");

        IDrawableStatic progressDrawable = guiHelper.drawableBuilder(location, 135, 177, 6, 18).addPadding(24, 0, 91, 0)
                .build();
        this.progress = guiHelper.createAnimatedDrawable(progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM,
                false);

        this.icon = guiHelper.createDrawableIngredient(Api.INSTANCE.definitions().blocks().inscriber().stack(1));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
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
    public void setRecipe(IRecipeLayout recipeLayout, InscriberRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(SLOT_INPUT_TOP, true, 0, 0);
        itemStacks.init(SLOT_INPUT_MIDDLE, true, 18, 23);
        itemStacks.init(SLOT_INPUT_BOTTOM, true, 0, 46);
        itemStacks.init(SLOT_OUTPUT, false, 68, 24);

        itemStacks.set(ingredients);
    }

    @Override
    public Class<? extends InscriberRecipe> getRecipeClass() {
        return InscriberRecipe.class;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setIngredients(InscriberRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(recipe.getIngredients());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void draw(InscriberRecipe recipe, double mouseX, double mouseY) {
        this.progress.draw();
    }
}
