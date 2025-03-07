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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.localization.GuiText;

public class AECraftingPattern implements IPatternDetails, IMolecularAssemblerSupportedPattern {
    public static final int CRAFTING_GRID_DIMENSION = 3;
    public static final int CRAFTING_GRID_SLOTS = CRAFTING_GRID_DIMENSION * CRAFTING_GRID_DIMENSION;

    private final AEItemKey definition;
    public final boolean canSubstitute;
    public final boolean canSubstituteFluids;
    private final RecipeHolder<?> recipeHolder;
    private final CraftingRecipe recipe;
    private final List<GenericStack> sparseInputs;
    private final int[] sparseToCompressed = new int[9];
    private final Input[] inputs;
    private final ItemStack output;
    private final List<GenericStack> outputsArray;
    private final CraftingInput.Positioned positionedPattern;
    /**
     * We cache results of isValid(...) calls for stacks that don't have NBT.
     */
    @SuppressWarnings("unchecked")
    private final Map<Item, Boolean>[] isValidCache = new Map[9];

    public AECraftingPattern(AEItemKey definition, ServerLevel level) {
        this.definition = definition;
        var encodedPattern = definition.get(AEComponents.ENCODED_CRAFTING_PATTERN);
        if (encodedPattern == null) {
            throw new IllegalArgumentException("Given item does not encode a crafting pattern: " + definition);
        } else if (encodedPattern.containsMissingContent()) {
            throw new IllegalArgumentException("Pattern references missing content");
        }

        this.canSubstitute = encodedPattern.canSubstitute();
        this.canSubstituteFluids = encodedPattern.canSubstituteFluids();
        this.sparseInputs = getCraftingInputs(encodedPattern.inputs());

        // Find recipe
        this.recipeHolder = level.recipeAccess().byKey(encodedPattern.recipeId()).orElse(null);
        if (recipeHolder == null || !(recipeHolder.value() instanceof CraftingRecipe)) {
            throw new IllegalArgumentException("Pattern references unknown recipe " + encodedPattern.recipeId());
        }
        this.recipe = (CraftingRecipe) recipeHolder.value();

        // Build frame and find output
        this.positionedPattern = makeCraftingInput();
        if (!this.recipe.matches(positionedPattern.input(), level)) {
            throw new IllegalStateException("The recipe " + recipe + " no longer matches the encoded input.");
        }

        this.output = this.recipe.assemble(positionedPattern.input(), level.registryAccess());
        if (this.output.isEmpty()) {
            throw new IllegalStateException(
                    "The recipe " + encodedPattern.recipeId() + " produced an empty item stack result.");
        }
        this.outputsArray = Collections.singletonList(Objects.requireNonNull(GenericStack.fromItemStack(this.output)));

        // Compress inputs
        var condensedInputs = AEPatternHelper.condenseStacks(sparseInputs);
        this.inputs = new Input[condensedInputs.size()];
        for (int i = 0; i < 9; ++i) {
            sparseToCompressed[i] = -1;
        }
        for (int j = 0; j < condensedInputs.size(); ++j) {
            var condensedInput = condensedInputs.get(j);

            for (int i = 0; i < 9; ++i) {
                if (sparseInputs.get(i) != null && sparseInputs.get(i).what().equals(condensedInput.what())) {
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
    public List<GenericStack> getOutputs() {
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
        if (sparseInputs.get(0) == null && sparseInputs.get(1) == null && sparseInputs.get(2) == null) {
            topOffset++; // First row is fully empty
            if (sparseInputs.get(3) == null && sparseInputs.get(4) == null && sparseInputs.get(5) == null) {
                topOffset++; // Second row is fully empty
            }
        }
        int leftOffset = 0;
        if (sparseInputs.get(0) == null && sparseInputs.get(3) == null && sparseInputs.get(6) == null) {
            leftOffset++; // First column is fully empty
            if (sparseInputs.get(1) == null && sparseInputs.get(4) == null && sparseInputs.get(7) == null) {
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
            return Ingredient.of();
        }

        return ingredients.get(ingredientIndex);
    }

    private Ingredient getShapelessRecipeIngredient(int slot) {
        // We map the list of *filled* sparse inputs to the shapeless (ergo unordered)
        // ingredients. While these do not actually correspond to each other,
        // since both lists have the same length, the mapping is at least stable.
        int ingredientIndex = 0;
        for (int i = 0; i < slot; i++) {
            if (sparseInputs.get(i) != null) {
                ingredientIndex++;
            }
        }

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (ingredientIndex < ingredients.size()) {
            return ingredients.get(ingredientIndex);
        }

        return Ingredient.of();
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

    @Override
    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        if (!canSubstitute) {
            return sparseInputs.get(slot) == null && key == null
                    || sparseInputs.get(slot) != null && sparseInputs.get(slot).what().equals(key);
        }

        if (key == null) {
            return sparseInputs.get(slot) == null;
        }

        var result = getTestResult(slot, key);
        if (result != null) {
            return result;
        }

        // Fill frame and check result
        var testCraftingInput = makeCraftingInputWithReplacedSlot(slot, key);

        var newResult = recipe.matches(testCraftingInput.input(), level)
                && ItemStack.matches(output, recipe.assemble(testCraftingInput.input(), level.registryAccess()));

        setTestResult(slot, key, newResult);

        return newResult;
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return sparseInputs.get(slot) != null;
    }

    private ItemStack getRecipeRemainder(int slot, AEItemKey key) {
        // Note: no need to call assemble again since we can assume that the item is valid!
        // Consider making this more efficient in the future? (e.g. cache the produced remainders)

        // Fill frame
        var positioned = makeCraftingInputWithReplacedSlot(slot, key);
        // Get remainder
        var remainingItems = recipe.getRemainingItems(positioned.input());

        var x = (slot % CRAFTING_GRID_DIMENSION - positioned.left());
        var y = slot / CRAFTING_GRID_DIMENSION - positioned.top();
        var remainderIdx = y * positioned.input().width() + x;
        if (remainderIdx >= 0 && remainderIdx < remainingItems.size()) {
            return remainingItems.get(remainderIdx);
        }

        return ItemStack.EMPTY;
    }

    /**
     * Retrieve a previous result of testing whether <code>what</code> is a valid ingredient for <code>slot</code>.
     *
     * @return null if the result is unknown, otherwise indicates whether the key is valid or not.
     */
    @Nullable
    private Boolean getTestResult(int slot, AEItemKey what) {
        if (what == null || what.hasComponents()) {
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
        if (what != null && !what.hasComponents()) {
            var cache = isValidCache[slot];
            if (cache == null) {
                cache = isValidCache[slot] = new IdentityHashMap<>();
            }
            cache.put(what.getItem(), result);
        }
    }

    public List<GenericStack> getSparseInputs() {
        return sparseInputs;
    }

    public List<GenericStack> getSparseOutputs() {
        return outputsArray;
    }

    public boolean canSubstitute() {
        return canSubstitute;
    }

    public boolean canSubstituteFluids() {
        return canSubstituteFluids;
    }

    private int getCompressedIndexFromSparse(int sparse) {
        return sparseToCompressed[sparse];
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] table, CraftingGridAccessor gridAccessor) {
        for (int sparseIndex = 0; sparseIndex < 9; ++sparseIndex) {
            int inputId = getCompressedIndexFromSparse(sparseIndex);
            if (inputId != -1) {
                var list = table[inputId];

                // Try substituting with a fluid, if allowed and available
                var validFluid = getValidFluid(sparseIndex);
                if (validFluid != null) {
                    var validFluidKey = validFluid.what();
                    var amount = list.get(validFluidKey);
                    int requiredAmount = (int) validFluid.amount();
                    if (amount >= requiredAmount) {
                        gridAccessor.set(sparseIndex,
                                GenericStack.wrapInItemStack(validFluidKey, requiredAmount));
                        list.remove(validFluidKey, requiredAmount);
                        continue;
                    }
                }

                // Try falling back to whatever is available
                for (var entry : list) {
                    if (entry.getLongValue() > 0 && entry.getKey() instanceof AEItemKey itemKey) {
                        gridAccessor.set(sparseIndex, itemKey.toStack());
                        list.remove(itemKey, 1);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public ItemStack assemble(CraftingInput container, Level level) {
        if (positionedPattern.input().width() != container.width()
                || positionedPattern.input().height() != container.height()) {
            return ItemStack.EMPTY;
        }

        if (canSubstitute && recipe.isSpecial()) {
            // For special recipes, we need to test the recipe with assemble, unfortunately, since the output might
            // depend on the inputs in a way that can't be detected by changing one input at the time.
            var items = NonNullList.withSize(CRAFTING_GRID_DIMENSION * CRAFTING_GRID_DIMENSION, ItemStack.EMPTY);

            for (int x = 0; x < container.size(); ++x) {
                ItemStack item = container.getItem(x);
                var stack = GenericStack.unwrapItemStack(item);
                if (stack != null) {
                    // If we receive a pure fluid stack, we convert it to the appropriate container item
                    // If it matches the allowable input
                    var validFluid = getValidFluid(x);
                    if (validFluid != null && validFluid.equals(stack)) {
                        items.set(x, ((AEItemKey) sparseInputs.get(x).what()).toStack());
                        continue;
                    }
                }
                items.set(x, item.copy());
            }

            var testInput = CraftingInput.of(CRAFTING_GRID_DIMENSION, CRAFTING_GRID_DIMENSION, items);
            return recipe.assemble(testInput, level.registryAccess());
        }

        for (int i = 0; i < sparseInputs.size(); i++) {
            var x = (i % CRAFTING_GRID_DIMENSION) - positionedPattern.left();
            var y = i / CRAFTING_GRID_DIMENSION - positionedPattern.top();
            if (x >= 0 && x < container.width() && y >= 0 && y < container.height()) {
                ItemStack item = container.getItem(x, y);
                var stack = GenericStack.unwrapItemStack(item);
                if (stack != null) {
                    // If we receive a pure fluid stack, we'll convert it to the appropriate container item
                    // If it matches the allowable input
                    var validFluid = getValidFluid(i);
                    if (validFluid != null && validFluid.equals(stack)) {
                        continue;
                    }
                }

                if (!isItemValid(i, AEItemKey.of(item), level)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        // Replace substituted fluids with the original item and ensure the slot is deleted
        // after calling getRemainingItems. This is to fix compatibility with mods that *actually*
        // search for the fluid containers in the container and warn/error if they're not found.
        // See AE2 bug 6804 for details.

        if (canSubstituteFluids) {
            var adjustedItems = new ArrayList<ItemStack>(container.size());
            for (int i = 0; i < container.size(); i++) {
                adjustedItems.add(container.getItem(i));
            }

            var slotsToClear = new boolean[container.size()];
            for (int x = 0; x < container.size(); ++x) {
                var validFluid = getValidFluid(x);
                if (validFluid != null) {
                    var item = container.getItem(x);
                    var stack = GenericStack.unwrapItemStack(item);
                    if (validFluid.equals(stack)) {
                        adjustedItems.set(x, ((AEItemKey) sparseInputs.get(x).what()).toStack());
                        slotsToClear[x] = true;
                    }
                }
            }

            // Since we did not remove items, the positioning itself should not change.
            var adjustedInput = CraftingInput.of(container.width(), container.height(), adjustedItems);
            if (adjustedInput.size() != container.size()) {
                throw new IllegalStateException("After fluid substitution, the container size changed: "
                        + adjustedInput.size() + " != " + container.size());
            }
            var result = this.recipe.getRemainingItems(adjustedInput);

            // Now ensure the empty buckets are cleared since we didn't really use any buckets to begin with
            for (int i = 0; i < slotsToClear.length; i++) {
                if (slotsToClear[i]) {
                    result.set(i, ItemStack.EMPTY);
                }
            }

            return result;
        } else {
            // If no fluid substitution occurred, just call it as-is
            return this.recipe.getRemainingItems(container);
        }
    }

    private GenericStack getItemOrFluidInput(int slot, GenericStack item) {
        if (!(item.what() instanceof AEItemKey itemKey)) {
            return item;
        }

        var containedFluid = ContainerItemStrategies.getContainedStack(itemKey.toStack(), AEKeyType.fluids());
        // Milk is not natively a fluid container, but it might be made one by other mods
        var isBucket = itemKey.getItem() instanceof BucketItem || itemKey.is(Items.MILK_BUCKET);

        if (canSubstituteFluids && containedFluid != null && isBucket) {
            // We only support buckets since we can't predict the behavior of other kinds of containers (ender tanks...)

            // Check that the remaining item is indeed the emptied container.
            // Note: the following call might do a performed extraction with mods that have native fluid container
            // support (such as immersive engineering "fluid aware" recipes). This is only safe because we restrict this
            // code path to buckets.
            var positioned = makeCraftingInput();

            var remainingItems = recipe.getRemainingItems(positioned.input());
            var x = (slot % 3 - positioned.left());
            var y = slot / 3 - positioned.top();
            var remainderIdx = y * positioned.input().width() + x;
            if (remainderIdx >= 0 && remainderIdx < remainingItems.size()) {
                var slotRemainder = remainingItems.get(remainderIdx);
                if (slotRemainder.getCount() == 1 && slotRemainder.is(Items.BUCKET)) {
                    return new GenericStack(containedFluid.what(), containedFluid.amount());
                }
            }
        }

        return item;
    }

    public static void encode(ItemStack result, RecipeHolder<CraftingRecipe> recipe, ItemStack[] sparseInputs,
            ItemStack output, boolean allowSubstitutes, boolean allowFluidSubstitutes) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(sparseInputs, "sparseInputs");
        Objects.requireNonNull(output, "output");

        result.set(
                AEComponents.ENCODED_CRAFTING_PATTERN,
                new EncodedCraftingPattern(
                        Stream.of(sparseInputs).map(ItemStack::copy).toList(),
                        output.copy(),
                        recipe.id(),
                        allowSubstitutes,
                        allowFluidSubstitutes));
    }

    @Override
    public PatternDetailsTooltip getTooltip(Level level, TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_CRAFTS);
        tooltip.addInputsAndOutputs(this);

        if (canSubstitute) {
            tooltip.addProperty(GuiText.PatternTooltipSubstitutions.text());
        }

        if (canSubstituteFluids) {
            tooltip.addProperty(GuiText.PatternTooltipFluidSubstitutions.text());
        }

        if (flags.isAdvanced()) {
            tooltip.addProperty(Component.literal("Recipe"), Component.literal(recipeHolder.id().toString()));
        }

        return tooltip;
    }

    public static PatternDetailsTooltip getInvalidPatternTooltip(ItemStack stack, Level level,
            @Nullable Exception cause, TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_CRAFTS);

        var encodedPattern = stack.get(AEComponents.ENCODED_CRAFTING_PATTERN);
        if (encodedPattern != null) {
            for (var input : encodedPattern.inputs()) {
                if (!input.isEmpty()) {
                    tooltip.addInput(AEItemKey.of(input), input.getCount());
                }
            }
            tooltip.addOutput(AEItemKey.of(encodedPattern.result()), encodedPattern.result().getCount());

            if (encodedPattern.canSubstitute()) {
                tooltip.addProperty(GuiText.PatternTooltipSubstitutions.text());
            }

            if (encodedPattern.canSubstituteFluids()) {
                tooltip.addProperty(GuiText.PatternTooltipFluidSubstitutions.text());
            }

            if (flags.isAdvanced()) {
                tooltip.addProperty(Component.literal("Recipe"),
                        Component.literal(encodedPattern.recipeId().toString()));
            }
        }

        return tooltip;
    }

    private class Input implements IInput {
        private final int slot;
        private final GenericStack[] possibleInputs;
        private final long multiplier;

        private Input(int slot, GenericStack condensedInput) {
            this.slot = slot;
            this.multiplier = condensedInput.amount();

            var itemOrFluidInput = getItemOrFluidInput(slot, sparseInputs.get(slot));

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
        public AEKey getRemainingKey(AEKey template) {
            if (template instanceof AEItemKey itemKey) {
                return AEItemKey.of(getRecipeRemainder(slot, itemKey));
            }
            return null;
        }
    }

    public static List<GenericStack> getCraftingInputs(List<ItemStack> stacks) {
        Preconditions.checkArgument(stacks.size() <= 9, "Cannot use more than 9 ingredients");

        var result = new GenericStack[stacks.size()];
        for (int x = 0; x < stacks.size(); ++x) {
            if (!stacks.get(x).isEmpty()) {
                result[x] = GenericStack.fromItemStack(stacks.get(x));
            }
        }
        return Arrays.asList(result);
    }

    private CraftingInput.Positioned makeCraftingInput() {
        return CraftingInput.ofPositioned(CRAFTING_GRID_DIMENSION, CRAFTING_GRID_DIMENSION, makeCraftingInputItems());
    }

    private CraftingInput.Positioned makeCraftingInputWithReplacedSlot(int slot, AEItemKey replacement) {
        var items = makeCraftingInputItems();
        items.set(slot, replacement.toStack());
        return CraftingInput.ofPositioned(CRAFTING_GRID_DIMENSION, CRAFTING_GRID_DIMENSION, items);
    }

    private List<ItemStack> makeCraftingInputItems() {
        var testFrameItems = new ArrayList<ItemStack>(sparseInputs.size());
        for (int i = 0; i < sparseInputs.size(); ++i) {
            if (sparseInputs.get(i) != null) {
                var itemKey = (AEItemKey) sparseInputs.get(i).what();
                testFrameItems.add(i, itemKey.toStack());
            } else {
                testFrameItems.add(ItemStack.EMPTY);
            }
        }
        return testFrameItems;
    }
}
