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

package appeng.recipes.game;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.features.ItemDefinition;

public final class DisassembleRecipe extends SpecialRecipe {
    public static final IRecipeSerializer<DisassembleRecipe> SERIALIZER = new SpecialRecipeSerializer<>(
            DisassembleRecipe::new);

    static {
        SERIALIZER.setRegistryName(new ResourceLocation(AppEng.MOD_ID, "disassemble"));
    }

    private static final ItemStack MISMATCHED_STACK = ItemStack.EMPTY;

    private final Map<ItemDefinition, ItemDefinition> cellMappings;
    private final Map<ItemDefinition, ItemDefinition> nonCellMappings;

    public DisassembleRecipe(ResourceLocation id) {
        super(id);

        this.cellMappings = new HashMap<>(4);
        this.nonCellMappings = new HashMap<>(5);

        this.cellMappings.put(AEItems.CELL1K, AEItems.ITEM_1K_CELL_COMPONENT);
        this.cellMappings.put(AEItems.CELL4K, AEItems.ITEM_4K_CELL_COMPONENT);
        this.cellMappings.put(AEItems.CELL16K, AEItems.ITEM_16K_CELL_COMPONENT);
        this.cellMappings.put(AEItems.CELL64K, AEItems.ITEM_64K_CELL_COMPONENT);

        this.nonCellMappings.put(AEItems.ENCODED_PATTERN, AEItems.BLANK_PATTERN);
        this.nonCellMappings.put(AEBlocks.CRAFTING_STORAGE_1K, AEItems.ITEM_1K_CELL_COMPONENT);
        this.nonCellMappings.put(AEBlocks.CRAFTING_STORAGE_4K, AEItems.ITEM_4K_CELL_COMPONENT);
        this.nonCellMappings.put(AEBlocks.CRAFTING_STORAGE_16K, AEItems.ITEM_16K_CELL_COMPONENT);
        this.nonCellMappings.put(AEBlocks.CRAFTING_STORAGE_64K, AEItems.ITEM_64K_CELL_COMPONENT);
    }

    @Override
    public boolean matches(@Nonnull final CraftingInventory inv, @Nonnull final World w) {
        return !this.getOutput(inv).isEmpty();
    }

    @Nonnull
    private ItemStack getOutput(final IInventory inventory) {
        int itemCount = 0;
        ItemStack output = MISMATCHED_STACK;

        for (int slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++) {
            final ItemStack stackInSlot = inventory.getStackInSlot(slotIndex);
            if (!stackInSlot.isEmpty()) {
                // needs a single input in the recipe
                itemCount++;
                if (itemCount > 1) {
                    return MISMATCHED_STACK;
                }

                // handle storage cells
                output = this.getCellOutput(stackInSlot);
                if (!output.isEmpty()) {
                    // make sure the storage cell stackInSlot empty...
                    final IMEInventory<IAEItemStack> cellInv = Api.instance().registries().cell().getCellInventory(
                            stackInSlot, null, Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
                    if (cellInv != null) {
                        final IItemList<IAEItemStack> list = cellInv.getAvailableItems(
                                Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
                        if (!list.isEmpty()) {
                            return ItemStack.EMPTY;
                        }
                    }
                }

                // handle crafting storage blocks
                output = this.getNonCellOutput(stackInSlot);
            }
        }

        return output;
    }

    @Nonnull
    private ItemStack getCellOutput(final ItemStack compared) {
        for (final Map.Entry<ItemDefinition, ItemDefinition> entry : this.cellMappings.entrySet()) {
            if (entry.getKey().isSameAs(compared)) {
                return entry.getValue().stack();
            }
        }

        return ItemStack.EMPTY;
    }

    @Nonnull
    private ItemStack getNonCellOutput(final ItemStack compared) {
        for (final Map.Entry<ItemDefinition, ItemDefinition> entry : this.nonCellMappings.entrySet()) {
            if (entry.getKey().isSameAs(compared)) {
                return entry.getValue().stack();
            }
        }

        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull final CraftingInventory inv) {
        return this.getOutput(inv);
    }

    @Override
    public boolean canFit(int i, int i1) {
        return false;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<DisassembleRecipe> getSerializer() {
        return SERIALIZER;
    }

}
