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

import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DisassembleRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<DisassembleRecipe> SERIALIZER = new SpecialRecipeSerializer<>(
            DisassembleRecipe::new);

    private static final ItemStack MISMATCHED_STACK = ItemStack.EMPTY;

    private final Map<IItemDefinition, IItemDefinition> cellMappings;
    private final Map<IItemDefinition, IItemDefinition> nonCellMappings;

    public DisassembleRecipe(Identifier id) {
        super(id);

        final IDefinitions definitions = Api.instance().definitions();
        final IBlocks blocks = definitions.blocks();
        final IItems items = definitions.items();
        final IMaterials mats = definitions.materials();

        this.cellMappings = new HashMap<>(4);
        this.nonCellMappings = new HashMap<>(5);

        this.cellMappings.put(items.cell1k(), mats.cell1kPart());
        this.cellMappings.put(items.cell4k(), mats.cell4kPart());
        this.cellMappings.put(items.cell16k(), mats.cell16kPart());
        this.cellMappings.put(items.cell64k(), mats.cell64kPart());

        this.nonCellMappings.put(items.encodedPattern(), mats.blankPattern());
        this.nonCellMappings.put(blocks.craftingStorage1k(), mats.cell1kPart());
        this.nonCellMappings.put(blocks.craftingStorage4k(), mats.cell4kPart());
        this.nonCellMappings.put(blocks.craftingStorage16k(), mats.cell16kPart());
        this.nonCellMappings.put(blocks.craftingStorage64k(), mats.cell64kPart());
    }

    @Override
    public boolean matches(@Nonnull final CraftingInventory inv, @Nonnull final World w) {
        return !this.getOutput(inv).isEmpty();
    }

    @Nonnull
    private ItemStack getOutput(final Inventory inventory) {
        int itemCount = 0;
        ItemStack output = MISMATCHED_STACK;

        for (int slotIndex = 0; slotIndex < inventory.size(); slotIndex++) {
            final ItemStack stackInSlot = inventory.getStack(slotIndex);
            if (!stackInSlot.isEmpty()) {
                // needs a single input in the recipe
                itemCount++;
                if (itemCount > 1) {
                    return MISMATCHED_STACK;
                }

                // handle storage cells
                Optional<ItemStack> maybeCellOutput = this.getCellOutput(stackInSlot);
                if (maybeCellOutput.isPresent()) {
                    ItemStack storageCellStack = maybeCellOutput.get();
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

                    output = storageCellStack;
                }

                // handle crafting storage blocks
                output = this.getNonCellOutput(stackInSlot).orElse(output);
            }
        }

        return output;
    }

    @Nonnull
    private Optional<ItemStack> getCellOutput(final ItemStack compared) {
        for (final Map.Entry<IItemDefinition, IItemDefinition> entry : this.cellMappings.entrySet()) {
            if (entry.getKey().isSameAs(compared)) {
                return entry.getValue().maybeStack(1);
            }
        }

        return Optional.empty();
    }

    @Nonnull
    private Optional<ItemStack> getNonCellOutput(final ItemStack compared) {
        for (final Map.Entry<IItemDefinition, IItemDefinition> entry : this.nonCellMappings.entrySet()) {
            if (entry.getKey().isSameAs(compared)) {
                return entry.getValue().maybeStack(1);
            }
        }

        return Optional.empty();
    }

    @Nonnull
    @Override
    public ItemStack craft(@Nonnull final CraftingInventory inv) {
        return this.getOutput(inv);
    }

    @Override
    public boolean fits(int i, int i1) {
        return false;
    }

    @Nonnull
    @Override
    public RecipeSerializer<DisassembleRecipe> getSerializer() {
        return SERIALIZER;
    }

}