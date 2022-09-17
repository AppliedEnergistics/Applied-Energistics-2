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


import appeng.items.parts.ItemFacade;
import mezz.jei.api.recipe.*;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;


/**
 * This plugin will dynamically add facade recipes for any item that can be turned into a facade.
 */
class FacadeRegistryPlugin implements IRecipeRegistryPlugin {

    private final ItemFacade itemFacade;

    private final ItemStack cableAnchor;

    FacadeRegistryPlugin(ItemFacade itemFacade, ItemStack cableAnchor) {
        this.itemFacade = itemFacade;
        this.cableAnchor = cableAnchor;
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        if (focus.getMode() == IFocus.Mode.OUTPUT && focus.getValue() instanceof ItemStack) {
            // Looking up how a certain facade is crafted
            ItemStack itemStack = (ItemStack) focus.getValue();
            if (itemStack.getItem() instanceof ItemFacade) {
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
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (!VanillaRecipeCategoryUid.CRAFTING.equals(recipeCategory.getUid())) {
            return Collections.emptyList();
        }

        if (focus.getMode() == IFocus.Mode.OUTPUT && focus.getValue() instanceof ItemStack) {
            // Looking up how a certain facade is crafted
            ItemStack itemStack = (ItemStack) focus.getValue();
            if (itemStack.getItem() instanceof ItemFacade) {
                ItemFacade facadeItem = (ItemFacade) itemStack.getItem();
                ItemStack textureItem = facadeItem.getTextureItem(itemStack);
                return Collections.singletonList((T) new FacadeRecipeWrapper(textureItem, this.cableAnchor, itemStack));
            }
        } else if (focus.getMode() == IFocus.Mode.INPUT && focus.getValue() instanceof ItemStack) {
            // Looking up if a certain block can be used to make a facade

            ItemStack itemStack = (ItemStack) focus.getValue();
            ItemStack facade = this.itemFacade.createFacadeForItem(itemStack, false);

            if (!facade.isEmpty()) {
                return Collections.singletonList((T) new FacadeRecipeWrapper(itemStack, this.cableAnchor, facade));
            }
        }

        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        return Collections.emptyList();
    }
}
