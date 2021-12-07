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
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.FluidContainerHelper;
import appeng.menu.NullMenu;
import appeng.util.CraftingRemainders;

public class AECraftingPattern implements IAEPatternDetails {
    private static final int CRAFTING_GRID_DIMENSION = 3;

    private final AEItemKey definition;
    public final boolean canSubstitute;
    public final boolean canSubstituteFluids;
    private final CraftingRecipe recipe;
    private final CraftingContainer testFrame;
    private final GenericStack[] sparseInputs;
    private final int[] sparseToCompressed = new int[9];
    private final Input[] inputs;
    private final ItemStack output;
    private final GenericStack[] outputsArray;
    /**
     * We cache results of isValid(...) calls for stacks that don't have NBT.
     */
    @SuppressWarnings("unchecked")
    private final Map<Item, Boolean>[] isValidCache = new Map[9];

    public AECraftingPattern(AEItemKey definition, Level level) {
        this.definition = definition;
        var tag = Objects.requireNonNull(definition.getTag());

        this.canSubstitute = CraftingPatternEncoding.canSubstitute(tag);
        this.canSubstituteFluids = CraftingPatternEncoding.canSubstituteFluids(tag);
        this.sparseInputs = CraftingPatternEncoding.getCraftingInputs(tag);

        // Find recipe
        var recipeId = CraftingPatternEncoding.getRecipeId(tag);
        var recipe = level.getRecipeManager().byType(RecipeType.CRAFTING).get(recipeId);
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) {
            throw new IllegalStateException("recipe id '" + recipeId + "' is not a valid crafting recipe");
        }
        this.recipe = craftingRecipe;

        // Build frame and find output
        this.testFrame = new CraftingContainer(new NullMenu(), 3, 3);
        for (int i = 0; i < 9; ++i) {
            if (sparseInputs[i] != null) {
                var itemKey = (AEItemKey) sparseInputs[i].what();
                testFrame.setItem(i, itemKey.toStack());
            }
        }
        if (!this.recipe.matches(testFrame, level)) {
            throw new IllegalStateException("The recipe " + recipe + " no longer matches the encoded input.");
        }

        this.output = this.recipe.assemble(testFrame);
        if (this.output.isEmpty()) {
            throw new IllegalStateException("The recipe " + recipeId + " produced an empty item stack result.");
        }
        this.outputsArray = new GenericStack[] { Objects.requireNonNull(GenericStack.fromItemStack(this.output)) };

        // Compress inputs
        var condensedInputs = AEPatternHelper.condenseStacks(sparseInputs);
        this.inputs = new Input[condensedInputs.length];
        for (int i = 0; i < 9; ++i) {
            sparseToCompressed[i] = -1;
        }
        for (int j = 0; j < condensedInputs.length; ++j) {
            var condensedInput = condensedInputs[j];

            for (int i = 0; i < 9; ++i) {
                if (sparseInputs[i] != null && sparseInputs[i].what().equals(condensedInput.what())) {
                    if (inputs[j] == null) {
                        inputs[j] = new Input(i, condensedInput);
                    }
                    sparseToCompressed[i] = j;
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && ((AECraftingPattern) obj).definition.equals(definition);
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
        return outputsArray;
    }

    /**
     * Gets the {@link Ingredient} from the actual used recipe for a given slot-index into {@link #sparseInputs}.
     * <p/>
     * Conversion is needed for two reasons: our sparse ingredients are always organized in a 3x3 grid, while Vanilla's
     * ingredient list will be condensed to the actual recipe's grid size. In addition, in our 3x3 grid, the user can
     * shift the actual recipe input to the right and down.
     */
    private Ingredient getRecipeIngredient(int slot) {

        if (recipe instanceof ShapedRecipe shapedRecipe) {

            return getShapedRecipeIngredient(slot, shapedRecipe.getWidth());
        } else {
            return getShapelessRecipeIngredient(slot);
        }
    }

    private Ingredient getShapedRecipeIngredient(int slot, int recipeWidth) {
        // Compute the offset of the user's input vs. crafting grid origin
        // Which is >0 if they have empty rows above or to the left of their input
        int topOffset = 0;
        if (sparseInputs[0] == null && sparseInputs[1] == null && sparseInputs[2] == null) {
            topOffset++; // First row is fully empty
            if (sparseInputs[3] == null && sparseInputs[4] == null && sparseInputs[5] == null) {
                topOffset++; // Second row is fully empty
            }
        }
        int leftOffset = 0;
        if (sparseInputs[0] == null && sparseInputs[3] == null && sparseInputs[6] == null) {
            leftOffset++; // First column is fully empty
            if (sparseInputs[1] == null && sparseInputs[4] == null && sparseInputs[7] == null) {
                leftOffset++; // Second column is fully empty
            }
        }

        // Compute the x,y of the slot, as-if the recipe was anchored to 0,0
        int slotX = slot % CRAFTING_GRID_DIMENSION - leftOffset;
        int slotY = slot / CRAFTING_GRID_DIMENSION - topOffset;

        // Compute the index into the recipe's ingredient list now
        int ingredientIndex = slotY * recipeWidth + slotX;

        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        if (ingredientIndex < 0 || ingredientIndex > ingredients.size()) {
            return Ingredient.EMPTY;
        }

        return ingredients.get(ingredientIndex);
    }

    private Ingredient getShapelessRecipeIngredient(int slot) {
        // We map the list of *filled* sparse inputs to the shapeless (ergo unordered)
        // ingredients. While these do not actually correspond to each other,
        // since both lists have the same length, the mapping is at least stable.
        int ingredientIndex = 0;
        for (int i = 0; i < slot; i++) {
            if (sparseInputs[i] != null) {
                ingredientIndex++;
            }
        }

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (ingredientIndex < ingredients.size()) {
            return ingredients.get(ingredientIndex);
        }

        return Ingredient.EMPTY;
    }

    /**
     * Return the fluid stack for the passed slot, or null if no fluid stack is accepted in the given slot.
     */
    @Nullable
    public GenericStack getValidFluid(int slot) {
        int compressed = sparseToCompressed[slot];

        if (compressed != -1) {
            var itemOrFluid = inputs[compressed].possibleInputs[0];

            if (itemOrFluid.what() instanceof AEFluidKey) {
                return itemOrFluid;
            }
        }

        return null;
    }

    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        if (!canSubstitute) {
            return sparseInputs[slot] == null && key == null
                    || sparseInputs[slot] != null && sparseInputs[slot].what().equals(key);
        }

        if (key == null) {
            return sparseInputs[slot] == null;
        }

        var result = getTestResult(slot, key);
        if (result != null) {
            return result;
        }

        // Fill frame and check result
        var previousStack = testFrame.removeItemNoUpdate(slot);
        testFrame.setItem(slot, key.toStack());

        var newResult = recipe.matches(testFrame, level) && ItemStack.matches(output, recipe.assemble(testFrame));

        setTestResult(slot, key, newResult);

        // Restore old stack in the frame
        testFrame.setItem(slot, previousStack);

        return newResult;
    }

    /**
     * Retrieve a previous result of testing whether <code>what</code> is a valid ingredient for <code>slot</code>.
     * @return null if the result is unknown, otherwise indicates whether the key is valid or not.
     */
    @Nullable
    private Boolean getTestResult(int slot, AEItemKey what) {
        if (what == null || what.hasTag()) {
            return null;
        }
        var cache = isValidCache[slot];
        if (cache == null) {
            return null;
        } else {
            return cache.get(what.getItem());
        }
    }

    private void setTestResult(int slot, AEItemKey what, boolean result) {
        if (what != null && !what.hasTag()) {
            var cache = isValidCache[slot];
            if (cache == null) {
                cache = isValidCache[slot] = new IdentityHashMap<>();
            }
            cache.put(what.getItem(), result);
        }
    }

    @Override
    public GenericStack[] getSparseInputs() {
        return sparseInputs;
    }

    @Override
    public GenericStack[] getSparseOutputs() {
        return outputsArray;
    }

    public int getCompressedIndexFromSparse(int sparse) {
        return sparseToCompressed[sparse];
    }

    public ItemStack getOutput(CraftingContainer craftingContainer, Level level) {
        for (int x = 0; x < craftingContainer.getContainerSize(); x++) {
            ItemStack item = craftingContainer.getItem(x);
            var stack = GenericStack.unwrapItemStack(item);
            if (stack != null) {
                // If we receive a pure fluid stack, we'll convert it to the appropriate container item
                // If it matches the allowable input
                var validFluid = getValidFluid(x);
                if (validFluid != null && validFluid.equals(stack)) {
                    continue;
                }
            }

            if (!isItemValid(x, AEItemKey.of(item), level)) {
                return ItemStack.EMPTY;
            }
        }
        return output;
    }

    private GenericStack getItemOrFluidInput(int slot, GenericStack item) {
        if (!(item.what() instanceof AEItemKey itemKey)) {
            return item;
        }

        var containedFluid = FluidContainerHelper.getContainedStack(itemKey.toStack());

        if (canSubstituteFluids && containedFluid != null) {
            // For the MVP, we only support buckets in regular shaped and shapeless recipes.
            if (recipe.getClass() == ShapedRecipe.class || recipe.getClass() == ShapelessRecipe.class) {
                if (itemKey.getItem() instanceof BucketItem) {
                    return new GenericStack(containedFluid.what(), containedFluid.amount());
                }
            }
        }

        return item;
    }

    private class Input implements IInput {
        private final int slot;
        private final GenericStack[] possibleInputs;
        private final long multiplier;

        private Input(int slot, GenericStack condensedInput) {
            this.slot = slot;
            this.multiplier = condensedInput.amount();

            var itemOrFluidInput = getItemOrFluidInput(slot, sparseInputs[slot]);

            if (!canSubstitute) {
                this.possibleInputs = new GenericStack[] { itemOrFluidInput };
            } else {
                ItemStack[] matchingStacks = getRecipeIngredient(slot).getItems();
                this.possibleInputs = new GenericStack[matchingStacks.length + 1];
                // Ensure that the stack chosen by the user gets precedence.
                this.possibleInputs[0] = itemOrFluidInput;
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
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            if (input.matches(possibleInputs[0])) {
                return true; // Exact match
            } else if (canSubstitute() && input instanceof AEItemKey itemKey) {
                return AECraftingPattern.this.isItemValid(slot, itemKey, level);
            } else {
                return false;
            }
        }

        @Nullable
        @Override
        public AEKey getContainerItem(AEKey template) {
            return CraftingRemainders.getRemainder(template);
        }
    }
}
