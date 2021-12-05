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

import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferError.Type;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.helpers.IMenuCraftingPacket;

abstract class RecipeTransferHandler<T extends AbstractContainerMenu & IMenuCraftingPacket, R extends Recipe<?>>
        implements IRecipeTransferHandler<T, R> {

    private final Class<T> menuClass;
    private final Class<R> recipeClass;
    protected final IRecipeTransferHandlerHelper helper;

    RecipeTransferHandler(Class<T> menuClass, Class<R> recipeClass, IRecipeTransferHandlerHelper helper) {
        this.menuClass = menuClass;
        this.recipeClass = recipeClass;
        this.helper = helper;
    }

    @Override
    public final Class<T> getContainerClass() {
        return this.menuClass;
    }

    @Override
    public final IRecipeTransferError transferRecipe(T menu, R recipe, IRecipeLayout recipeLayout,
            Player player, boolean maxTransfer, boolean doTransfer) {
        final ResourceLocation recipeId = recipe.getId();

        if (recipeId == null) {
            return this.helper
                    .createUserErrorWithTooltip(new TranslatableComponent("jei.appliedenergistics2.missing_id"));
        }

        // Check that the recipe can actually be looked up via the manager, i.e. our facade recipes have an ID, but are
        // never registered with the recipe manager.
        boolean canSendReference = true;
        if (!player.getCommandSenderWorld().getRecipeManager().byKey(recipeId).isPresent()) {
            // Validate that the recipe is a shapeless or shapedrecipe, since we can serialize those
            if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
                return this.helper
                        .createUserErrorWithTooltip(new TranslatableComponent("jei.appliedenergistics2.missing_id"));
            }
            canSendReference = false;
        }

        if (!recipe.canCraftInDimensions(3, 3)) {
            return this.helper.createUserErrorWithTooltip(
                    new TranslatableComponent("jei.ae2.recipe_too_large"));
        }

        final IRecipeTransferError error = doTransferRecipe(menu, recipe, recipeLayout, player, maxTransfer);

        if (doTransfer && this.canTransfer(error)) {
            if (canSendReference) {
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipeId));
            } else {
                // To avoid earlier problems of too large packets being sent that crashed the client, as a fallback when
                // the recipe ID could not be resolved, we'll just send the displayed items.
                NonNullList<Ingredient> flatIngredients = NonNullList.withSize(9, Ingredient.EMPTY);
                ItemStack output = ItemStack.EMPTY;

                // Determine the first JEI slot that has an actual input, we'll use this to offset the crafting grid
                // target slot
                int firstInputSlot = recipeLayout.getItemStacks().getGuiIngredients().entrySet().stream()
                        .filter(e -> e.getValue().isInput()).mapToInt(Map.Entry::getKey).min().orElse(0);

                // Now map the actual ingredients into the output/input
                for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : recipeLayout.getItemStacks()
                        .getGuiIngredients().entrySet()) {
                    IGuiIngredient<ItemStack> item = entry.getValue();
                    if (item.getDisplayedIngredient() == null) {
                        continue;
                    }

                    int inputIndex = entry.getKey() - firstInputSlot;
                    if (item.isInput() && inputIndex < flatIngredients.size()) {
                        ItemStack displayedIngredient = item.getDisplayedIngredient();
                        if (displayedIngredient != null) {
                            flatIngredients.set(inputIndex, Ingredient.of(displayedIngredient));
                        }
                    } else if (!item.isInput() && output.isEmpty()) {
                        output = item.getDisplayedIngredient();
                    }
                }

                ShapedRecipe fallbackRecipe = new ShapedRecipe(recipeId, "", 3, 3, flatIngredients, output);
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(fallbackRecipe));
            }
        }

        return error;
    }

    @Override
    public Class<R> getRecipeClass() {
        return this.recipeClass;
    }

    protected abstract IRecipeTransferError doTransferRecipe(T menu, R recipe, IRecipeLayout recipeLayout,
            Player player, boolean maxTransfer);

    private boolean canTransfer(IRecipeTransferError error) {
        return error == null || error.getType() == Type.COSMETIC;
    }
}
