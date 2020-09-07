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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import appeng.container.implementations.CraftingTermContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;

public class CraftingRecipeTransferHandler extends RecipeTransferHandler<CraftingTermContainer> {

    CraftingRecipeTransferHandler(Class<CraftingTermContainer> containerClass, IRecipeTransferHandlerHelper helper) {
        super(containerClass, helper);
    }

    @Override
    protected IRecipeTransferError doTransferRecipe(CraftingTermContainer container, IRecipe<?> recipe,
            IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer) {
        return null;
    }

    @Override
    protected void sendPacket(ResourceLocation recipeId) {
        NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipeId.toString(), true));

    }
}
