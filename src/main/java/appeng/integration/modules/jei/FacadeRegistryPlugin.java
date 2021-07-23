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

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.AppEng;
import appeng.items.parts.FacadeItem;

/**
 * This plugin will dynamically add facade recipes for any item that can be turned into a facade.
 */
class FacadeRegistryPlugin implements IRecipeManagerPlugin {

    private final FacadeItem itemFacade;

    private final ItemStack cableAnchor;

    FacadeRegistryPlugin(FacadeItem itemFacade, ItemStack cableAnchor) {
        this.itemFacade = itemFacade;
        this.cableAnchor = cableAnchor;
    }

    @Override
    public <V> List<ResourceLocation> getRecipeCategoryUids(IFocus<V> focus) {
        if (focus.getMode() == IFocus.Mode.OUTPUT && focus.getValue() instanceof ItemStack) {
            // Looking up how a certain facade is crafted
            ItemStack itemStack = (ItemStack) focus.getValue();
            if (itemStack.getItem() instanceof FacadeItem) {
                return Collections.singletonList(VanillaRecipeCategoryUid.CRAFTING);
            }
        } else if (focus.getMode() == IFocus.Mode.INPUT && focus.getValue() instanceof ItemStack) {
            // Looking up if a certain block can be used to make a facade
            ItemStack itemStack = (ItemStack) focus.getValue();

            if (!this.itemFacade.createFacadeForItem(itemStack, true).isEmpty()) {
                return Collections.singletonList(VanillaRecipeCategoryUid.CRAFTING);
            }
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (!VanillaRecipeCategoryUid.CRAFTING.equals(recipeCategory.getUid())) {
            return Collections.emptyList();
        }

        if (focus.getMode() == IFocus.Mode.OUTPUT && focus.getValue() instanceof ItemStack) {
            // Looking up how a certain facade is crafted
            ItemStack itemStack = (ItemStack) focus.getValue();
            if (itemStack.getItem() instanceof FacadeItem) {
                FacadeItem facadeItem = (FacadeItem) itemStack.getItem();
                ItemStack textureItem = facadeItem.getTextureItem(itemStack);
                return Collections.singletonList((T) make(textureItem, this.cableAnchor, itemStack));
            }
        } else if (focus.getMode() == IFocus.Mode.INPUT && focus.getValue() instanceof ItemStack) {
            // Looking up if a certain block can be used to make a facade

            ItemStack itemStack = (ItemStack) focus.getValue();
            ItemStack facade = this.itemFacade.createFacadeForItem(itemStack, false);

            if (!facade.isEmpty()) {
                return Collections.singletonList((T) make(itemStack, this.cableAnchor, facade));
            }
        }

        return Collections.emptyList();
    }

    private ShapedRecipe make(ItemStack textureItem, ItemStack cableAnchor, ItemStack result) {
        // This id should only be used within JEI and not really matter
        ResourceLocation id = new ResourceLocation(AppEng.MOD_ID,
                "facade/" + textureItem.getItem().getRegistryName().toString().replace(':', '/'));

        NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
        ingredients.set(1, Ingredient.of(cableAnchor));
        ingredients.set(3, Ingredient.of(cableAnchor));
        ingredients.set(5, Ingredient.of(cableAnchor));
        ingredients.set(7, Ingredient.of(cableAnchor));
        ingredients.set(4, Ingredient.of(textureItem));

        result.setCount(4);

        return new ShapedRecipe(id, "", 3, 3, ingredients, result);
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        return Collections.emptyList();
    }
}
