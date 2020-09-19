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

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.TransferRecipeDisplay;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.helpers.IContainerCraftingPacket;

abstract class RecipeTransferHandler<T extends ScreenHandler & IContainerCraftingPacket>
        implements AutoTransferHandler {

    private final Class<T> containerClass;

    RecipeTransferHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    @Override
    public AutoTransferHandler.Result handle(AutoTransferHandler.Context context) {
        RecipeDisplay recipe = context.getRecipe();

        if (!containerClass.isInstance(context.getContainerScreen().getScreenHandler())) {
            return AutoTransferHandler.Result.createNotApplicable();
        }

        T container = containerClass.cast(context.getContainerScreen().getScreenHandler());

        final Identifier recipeId = recipe.getRecipeLocation().orElse(null);

        // Check that the recipe can actually be looked up via the manager, i.e. our
        // facade recipes
        // have an ID, but are never registered with the recipe manager.
        boolean canSendReference = true;
        if (recipeId == null || !context.getMinecraft().world.getRecipeManager().get(recipeId).isPresent()) {
            canSendReference = false;
        }

        if (recipe instanceof TransferRecipeDisplay) {
            TransferRecipeDisplay trd = (TransferRecipeDisplay) recipe;
            if (trd.getWidth() > 3 || trd.getHeight() > 3) {
                return AutoTransferHandler.Result.createFailed("jei.appliedenergistics2.recipe_too_large");
            }
        } else if (recipe.getInputEntries().size() > 9) {
            return AutoTransferHandler.Result.createFailed("jei.appliedenergistics2.recipe_too_large");
        }

        final AutoTransferHandler.Result error = doTransferRecipe(container, recipe, context);

        if (error != null) {
            return error;
        }

        if (context.isActuallyCrafting()) {
            if (canSendReference) {
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipeId, isCrafting()));
            } else {
                // To avoid earlier problems of too large packets being sent that crashed the
                // client,
                // as a fallback when the recipe ID could not be resolved, we'll just send the
                // displayed
                // items.
                DefaultedList<Ingredient> flatIngredients = DefaultedList.ofSize(9, Ingredient.EMPTY);
                ItemStack output = null;
                for (EntryStack entryStack : recipe.getResultingEntries().get(0)) {
                    if (entryStack.getType() == EntryStack.Type.ITEM) {
                        output = entryStack.getItemStack();
                    }
                }
                if (output == null || output.isEmpty()) {
                    return AutoTransferHandler.Result.createFailed("jei.appliedenergistics2.no_output");
                }

                // Now map the actual ingredients into the output/input
                for (int i = 0; i < recipe.getInputEntries().size(); i++) {
                    List<EntryStack> inputEntry = recipe.getInputEntries().get(i);
                    if (inputEntry.isEmpty()) {
                        continue;
                    }
                    EntryStack first = inputEntry.get(0);
                    if (i < flatIngredients.size()) {
                        ItemStack displayedIngredient = first.getItemStack();
                        if (displayedIngredient != null) {
                            flatIngredients.set(i, Ingredient.ofStacks(Stream.of(displayedIngredient)));
                        }
                    }
                }

                ShapedRecipe fallbackRecipe = new ShapedRecipe(recipeId, "", 3, 3, flatIngredients, output);
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(fallbackRecipe, isCrafting()));
            }
        }

        return Result.createSuccessful().blocksFurtherHandling();
    }

    protected abstract AutoTransferHandler.Result doTransferRecipe(T container, RecipeDisplay recipe,
            AutoTransferHandler.Context context);

    protected abstract boolean isCrafting();
}
