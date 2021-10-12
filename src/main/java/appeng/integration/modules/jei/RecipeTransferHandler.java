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

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.helpers.IMenuCraftingPacket;

abstract class RecipeTransferHandler<T extends AbstractContainerMenu & IMenuCraftingPacket>
        implements TransferHandler {

    private final Class<T> containerClass;

    RecipeTransferHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    @Override
    public Result handle(Context context) {
        if (!containerClass.isInstance(context.getMenu())) {
            return Result.createNotApplicable();
        }

        Display recipe = context.getDisplay();

        T menu = containerClass.cast(context.getMenu());

        var recipeId = recipe.getDisplayLocation().orElse(null);

        // Check that the recipe can actually be looked up via the manager, i.e. our
        // facade recipes
        // have an ID, but are never registered with the recipe manager.
        boolean canSendReference = true;
        if (recipeId == null || context.getMinecraft().level.getRecipeManager().byKey(recipeId).isEmpty()) {
            canSendReference = false;
        }

        if (recipe instanceof SimpleGridMenuDisplay gridDisplay) {
            if (gridDisplay.getWidth() > 3 || gridDisplay.getHeight() > 3) {
                return Result.createFailed(new TranslatableComponent("jei.appliedenergistics2.recipe_too_large"));
            }
        } else if (recipe.getInputEntries().size() > 9) {
            return Result.createFailed(new TranslatableComponent("jei.appliedenergistics2.recipe_too_large"));
        }

        final Result error = doTransferRecipe(menu, recipe, context);

        if (error != null) {
            return error;
        }

        if (context.isActuallyCrafting()) {
            if (canSendReference) {
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipeId));
            } else {
                // To avoid earlier problems of too large packets being sent that crashed the
                // client,
                // as a fallback when the recipe ID could not be resolved, we'll just send the
                // displayed
                // items.
                NonNullList<Ingredient> flatIngredients = NonNullList.withSize(9, Ingredient.EMPTY);
                ItemStack output = null;
                for (EntryStack<?> entryStack : recipe.getOutputEntries().get(0)) {
                    if (entryStack.getType() == VanillaEntryTypes.ITEM) {
                        output = entryStack.castValue();
                    }
                }
                if (output == null || output.isEmpty()) {
                    return Result.createFailed(new TranslatableComponent("jei.appliedenergistics2.no_output"));
                }

                // Now map the actual ingredients into the output/input
                for (int i = 0; i < recipe.getInputEntries().size(); i++) {
                    var inputIngredient = recipe.getInputEntries().get(i);
                    if (inputIngredient.isEmpty()) {
                        continue;
                    }
                    if (i < flatIngredients.size()) {
                        var ingredients = inputIngredient
                                .stream()
                                .filter(entry -> entry.getType() == VanillaEntryTypes.ITEM)
                                .map(entry -> (ItemStack) entry.getValue());
                        flatIngredients.set(i, Ingredient.of(ingredients));
                    }
                }

                ShapedRecipe fallbackRecipe = new ShapedRecipe(recipeId, "", 3, 3, flatIngredients, output);
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(fallbackRecipe));
            }
        }

        return Result.createSuccessful().blocksFurtherHandling();
    }

    protected abstract Result doTransferRecipe(T container, Display recipe,
            TransferHandler.Context context);
}
