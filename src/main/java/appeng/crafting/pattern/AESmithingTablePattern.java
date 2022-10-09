/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.crafting.pattern;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;

/**
 * Encodes patterns for the {@link net.minecraft.world.level.block.SmithingTableBlock}.
 */
public class AESmithingTablePattern implements IPatternDetails, IMolecularAssemblerSupportedPattern {
    // The slot indices in the 3x3 crafting grid that we insert our item into (in the MAC)
    private static final int BASE_CRAFTING_GRID_SLOT = 3;
    private static final int ADDITION_CRAFTING_GRID_SLOT = 5;

    private final AEItemKey definition;
    public final boolean canSubstitute;
    private final UpgradeRecipe recipe;
    private final Container testFrame;
    private final ItemStack output;
    private final AEItemKey base;
    private final AEItemKey addition;
    private final IInput[] inputs;
    private final GenericStack[] outputs;

    public AESmithingTablePattern(AEItemKey definition, Level level) {
        this.definition = definition;
        var tag = Objects.requireNonNull(definition.getTag());

        this.base = SmithingTablePatternEncoding.getBase(tag);
        this.addition = SmithingTablePatternEncoding.getAddition(tag);
        this.canSubstitute = SmithingTablePatternEncoding.canSubstitute(tag);

        // Find recipe
        var recipeId = SmithingTablePatternEncoding.getRecipeId(tag);
        this.recipe = level.getRecipeManager().byType(RecipeType.SMITHING).get(recipeId);

        // Build frame and find output
        this.testFrame = new SimpleContainer(2);
        this.testFrame.setItem(0, base.toStack());
        this.testFrame.setItem(1, addition.toStack());

        if (!this.recipe.matches(testFrame, level)) {
            throw new IllegalStateException("The recipe " + recipeId + " no longer matches the encoded input.");
        }

        this.output = this.recipe.assemble(testFrame);
        if (this.output.isEmpty()) {
            throw new IllegalStateException("The recipe " + recipeId + " produced an empty item stack result.");
        }

        this.inputs = new IInput[] {
                new Input(base, recipe.base, BASE_CRAFTING_GRID_SLOT),
                new Input(addition, recipe.addition, ADDITION_CRAFTING_GRID_SLOT)
        };
        this.outputs = new GenericStack[] {
                GenericStack.fromItemStack(this.output)
        };
    }

    public ResourceLocation getRecipeId() {
        return recipe.getId();
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass()
                && ((AESmithingTablePattern) obj).definition.equals(definition);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    public AEItemKey getBase() {
        return base;
    }

    public AEItemKey getAddition() {
        return addition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public GenericStack[] getOutputs() {
        return outputs;
    }

    public boolean canSubstitute() {
        return canSubstitute;
    }

    @Override
    public ItemStack assemble(Container container, Level level) {
        // Jiggle the container around
        var testContainer = new SimpleContainer(2);
        testContainer.setItem(0, container.getItem(BASE_CRAFTING_GRID_SLOT));
        testContainer.setItem(1, container.getItem(ADDITION_CRAFTING_GRID_SLOT));

        if (recipe.matches(testContainer, level)) {
            return recipe.assemble(testContainer);
        }
        return ItemStack.EMPTY;
    }

    public boolean isItemValid(int gridSlot, AEItemKey key, Level level) {
        if (key == null) {
            return false;
        }

        if (!canSubstitute) {
            if (gridSlot == BASE_CRAFTING_GRID_SLOT) {
                return base.equals(key);
            } else if (gridSlot == ADDITION_CRAFTING_GRID_SLOT) {
                return addition.equals(key);
            } else {
                return false;
            }
        }

        // Fill frame and check result
        int containerSlot;
        if (gridSlot == BASE_CRAFTING_GRID_SLOT) {
            containerSlot = 0;
        } else if (gridSlot == ADDITION_CRAFTING_GRID_SLOT) {
            containerSlot = 1;
        } else {
            return false;
        }

        var previousStack = testFrame.removeItemNoUpdate(containerSlot);
        testFrame.setItem(containerSlot, key.toStack());

        var newResult = recipe.matches(testFrame, level) && ItemStack.matches(output, recipe.assemble(testFrame));

        // Restore old stack in the frame
        testFrame.setItem(containerSlot, previousStack);

        return newResult;
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return slot == BASE_CRAFTING_GRID_SLOT || slot == ADDITION_CRAFTING_GRID_SLOT;
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] table, CraftingGridAccessor gridAccessor) {
        var entry = table[0].getFirstEntry();
        if (entry != null && entry.getKey() instanceof AEItemKey itemKey) {
            gridAccessor.set(BASE_CRAFTING_GRID_SLOT, itemKey.toStack());
            table[0].remove(entry.getKey(), 1);
        }

        entry = table[1].getFirstEntry();
        if (entry != null && entry.getKey() instanceof AEItemKey itemKey) {
            gridAccessor.set(ADDITION_CRAFTING_GRID_SLOT, itemKey.toStack());
            table[1].remove(entry.getKey(), 1);
        }
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        // Smithing table does not support remainders
        return NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
    }

    private class Input implements IInput {
        private final GenericStack[] possibleInputs;
        private final int gridSlot;

        private Input(AEItemKey what, Ingredient recipeIngredient, int gridSlot) {
            this.gridSlot = gridSlot;

            if (!canSubstitute) {
                this.possibleInputs = new GenericStack[] { new GenericStack(what, 1) };
            } else {
                ItemStack[] matchingStacks = recipeIngredient.getItems();
                this.possibleInputs = new GenericStack[matchingStacks.length + 1];
                // Ensure that the stack chosen by the user gets precedence.
                this.possibleInputs[0] = new GenericStack(what, 1);
                for (int i = 0; i < matchingStacks.length; ++i) {
                    this.possibleInputs[i + 1] = GenericStack.fromItemStack(matchingStacks[i]);
                }
            }
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return possibleInputs;
        }

        @Override
        public long getMultiplier() {
            return 1;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            if (input.matches(possibleInputs[0])) {
                return true; // Exact match
            } else if (canSubstitute() && input instanceof AEItemKey itemKey) {
                return AESmithingTablePattern.this.isItemValid(gridSlot, itemKey, level);
            } else {
                return false;
            }
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
