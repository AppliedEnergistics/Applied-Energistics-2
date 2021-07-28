/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020 Team Appliedenergistics, All rights reserved.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.me.items.CraftingTermContainer;
import appeng.util.item.AEItemStack;

public class CraftingRecipeTransferHandler extends RecipeTransferHandler<CraftingTermContainer> {

    CraftingRecipeTransferHandler(Class<CraftingTermContainer> containerClass, IRecipeTransferHandlerHelper helper) {
        super(containerClass, helper);
    }

    @Override
    protected IRecipeTransferError doTransferRecipe(CraftingTermContainer container, Recipe<?> recipe,
            IRecipeLayout recipeLayout, Player player, boolean maxTransfer) {

        // Try to figure out if any slots have missing ingredients
        // Find every "slot" (in JEI parlance) that has no equivalent item in the item repo or player inventory
        List<Integer> missingSlots = new ArrayList<>();

        // We need to track how many of a given item stack we've already used for other slots in the recipe.
        // Otherwise recipes that need 4x<item> will not correctly show missing items if at least 1 of <item> is in
        // the grid.
        Map<IAEItemStack, Integer> reservedGridAmounts = new HashMap<>();

        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : recipeLayout.getItemStacks()
                .getGuiIngredients().entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            List<ItemStack> ingredients = ingredient.getAllIngredients();
            if (!ingredient.isInput() || ingredients.isEmpty()) {
                continue;
            }
            boolean found = false;
            // Player inventory is cheaper to check
            for (ItemStack itemStack : ingredients) {
                if (itemStack != null && player.getInventory().findSlotMatchingItem(itemStack) != -1) {
                    found = true;
                    break;
                }
            }
            // Then check the terminal screen's repository of network items
            if (!found) {
                for (ItemStack itemStack : ingredients) {
                    if (itemStack != null) {
                        // We use AE stacks to get an easily comparable item type key that ignores stack size
                        IAEItemStack aeStack = AEItemStack.fromItemStack(itemStack);
                        int reservedAmount = reservedGridAmounts.getOrDefault(aeStack, 0) + 1;
                        if (container.hasItemType(itemStack, reservedAmount)) {
                            reservedGridAmounts.put(aeStack, reservedAmount);
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (!found) {
                missingSlots.add(entry.getKey());
            }
        }

        if (!missingSlots.isEmpty()) {
            Component message = new TranslatableComponent("jei.appliedenergistics2.missing_items");
            return new TransferWarning(helper.createUserErrorForSlots(message, missingSlots));
        }

        return null;
    }

    @Override
    protected boolean isCrafting() {
        return true;
    }

    private static class TransferWarning implements IRecipeTransferError {

        private final IRecipeTransferError parent;

        public TransferWarning(IRecipeTransferError parent) {
            this.parent = parent;
        }

        @Override
        public Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public void showError(PoseStack matrixStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX,
                int recipeY) {
            this.parent.showError(matrixStack, mouseX, mouseY, recipeLayout, recipeX, recipeY);
        }

    }

}
