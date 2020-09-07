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

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.helpers.IContainerCraftingPacket;

abstract class RecipeTransferHandler<T extends Container & IContainerCraftingPacket>
        implements IRecipeTransferHandler<T> {

    private final Class<T> containerClass;
    protected final IRecipeTransferHandlerHelper helper;

    RecipeTransferHandler(Class<T> containerClass, IRecipeTransferHandlerHelper helper) {
        this.containerClass = containerClass;
        this.helper = helper;
    }

    @Override
    public final Class<T> getContainerClass() {
        return this.containerClass;
    }

    @Override
    public final IRecipeTransferError transferRecipe(T container, Object recipe, IRecipeLayout recipeLayout,
            PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        if (!(recipe instanceof IRecipe)) {
            return this.helper.createInternalError();
        }
        final IRecipe<?> irecipe = (IRecipe<?>) recipe;
        final ResourceLocation recipeId = irecipe.getId();

        if (recipeId == null) {
            return this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.missing_id"));
        }

        // Check that the recipe can actually be looked up via the manager, i.e. our
        // facade recipes
        // have an ID, but are never registered with the recipe manager.
        boolean canSendReference = true;
        if (!player.getEntityWorld().getRecipeManager().getRecipe(recipeId).isPresent()) {
            // Validate that the recipe is a shapeless or shapedrecipe, since we can
            // serialize those
            if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
                return this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.missing_id"));
            }
            canSendReference = false;
        }

        if (!irecipe.canFit(3, 3)) {
            return this.helper.createUserErrorWithTooltip(I18n.format("jei.appliedenergistics2.recipe_too_large"));
        }

        final IRecipeTransferError error = doTransferRecipe(container, irecipe, recipeLayout, player, maxTransfer);

        if (error != null) {
            return error;
        }

        if (doTransfer) {
            if (canSendReference) {
                NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipeId, isCrafting()));
            } else if (recipe instanceof ShapedRecipe) {
                NetworkHandler.instance().sendToServer(new JEIRecipePacket((ShapedRecipe) recipe, isCrafting()));
            } else if (recipe instanceof ShapelessRecipe) {
                NetworkHandler.instance().sendToServer(new JEIRecipePacket((ShapelessRecipe) recipe, isCrafting()));
            }
        }

        return null;
    }

    protected abstract IRecipeTransferError doTransferRecipe(T container, IRecipe<?> recipe, IRecipeLayout recipeLayout,
            PlayerEntity player, boolean maxTransfer);

    protected abstract boolean isCrafting();
}
