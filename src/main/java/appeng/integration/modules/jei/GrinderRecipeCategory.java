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
import java.util.Collections;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.handlers.GrinderOptionalResult;
import appeng.recipes.handlers.GrinderRecipe;

class GrinderRecipeCategory implements IRecipeCategory<GrinderRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(AppEng.MOD_ID, "grinder");

    private final String localizedName;

    private final IDrawable background;

    private final IDrawable icon;

    public GrinderRecipeCategory(IGuiHelper guiHelper) {
        this.localizedName = I18n.format("block.appliedenergistics2.grindstone");

        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/grinder.png");
        this.background = guiHelper.createDrawable(location, 11, 16, 154, 70);

        this.icon = guiHelper.createDrawableIngredient(AEBlocks.GRINDSTONE.stack());
    }

    @Override
    public ResourceLocation getUid() {
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
    public void setRecipe(IRecipeLayout recipeLayout, GrinderRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(0, true, 0, 0);
        itemStacks.init(1, false, 100, 46);
        itemStacks.init(2, false, 118, 46);
        itemStacks.init(3, false, 136, 46);

        itemStacks.set(ingredients);
    }

    @Override
    public Class<? extends GrinderRecipe> getRecipeClass() {
        return GrinderRecipe.class;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(GrinderRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getIngredient()));
        List<ItemStack> outputs = new ArrayList<>(3);
        outputs.add(recipe.getRecipeOutput());
        for (GrinderOptionalResult optionalResult : recipe.getOptionalResults()) {
            outputs.add(optionalResult.getResult());
        }
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    // FIXME USE SPECIAL INGREDIENT TYPE
    // FIXME @Override
    // FIXME public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY )
    // FIXME {
    // FIXME
    // FIXME FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
    // FIXME
    // FIXME int x = 118;
    // FIXME
    // FIXME final float scale = 0.85f;
    // FIXME final float invScale = 1 / scale;
    // FIXME GlStateManager.scale( scale, scale, 1 );
    // FIXME
    // FIXME if( this.recipe.getOptionalOutput() != null )
    // FIXME {
    // FIXME String text = String.format( "%d%%", (int) ( this.recipe.getOptionalChance() * 100 ) );
    // FIXME float width = fr.getStringWidth( text ) * scale;
    // FIXME int xScaled = Math.round( ( x + ( 18 - width ) / 2 ) * invScale );
    // FIXME fr.drawString( text, xScaled, (int) ( 65 * invScale ), Color.gray.getRGB() );
    // FIXME x += 18;
    // FIXME }
    // FIXME
    // FIXME if( this.recipe.getSecondOptionalOutput() != null )
    // FIXME {
    // FIXME String text = String.format( "%d%%", (int) ( this.recipe.getSecondOptionalChance() * 100 ) );
    // FIXME float width = fr.getStringWidth( text ) * scale;
    // FIXME int xScaled = Math.round( ( x + ( 18 - width ) / 2 ) * invScale );
    // FIXME fr.drawString( text, xScaled, (int) ( 65 * invScale ), Color.gray.getRGB() );
    // FIXME }
    // FIXME
    // FIXME GlStateManager.scale( invScale, invScale, 1 );
    // FIXME }

}
