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


import appeng.api.AEApi;
import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IStorageComponent;
import appeng.core.AppEng;
import appeng.tile.misc.TileCondenser;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;


class CondenserCategory implements IRecipeCategory<CondenserOutputWrapper> {

    public static final String UID = "appliedenergistics2.condenser";

    private final String localizedName;

    private final IDrawable background;

    private final IDrawable iconTrash;

    private final IDrawableAnimated progress;

    private final IDrawable iconButton;

    public CondenserCategory(IGuiHelper guiHelper) {
        this.localizedName = I18n.format("gui.appliedenergistics2.Condenser");

        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/condenser.png");
        this.background = guiHelper.createDrawable(location, 50, 25, 94, 48);

        ResourceLocation statesLocation = new ResourceLocation(AppEng.MOD_ID, "textures/guis/states.png");
        this.iconTrash = guiHelper.createDrawable(statesLocation, 241, 81, 14, 14, 28, 0, 2, 0);
        this.iconButton = guiHelper.createDrawable(statesLocation, 240, 240, 16, 16, 28, 0, 78, 0);

        IDrawableStatic progressDrawable = guiHelper.createDrawable(location, 178, 25, 6, 18, 0, 0, 70, 0);
        this.progress = guiHelper.createAnimatedDrawable(progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public String getUid() {
        return CondenserCategory.UID;
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
        this.iconTrash.draw(minecraft);
        this.iconButton.draw(minecraft);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CondenserOutputWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, false, 54, 26);

        // Get all storage cells and cycle them through a fake input slot
        itemStacks.init(1, true, 50, 0);
        itemStacks.set(1, this.getViableStorageComponents(recipeWrapper));

        // This only sets the output
        itemStacks.set(ingredients);
    }

    private List<ItemStack> getViableStorageComponents(CondenserOutputWrapper recipeWrapper) {
        CondenserOutput condenserOutput = recipeWrapper.getCondenserOutput();
        IMaterials materials = AEApi.instance().definitions().materials();
        List<ItemStack> viableComponents = new ArrayList<>();
        materials.cell1kPart().maybeStack(1).ifPresent(itemStack -> this.addViableComponent(condenserOutput, viableComponents, itemStack));
        materials.cell4kPart().maybeStack(1).ifPresent(itemStack -> this.addViableComponent(condenserOutput, viableComponents, itemStack));
        materials.cell16kPart().maybeStack(1).ifPresent(itemStack -> this.addViableComponent(condenserOutput, viableComponents, itemStack));
        materials.cell64kPart().maybeStack(1).ifPresent(itemStack -> this.addViableComponent(condenserOutput, viableComponents, itemStack));
        return viableComponents;
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<ItemStack> viableComponents, ItemStack itemStack) {
        IStorageComponent comp = (IStorageComponent) itemStack.getItem();
        int storage = comp.getBytes(itemStack) * TileCondenser.BYTE_MULTIPLIER;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add(itemStack);
        }
    }
}
