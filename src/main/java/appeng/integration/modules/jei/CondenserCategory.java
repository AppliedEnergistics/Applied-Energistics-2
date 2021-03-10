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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.api.config.CondenserOutput;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IStorageComponent;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.tile.misc.CondenserTileEntity;

class CondenserCategory implements IRecipeCategory<CondenserOutput> {

    public static final ResourceLocation UID = new ResourceLocation(AppEng.MOD_ID, "condenser");

    private final String localizedName;

    private final IDrawable background;

    private final IDrawable iconTrash;

    private final IDrawableAnimated progress;

    private final IDrawable iconButton;

    private final IDrawable icon;

    private final Map<CondenserOutput, IDrawable> buttonIcons;

    public CondenserCategory(IGuiHelper guiHelper) {
        this.localizedName = I18n.get("gui.appliedenergistics2.Condenser");
        this.icon = guiHelper.createDrawableIngredient(Api.INSTANCE.definitions().blocks().condenser().stack(1));

        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/condenser.png");
        this.background = guiHelper.createDrawable(location, 50, 25, 94, 48);

        ResourceLocation statesLocation = new ResourceLocation(AppEng.MOD_ID, "textures/guis/states.png");
        this.iconTrash = guiHelper.drawableBuilder(statesLocation, 241, 81, 14, 14).addPadding(28, 0, 2, 0).build();
        this.iconButton = guiHelper.drawableBuilder(statesLocation, 240, 240, 16, 16).addPadding(28, 0, 78, 0).build();

        IDrawableStatic progressDrawable = guiHelper.drawableBuilder(location, 178, 25, 6, 18).addPadding(0, 0, 70, 0)
                .build();
        this.progress = guiHelper.createAnimatedDrawable(progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM,
                false);

        this.buttonIcons = new EnumMap<>(CondenserOutput.class);

        this.buttonIcons.put(CondenserOutput.MATTER_BALLS,
                guiHelper.drawableBuilder(statesLocation, 16, 112, 14, 14).addPadding(28, 0, 78, 0).build());
        this.buttonIcons.put(CondenserOutput.SINGULARITY,
                guiHelper.drawableBuilder(statesLocation, 32, 112, 14, 14).addPadding(28, 0, 78, 0).build());
    }

    private ItemStack getOutput(CondenserOutput recipe) {
        switch (recipe) {
            case MATTER_BALLS:
                return Api.INSTANCE.definitions().materials().matterBall().stack(1);
            case SINGULARITY:
                return Api.INSTANCE.definitions().materials().singularity().stack(1);
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CondenserCategory.UID;
    }

    @Override
    public Class<? extends CondenserOutput> getRecipeClass() {
        return CondenserOutput.class;
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
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(CondenserOutput recipe, IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, getOutput(recipe));
    }

    @Override
    public void draw(CondenserOutput recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        this.progress.draw(matrixStack);
        this.iconTrash.draw(matrixStack);
        this.iconButton.draw(matrixStack);

        IDrawable buttonIcon = this.buttonIcons.get(recipe);
        if (buttonIcon != null) {
            buttonIcon.draw(matrixStack);
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CondenserOutput output, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, false, 54, 26);

        // Get all storage cells and cycle them through a fake input slot
        itemStacks.init(1, true, 50, 0);
        itemStacks.set(1, this.getViableStorageComponents(output));

        // This only sets the output
        itemStacks.set(ingredients);
    }

    private List<ItemStack> getViableStorageComponents(CondenserOutput condenserOutput) {
        IMaterials materials = Api.instance().definitions().materials();
        List<ItemStack> viableComponents = new ArrayList<>();
        materials.cell1kPart().maybeStack(1)
                .ifPresent(itemStack -> this.addViableComponent(condenserOutput, viableComponents, itemStack));
        materials.cell4kPart().maybeStack(1)
                .ifPresent(itemStack -> this.addViableComponent(condenserOutput, viableComponents, itemStack));
        materials.cell16kPart().maybeStack(1)
                .ifPresent(itemStack -> this.addViableComponent(condenserOutput, viableComponents, itemStack));
        materials.cell64kPart().maybeStack(1)
                .ifPresent(itemStack -> this.addViableComponent(condenserOutput, viableComponents, itemStack));
        return viableComponents;
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<ItemStack> viableComponents,
            ItemStack itemStack) {
        IStorageComponent comp = (IStorageComponent) itemStack.getItem();
        int storage = comp.getBytes(itemStack) * CondenserTileEntity.BYTE_MULTIPLIER;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add(itemStack);
        }
    }

    @Override
    public List<ITextComponent> getTooltipStrings(CondenserOutput output, double mouseX, double mouseY) {

        if (mouseX >= 28 && mouseX < 28 + 16 && mouseY >= 78 && mouseY < 78 + 16) {
            String key;

            switch (output) {
                case MATTER_BALLS:
                    key = "gui.tooltips.appliedenergistics2.MatterBalls";
                    break;
                case SINGULARITY:
                    key = "gui.tooltips.appliedenergistics2.Singularity";
                    break;
                default:
                    return Collections.emptyList();
            }

            return Lists.newArrayList(new TranslationTextComponent(key));
        }
        return Collections.emptyList();
    }

}
