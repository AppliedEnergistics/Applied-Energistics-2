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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

import appeng.container.slot.CraftingMatrixSlot;
import appeng.container.slot.FakeCraftingMatrixSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.util.Platform;

class RecipeTransferHandler<T extends ScreenHandler> implements IRecipeTransferHandler<T> {

    private final Class<T> containerClass;

    RecipeTransferHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    @Override
    public Class<T> getContainerClass() {
        return this.containerClass;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(T container, IRecipeLayout recipeLayout, PlayerEntity player,
            boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }

        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks()
                .getGuiIngredients();

        final CompoundTag recipe = new CompoundTag();

        int slotIndex = 0;
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingredientEntry : ingredients.entrySet()) {
            IGuiIngredient<ItemStack> ingredient = ingredientEntry.getValue();
            if (!ingredient.isInput()) {
                continue;
            }

            for (final Slot slot : container.slots) {
                if (slot instanceof CraftingMatrixSlot || slot instanceof FakeCraftingMatrixSlot) {
                    if (slot.getSlotIndex() == slotIndex) {
                        final ListTag tags = new ListTag();
                        final List<ItemStack> list = new ArrayList<>();
                        final ItemStack displayed = ingredient.getDisplayedIngredient();

                        // prefer currently displayed item
                        if (displayed != null && !displayed.isEmpty()) {
                            list.add(displayed);
                        }

                        // prefer pure crystals.
                        for (ItemStack stack : ingredient.getAllIngredients()) {
                            if (Platform.isRecipePrioritized(stack)) {
                                list.add(0, stack);
                            } else {
                                list.add(stack);
                            }
                        }

                        for (final ItemStack is : list) {
                            final CompoundTag tag = new CompoundTag();
                            is.toTag(tag);
                            tags.add(tag);
                        }

                        recipe.put("#" + slot.getSlotIndex(), tags);
                        break;
                    }
                }
            }

            slotIndex++;
        }

        NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipe));

        return null;
    }
}
