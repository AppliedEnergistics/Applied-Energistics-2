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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.EntryStack;

import appeng.container.slot.CraftingMatrixSlot;
import appeng.container.slot.FakeCraftingMatrixSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.mixins.SlotMixin;
import appeng.util.Platform;

class RecipeTransferHandler<T extends ScreenHandler> implements AutoTransferHandler {

    private final Class<T> containerClass;

    RecipeTransferHandler(Class<T> containerClass) {
        this.containerClass = containerClass;
    }

    @Override
    public Result handle(Context context) {

        ScreenHandler container = context.getContainerScreen().getScreenHandler();

        if (!containerClass.isInstance(container)) {
            return Result.createNotApplicable();
        }

        if (!context.isActuallyCrafting()) {
            // This is just to check whether the button is enabled
            return Result.createSuccessful();
        }

        List<List<EntryStack>> ingredients = context.getRecipe().getInputEntries();

        final CompoundTag recipe = new CompoundTag();

        for (int slotIndex = 0; slotIndex < ingredients.size(); slotIndex++) {
            List<EntryStack> ingredientEntry = ingredients.get(slotIndex);

            for (final Slot slot : container.slots) {
                if (slot instanceof CraftingMatrixSlot || slot instanceof FakeCraftingMatrixSlot) {
                    int containerSlotInvIdx = ((SlotMixin) slot).getIndex();
                    if (containerSlotInvIdx == slotIndex) {
                        final ListTag tags = new ListTag();
                        final List<ItemStack> list = new ArrayList<>();

                        // prefer pure crystals.
                        for (EntryStack stack : ingredientEntry) {
                            if (Platform.isRecipePrioritized(stack.getItemStack())) {
                                list.add(0, stack.getItemStack());
                            } else {
                                list.add(stack.getItemStack());
                            }
                        }

                        for (final ItemStack is : list) {
                            final CompoundTag tag = new CompoundTag();
                            is.toTag(tag);
                            tags.add(tag);
                        }

                        recipe.put("#" + containerSlotInvIdx, tags);
                        break;
                    }
                }
            }
        }

        NetworkHandler.instance().sendToServer(new JEIRecipePacket(recipe));

        return Result.createFailed(""); // this will return to the screen
    }

}
