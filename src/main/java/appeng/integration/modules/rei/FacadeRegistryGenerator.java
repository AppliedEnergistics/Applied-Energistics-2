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

package appeng.integration.modules.rei;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay;

import appeng.core.AppEng;
import appeng.core.FacadeCreativeTab;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;

/**
 * This plugin will dynamically add facade recipes for any item that can be turned into a facade.
 */
class FacadeRegistryGenerator implements DynamicDisplayGenerator<DefaultShapedDisplay> {

    private final Ingredient cableAnchor;
    private final FacadeItem itemFacade;

    FacadeRegistryGenerator() {
        this.itemFacade = AEItems.FACADE.asItem();
        this.cableAnchor = Ingredient.of(AEParts.CABLE_ANCHOR.stack());
    }

    @Override
    public Optional<List<DefaultShapedDisplay>> getRecipeFor(EntryStack<?> entry) {
        if (entry.getType() != VanillaEntryTypes.ITEM) {
            return Optional.empty(); // We only have items
        }

        // Looking up how a certain facade is crafted
        ItemStack itemStack = entry.castValue();
        if (itemStack.getItem() instanceof FacadeItem facadeItem) {
            ItemStack textureItem = facadeItem.getTextureItem(itemStack);
            return Optional.of(Collections.singletonList(make(textureItem, itemStack.copy())));
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<DefaultShapedDisplay>> getUsageFor(EntryStack<?> entry) {
        if (entry.getType() != VanillaEntryTypes.ITEM) {
            return Optional.empty(); // We only have items
        }

        // Looking up if a certain block can be used to make a facade
        ItemStack itemStack = entry.castValue();
        itemStack = itemStack.copy();
        itemStack.setCount(1);
        ItemStack facade = this.itemFacade.createFacadeForItem(itemStack, false);

        if (!facade.isEmpty()) {
            return Optional.of(Collections.singletonList(make(itemStack, facade)));
        }

        // Looking up uses for cable anchors
        if (cableAnchor.test(itemStack)) {
            return Optional.of(FacadeCreativeTab.getDisplayItems().stream()
                    .map(stack -> {
                        if (stack.getItem() instanceof FacadeItem facadeItem) {
                            ItemStack textureItem = facadeItem.getTextureItem(stack);
                            return make(textureItem, stack.copy());
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList());
        }

        return Optional.empty();
    }

    private DefaultShapedDisplay make(ItemStack textureItem, ItemStack result) {
        var ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
        ingredients.set(1, cableAnchor);
        ingredients.set(3, cableAnchor);
        ingredients.set(5, cableAnchor);
        ingredients.set(7, cableAnchor);
        ingredients.set(4, Ingredient.of(textureItem));
        var pattern = new ShapedRecipePattern(3, 3, ingredients, Optional.empty());

        result.setCount(4);

        // This id should only be used within REI and not really matter
        ResourceLocation id = AppEng.makeId("facade/" + Item.getId(textureItem.getItem()));
        return new DefaultShapedDisplay(
                new RecipeHolder<>(
                        id,
                        new ShapedRecipe("", CraftingBookCategory.MISC, pattern, result)));
    }

}
