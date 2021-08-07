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
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
import appeng.api.implementations.items.IStorageComponent;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.client.gui.Icon;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

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
        this.icon = guiHelper.createDrawableIngredient(AEBlocks.CONDENSER.stack());

        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/condenser.png");
        this.background = guiHelper.createDrawable(location, 50, 25, 94, 48);

        // This is shown on the "input slot" for condenser operations to indicate that any item can be used
        this.iconTrash = new IconDrawable(Icon.BACKGROUND_TRASH, 1, 27);
        this.iconButton = new IconDrawable(Icon.TOOLBAR_BUTTON_BACKGROUND, 78, 26);

        IDrawableStatic progressDrawable = guiHelper.drawableBuilder(location, 178, 25, 6, 18).addPadding(0, 0, 70, 0)
                .build();
        this.progress = guiHelper.createAnimatedDrawable(progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM,
                false);

        this.buttonIcons = new EnumMap<>(CondenserOutput.class);

        this.buttonIcons.put(CondenserOutput.MATTER_BALLS,
                new IconDrawable(Icon.CONDENSER_OUTPUT_MATTER_BALL, 78, 26));
        this.buttonIcons.put(CondenserOutput.SINGULARITY,
                new IconDrawable(Icon.CONDENSER_OUTPUT_SINGULARITY, 78, 26));
    }

    private ItemStack getOutput(CondenserOutput recipe) {
        switch (recipe) {
            case MATTER_BALLS:
                return AEItems.MATTER_BALL.stack();
            case SINGULARITY:
                return AEItems.SINGULARITY.stack();
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
    public void draw(CondenserOutput recipe, PoseStack poseStack, double mouseX, double mouseY) {
        this.progress.draw(poseStack);
        this.iconTrash.draw(poseStack);
        this.iconButton.draw(poseStack);

        IDrawable buttonIcon = this.buttonIcons.get(recipe);
        if (buttonIcon != null) {
            buttonIcon.draw(poseStack);
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
        List<ItemStack> viableComponents = new ArrayList<>();
        this.addViableComponent(condenserOutput, viableComponents, AEItems.ITEM_1K_CELL_COMPONENT.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.ITEM_4K_CELL_COMPONENT.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.ITEM_16K_CELL_COMPONENT.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.ITEM_64K_CELL_COMPONENT.stack());
        return viableComponents;
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<ItemStack> viableComponents,
            ItemStack itemStack) {
        IStorageComponent comp = (IStorageComponent) itemStack.getItem();
        int storage = comp.getBytes(itemStack) * CondenserBlockEntity.BYTE_MULTIPLIER;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add(itemStack);
        }
    }

    @Override
    public List<Component> getTooltipStrings(CondenserOutput output, double mouseX, double mouseY) {

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

            return Lists.newArrayList(new TranslatableComponent(key));
        }
        return Collections.emptyList();
    }

}
