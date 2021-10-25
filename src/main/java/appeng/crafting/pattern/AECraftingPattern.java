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

import com.google.common.base.Preconditions;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.definitions.AEItems;
import appeng.helpers.FluidContainerHelper;
import appeng.items.misc.WrappedFluidStack;
import appeng.menu.NullMenu;
import appeng.util.CraftingRemainders;
import appeng.util.item.AEItemStack;

public class AECraftingPattern implements IAEPatternDetails {
    private static final int CRAFTING_GRID_DIMENSION = 3;

    private final CompoundTag definition;
    public final boolean canSubstitute;
    public final boolean canSubstituteFluids;
    private final CraftingRecipe recipe;
    private final CraftingContainer testFrame;
    private final IAEItemStack[] sparseInputs;
    private final int[] sparseToCompressed = new int[9];
    private final Input[] inputs;
    private final IAEItemStack[] outputs;
    /**
     * We cache results of isValid(...) calls for stacks that don't have NBT.
     */
    @SuppressWarnings("unchecked")
    private final Map<Item, Boolean>[] isValidCache = new Map[9];

    public AECraftingPattern(CompoundTag definition, Level level) {
        this.definition = definition;
        this.canSubstitute = AEPatternHelper.canSubstitute(definition);
        this.canSubstituteFluids = AEPatternHelper.canSubstituteFluids(definition);
        this.sparseInputs = AEPatternHelper.getCraftingInputs(definition);

        // Find recipe
        var recipeId = AEPatternHelper.getRecipeId(definition);
        var recipe = level.getRecipeManager().byKey(recipeId).orElse(null);
        if (recipe == null || recipe.getType() != RecipeType.CRAFTING) {
            throw new IllegalStateException("recipe id is not a crafting recipe");
        }
        this.recipe = (CraftingRecipe) recipe;

        // Build frame and find output
        this.testFrame = new CraftingContainer(new NullMenu(), 3, 3);
        for (int i = 0; i < 9; ++i) {
            if (sparseInputs[i] != null) {
                testFrame.setItem(i, sparseInputs[i].createItemStack());
            }
        }
        this.outputs = new IAEItemStack[] { AEItemStack.fromItemStack(this.recipe.assemble(testFrame)) };

        // Compress inputs
        var condensedInputs = AEPatternHelper.condenseStacks(sparseInputs);
        this.inputs = new Input[condensedInputs.length];
        for (int i = 0; i < 9; ++i) {
            sparseToCompressed[i] = -1;
        }
        for (int j = 0; j < condensedInputs.length; ++j) {
            var condensedInput = condensedInputs[j];

            for (int i = 0; i < 9; ++i) {
                if (sparseInputs[i] != null && sparseInputs[i].equals(condensedInput)) {
                    if (inputs[j] == null) {
                        inputs[j] = new Input(i, condensedInput);
                    }
                    sparseToCompressed[i] = j;
                }
            }
        }
    }

    @Override
    public ItemStack copyDefinition() {
        var result = new ItemStack(AEItems.CRAFTING_PATTERN);
        result.setTag(definition.copy());
        return result;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public IAEStack[] getOutputs() {
        return outputs;
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
    public IAEFluidStack getValidFluid(int slot) {
        int compressed = sparseToCompressed[slot];

        if (compressed != -1) {
            var itemOrFluid = inputs[compressed].possibleInputs[0];

            if (itemOrFluid.getChannel() == StorageChannels.fluids()) {
                return itemOrFluid.cast(StorageChannels.fluids());
            }
        }

        return null;
    }

    public boolean isItemValid(int slot, IAEItemStack stack, Level level) {
        if (!canSubstitute) {
            return Objects.equals(sparseInputs[slot], stack);
        }

        var result = getTestResult(slot, stack);
        if (result != null) {
            return result;
        }

        // Fill frame and check result
        var previousStack = testFrame.removeItemNoUpdate(slot);
        var newStack = stack == null ? ItemStack.EMPTY : stack.createItemStack();
        testFrame.setItem(slot, newStack);

        var newResult = recipe.matches(testFrame, level) && outputs[0].equals(recipe.assemble(testFrame));

        setTestResult(slot, stack, newResult);

        // Restore old stack in the frame
        testFrame.setItem(slot, previousStack);

        return newResult;
    }

    @Nullable
    private Boolean getTestResult(int slot, IAEItemStack stack) {
        if (stack == null || stack.hasTagCompound()) {
            return null;
        }
        var cache = isValidCache[slot];
        if (cache == null) {
            return null;
        } else {
            return cache.get(stack.getItem());
        }
    }

    private void setTestResult(int slot, IAEItemStack stack, boolean result) {
        if (stack != null && !stack.hasTagCompound()) {
            var cache = isValidCache[slot];
            if (cache == null) {
                cache = isValidCache[slot] = new IdentityHashMap<>();
            }
            cache.put(stack.getItem(), result);
        }
    }

    @Override
    public IAEStack[] getSparseInputs() {
        return sparseInputs;
    }

    @Override
    public IAEStack[] getSparseOutputs() {
        return outputs;
    }

    public int getCompressedIndexFromSparse(int sparse) {
        return sparseToCompressed[sparse];
    }

    public ItemStack getOutput(CraftingContainer craftingContainer, Level level) {
        for (int x = 0; x < craftingContainer.getContainerSize(); x++) {
            ItemStack item = craftingContainer.getItem(x);
            var fluidStack = WrappedFluidStack.unwrap(item);
            if (fluidStack != null) {
                // If we receive a pure fluid stack, we'll convert it to the appropriate container item
                // If it matches the allowable input
                var validFluid = getValidFluid(x);
                if (fluidStack.equals(validFluid) && validFluid.getStackSize() == fluidStack.getStackSize()) {
                    continue;
                }
            }

            if (!isItemValid(x, AEItemStack.fromItemStack(item), level)) {
                return ItemStack.EMPTY;
            }
        }

        return outputs[0].createItemStack();
    }

    private IAEStack getItemOrFluidInput(int slot, IAEItemStack item) {
        var containedFluid = FluidContainerHelper.getContainedFluid(item.createItemStack());

        if (canSubstituteFluids && containedFluid != null) {
            // For the MVP, we only support buckets in regular shaped and shapeless recipes.
            if (recipe.getClass() == ShapedRecipe.class || recipe.getClass() == ShapelessRecipe.class) {
                if (item.getItem() instanceof BucketItem) {
                    return containedFluid;
                }
            }
        }

        return item;
    }

    private class Input implements IInput {
        private final int slot;
        private final IAEStack[] possibleInputs;
        private final long multiplier;

        private Input(int slot, IAEItemStack condensedInput) {
            this.slot = slot;
            this.multiplier = condensedInput.getStackSize();

            var itemOrFluidInput = getItemOrFluidInput(slot, sparseInputs[slot]);

            if (!canSubstitute) {
                this.possibleInputs = new IAEStack[] { itemOrFluidInput };
            } else {
                ItemStack[] matchingStacks = getRecipeIngredient(slot).getItems();
                this.possibleInputs = new IAEStack[matchingStacks.length + 1];
                // Ensure that the stack chosen by the user gets precedence.
                this.possibleInputs[0] = itemOrFluidInput;
                for (int i = 0; i < matchingStacks.length; ++i) {
                    this.possibleInputs[i + 1] = AEItemStack.fromItemStack(matchingStacks[i]);
                }
            }
        }

        @Override
        public IAEStack[] getPossibleInputs() {
            return possibleInputs;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(IAEStack input, Level level) {
            if (input.equals(possibleInputs[0])) {
                // Exact match
                return input.equals(possibleInputs[0]);
            } else if (canSubstitute() && input.getChannel() == StorageChannels.items()) {
                return AECraftingPattern.this.isItemValid(slot, input.cast(StorageChannels.items()), level);
            } else {
                return false;
            }
        }

        @Nullable
        @Override
        public IAEStack getContainerItem(IAEStack template) {
            if (template.getChannel() == StorageChannels.items()) {
                Preconditions.checkArgument(template.getStackSize() == 1);
                return CraftingRemainders.getRemainder(template.cast(StorageChannels.items()));
            }
            return null;
        }
    }
}
