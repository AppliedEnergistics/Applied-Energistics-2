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
import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.LiveRecipeGenerator;
import me.shedaniel.rei.plugin.crafting.DefaultShapedDisplay;

import appeng.core.AppEng;
import appeng.items.parts.FacadeItem;

/**
 * This plugin will dynamically add facade recipes for any item that can be turned into a facade.
 */
class FacadeRegistryGenerator implements LiveRecipeGenerator<DefaultShapedDisplay> {

    private final FacadeItem itemFacade;

    private final ItemStack cableAnchor;

    FacadeRegistryGenerator(FacadeItem itemFacade, ItemStack cableAnchor) {
        this.itemFacade = itemFacade;
        this.cableAnchor = cableAnchor;
    }

    @Override
    public ResourceLocation getCategoryIdentifier() {
        return FacadeRecipeCategory.ID;
    }

    @Override
    public Optional<List<DefaultShapedDisplay>> getRecipeFor(EntryStack entry) {
        if (entry.getType() != EntryStack.Type.ITEM) {
            return Optional.empty(); // We only have items
        }

        // Looking up how a certain facade is crafted
        ItemStack itemStack = entry.getItemStack();
        if (itemStack.getItem() instanceof FacadeItem) {
            FacadeItem facadeItem = (FacadeItem) itemStack.getItem();
            ItemStack textureItem = facadeItem.getTextureItem(itemStack);
            return Optional.of(Collections.singletonList(make(textureItem, this.cableAnchor, itemStack)));
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<DefaultShapedDisplay>> getUsageFor(EntryStack entry) {
        if (entry.getType() != EntryStack.Type.ITEM) {
            return Optional.empty(); // We only have items
        }

        // Looking up if a certain block can be used to make a facade
        ItemStack itemStack = entry.getItemStack();
        ItemStack facade = this.itemFacade.createFacadeForItem(itemStack, false);

        if (!facade.isEmpty()) {
            return Optional.of(Collections.singletonList(make(itemStack, this.cableAnchor, facade)));
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<DefaultShapedDisplay>> getDisplaysGenerated(ClientHelper.ViewSearchBuilder builder) {
        // FABRIC FIXME Return a list of all facade recipes here
        return Optional.empty();
    }

    private DefaultShapedDisplay make(ItemStack textureItem, ItemStack cableAnchor, ItemStack result) {
        // This id should only be used within JEI and not really matter
        ResourceLocation id = AppEng.makeId("facade/" + Item.getIdFromItem(textureItem.getItem()));

        NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
        ingredients.set(1, Ingredient.fromStacks(cableAnchor));
        ingredients.set(3, Ingredient.fromStacks(cableAnchor));
        ingredients.set(5, Ingredient.fromStacks(cableAnchor));
        ingredients.set(7, Ingredient.fromStacks(cableAnchor));
        ingredients.set(4, Ingredient.fromStacks(textureItem));

        result.setCount(4);

        return new DefaultShapedDisplay(new ShapedRecipe(id, "", 3, 3, ingredients, result));
    }

}
