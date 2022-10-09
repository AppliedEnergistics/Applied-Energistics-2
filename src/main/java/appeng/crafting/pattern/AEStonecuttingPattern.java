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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;

/**
 * Encodes patterns for the {@link net.minecraft.world.level.block.StonecutterBlock}.
 */
public class AEStonecuttingPattern implements IPatternDetails, IMolecularAssemblerSupportedPattern {
    // The slot index in the 3x3 crafting grid that we insert our item into (in the MAC)
    private static final int CRAFTING_GRID_SLOT = 4;

    private final AEItemKey definition;
    public final boolean canSubstitute;
    private final StonecutterRecipe recipe;
    private final Container testFrame;
    private final AEItemKey input;
    private final ItemStack output;
    private final IInput[] inputs;
    private final GenericStack[] outputs;

    /**
     * We cache results of isValid(...) calls for stacks that don't have NBT.
     */
    private final Map<Item, Boolean> isValidCache = new IdentityHashMap<>();

    public AEStonecuttingPattern(AEItemKey definition, Level level) {
        this.definition = definition;
        var tag = Objects.requireNonNull(definition.getTag());

        this.input = StonecuttingPatternEncoding.getInput(tag);
        this.canSubstitute = StonecuttingPatternEncoding.canSubstitute(tag);

        // Find recipe
        var recipeId = StonecuttingPatternEncoding.getRecipeId(tag);
        this.recipe = level.getRecipeManager().byType(RecipeType.STONECUTTING).get(recipeId);

        // Build frame and find output
        this.testFrame = new SimpleContainer(1);
        this.testFrame.setItem(0, input.toStack());

        if (!this.recipe.matches(testFrame, level)) {
            throw new IllegalStateException("The recipe " + recipeId + " no longer matches the encoded input.");
        }

        this.output = this.recipe.assemble(testFrame);
        if (this.output.isEmpty()) {
            throw new IllegalStateException("The recipe " + recipeId + " produced an empty item stack result.");
        }

        this.inputs = new IInput[] {
                new Input()
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
                && ((AEStonecuttingPattern) obj).definition.equals(definition);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public GenericStack[] getOutputs() {
        return outputs;
    }

    /**
     * Gets the {@link Ingredient} from the actual used recipe. Stonecutting recipes must have exactly one ingredient.
     */
    private Ingredient getRecipeIngredient() {
        return recipe.getIngredients().get(0);
    }

    public boolean isItemValid(AEItemKey key, Level level) {
        if (key == null) {
            return false;
        }

        if (!canSubstitute) {
            return input.equals(key);
        }

        var result = getTestResult(key);
        if (result != null) {
            return result;
        }

        // Fill frame and check result
        var previousStack = testFrame.removeItemNoUpdate(0);
        testFrame.setItem(0, key.toStack());

        var newResult = recipe.matches(testFrame, level) && ItemStack.matches(output, recipe.assemble(testFrame));

        setTestResult(key, newResult);

        // Restore old stack in the frame
        testFrame.setItem(0, previousStack);

        return newResult;
    }

    /**
     * Retrieve a previous result of testing whether <code>what</code> is a valid ingredient for <code>slot</code>.
     *
     * @return null if the result is unknown, otherwise indicates whether the key is valid or not.
     */
    @Nullable
    private Boolean getTestResult(AEItemKey what) {
        if (what == null || what.hasTag()) {
            return null;
        }

        return isValidCache.get(what.getItem());
    }

    private void setTestResult(AEItemKey what, boolean result) {
        if (what != null && !what.hasTag()) {
            isValidCache.put(what.getItem(), result);
        }
    }

    public boolean canSubstitute() {
        return canSubstitute;
    }

    @Override
    public ItemStack assemble(Container container, Level level) {
        // Jiggle the container around
        var testContainer = new SimpleContainer(2);
        testContainer.setItem(0, container.getItem(CRAFTING_GRID_SLOT));

        if (recipe.matches(testContainer, level)) {
            return recipe.assemble(testContainer);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        return slot == CRAFTING_GRID_SLOT && isItemValid(key, level);
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return slot == CRAFTING_GRID_SLOT;
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] table, CraftingGridAccessor gridAccessor) {
        var entry = table[0].getFirstEntry();
        if (entry != null && entry.getKey() instanceof AEItemKey itemKey) {
            gridAccessor.set(CRAFTING_GRID_SLOT, itemKey.toStack());
            table[0].remove(entry.getKey(), 1);
        }
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        // Stonecutter does not support remainders
        return NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
    }

    public AEItemKey getInput() {
        return input;
    }

    private class Input implements IInput {
        private final GenericStack[] possibleInputs;

        private Input() {
            if (!canSubstitute) {
                this.possibleInputs = new GenericStack[] { new GenericStack(input, 1) };
            } else {
                ItemStack[] matchingStacks = getRecipeIngredient().getItems();
                this.possibleInputs = new GenericStack[matchingStacks.length + 1];
                // Ensure that the stack chosen by the user gets precedence.
                this.possibleInputs[0] = new GenericStack(input, 1);
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
                return AEStonecuttingPattern.this.isItemValid(itemKey, level);
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
