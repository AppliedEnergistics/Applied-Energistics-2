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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.core.localization.GuiText;

/**
 * Encodes patterns for the {@link net.minecraft.world.level.block.SmithingTableBlock}.
 */
public class AESmithingTablePattern implements IPatternDetails, IMolecularAssemblerSupportedPattern {
    // The slot indices in the 3x3 crafting grid that we insert our item into (in the MAC)
    private static final int TEMPLATE_CRAFTING_GRID_SLOT = 3;
    private static final int BASE_CRAFTING_GRID_SLOT = 4;
    private static final int ADDITION_CRAFTING_GRID_SLOT = 5;

    private final AEItemKey definition;
    public final boolean canSubstitute;
    private final ResourceLocation recipeId;
    private final SmithingRecipe recipe;
    private final ItemStack output;
    private final AEItemKey template;
    private final AEItemKey base;
    private final AEItemKey addition;
    private final IInput[] inputs;
    private final List<GenericStack> outputs;

    public AESmithingTablePattern(AEItemKey definition, Level level) {
        this.definition = definition;

        var encodedPattern = definition.get(AEComponents.ENCODED_SMITHING_TABLE_PATTERN);
        if (encodedPattern == null) {
            throw new IllegalArgumentException("Given item does not encode a smithing table pattern: " + definition);
        } else if (encodedPattern.containsMissingContent()) {
            throw new IllegalArgumentException("Pattern references missing content");
        }

        this.template = Objects.requireNonNull(AEItemKey.of(encodedPattern.template()), "template");
        this.base = Objects.requireNonNull(AEItemKey.of(encodedPattern.base()), "base");
        this.addition = Objects.requireNonNull(AEItemKey.of(encodedPattern.addition()), "addition");
        this.canSubstitute = encodedPattern.canSubstitute();

        // Find recipe
        this.recipeId = encodedPattern.recipeId();
        this.recipe = level.getRecipeManager().byKey(recipeId).map(holder -> (SmithingRecipe) holder.value())
                .orElse(null);
        if (recipe == null) {
            throw new IllegalStateException("Smithing pattern references unknown recipe " + recipeId);
        }

        // Build frame and find output
        var testFrame = new SmithingRecipeInput(
                template.toStack(),
                base.toStack(),
                addition.toStack());

        if (!this.recipe.matches(testFrame, level)) {
            throw new IllegalStateException("The recipe " + recipeId + " no longer matches the encoded input.");
        }

        this.output = this.recipe.assemble(testFrame, level.registryAccess());
        if (this.output.isEmpty()) {
            throw new IllegalStateException("The recipe " + recipeId + " produced an empty item stack result.");
        }

        // Find ingredients
        Ingredient templateIngredient, baseIngredient, additionIngredient;
        if (this.recipe instanceof SmithingTransformRecipe r) {
            templateIngredient = r.template;
            baseIngredient = r.base;
            additionIngredient = r.addition;
        } else if (this.recipe instanceof SmithingTrimRecipe r) {
            templateIngredient = r.template;
            baseIngredient = r.base;
            additionIngredient = r.addition;
        } else {
            throw new IllegalStateException(
                    "Don't know how to process non-vanilla smithing recipe: " + this.recipe.getClass());
        }

        this.inputs = new IInput[] {
                new Input(template, templateIngredient, TEMPLATE_CRAFTING_GRID_SLOT),
                new Input(base, baseIngredient, BASE_CRAFTING_GRID_SLOT),
                new Input(addition, additionIngredient, ADDITION_CRAFTING_GRID_SLOT)
        };
        this.outputs = Collections.singletonList(GenericStack.fromItemStack(this.output));
    }

    public ResourceLocation getRecipeId() {
        return recipeId;
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

    public AEItemKey getTemplate() {
        return template;
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
    public List<GenericStack> getOutputs() {
        return outputs;
    }

    public boolean canSubstitute() {
        return canSubstitute;
    }

    @Override
    public ItemStack assemble(CraftingInput container, Level level) {
        // Note that the crafting input is always "compressed" and removes any margins,
        // so the slots we used to insert into the Molecular Assembler will change.
        var testFrame = new SmithingRecipeInput(
                container.getItem(0),
                container.getItem(1),
                container.getItem(2));

        if (recipe.matches(testFrame, level)) {
            return recipe.assemble(testFrame, level.registryAccess());
        }
        return ItemStack.EMPTY;
    }

    public boolean isItemValid(int gridSlot, AEItemKey key, Level level) {
        if (key == null) {
            return false;
        }

        if (!canSubstitute) {
            if (gridSlot == TEMPLATE_CRAFTING_GRID_SLOT) {
                return template.equals(key);
            } else if (gridSlot == BASE_CRAFTING_GRID_SLOT) {
                return base.equals(key);
            } else if (gridSlot == ADDITION_CRAFTING_GRID_SLOT) {
                return addition.equals(key);
            } else {
                return false;
            }
        }

        // Fill frame and check result
        var testInput = switch (gridSlot) {
            case TEMPLATE_CRAFTING_GRID_SLOT -> new SmithingRecipeInput(
                    key.toStack(),
                    base.toStack(),
                    addition.toStack());
            case BASE_CRAFTING_GRID_SLOT -> new SmithingRecipeInput(
                    template.toStack(),
                    key.toStack(),
                    addition.toStack());
            case ADDITION_CRAFTING_GRID_SLOT -> new SmithingRecipeInput(
                    template.toStack(),
                    base.toStack(),
                    key.toStack());
            default -> null;
        };
        if (testInput == null) {
            return false;
        }

        return recipe.matches(testInput, level)
                && ItemStack.matches(output, recipe.assemble(testInput, level.registryAccess()));
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return slot == TEMPLATE_CRAFTING_GRID_SLOT || slot == BASE_CRAFTING_GRID_SLOT
                || slot == ADDITION_CRAFTING_GRID_SLOT;
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] table, CraftingGridAccessor gridAccessor) {
        var entry = table[0].getFirstEntry();
        if (entry != null && entry.getKey() instanceof AEItemKey itemKey) {
            gridAccessor.set(TEMPLATE_CRAFTING_GRID_SLOT, itemKey.toStack());
            table[0].remove(entry.getKey(), 1);
        }

        entry = table[1].getFirstEntry();
        if (entry != null && entry.getKey() instanceof AEItemKey itemKey) {
            gridAccessor.set(BASE_CRAFTING_GRID_SLOT, itemKey.toStack());
            table[1].remove(entry.getKey(), 1);
        }

        entry = table[2].getFirstEntry();
        if (entry != null && entry.getKey() instanceof AEItemKey itemKey) {
            gridAccessor.set(ADDITION_CRAFTING_GRID_SLOT, itemKey.toStack());
            table[2].remove(entry.getKey(), 1);
        }
    }

    public static void encode(ItemStack stack, RecipeHolder<SmithingRecipe> recipe, AEItemKey template, AEItemKey base,
            AEItemKey addition,
            AEItemKey output, boolean allowSubstitutes) {
        Preconditions.checkNotNull(recipe, "recipe");
        Preconditions.checkNotNull(recipe, "template");
        Preconditions.checkNotNull(base, "base");
        Preconditions.checkNotNull(addition, "addition");
        Preconditions.checkNotNull(output, "output");

        stack.set(AEComponents.ENCODED_SMITHING_TABLE_PATTERN, new EncodedSmithingTablePattern(
                template.toStack(),
                base.toStack(),
                addition.toStack(),
                output.toStack(),
                allowSubstitutes,
                recipe.id()));
    }

    @Override
    public PatternDetailsTooltip getTooltip(Level level, TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_CRAFTS);
        tooltip.addInputsAndOutputs(this);
        if (flags.isAdvanced()) {
            tooltip.addProperty(Component.literal("Recipe"), Component.literal(recipeId.toString()));
        }
        return tooltip;
    }

    public static PatternDetailsTooltip getInvalidTooltip(ItemStack stack, Level level, @Nullable Exception cause,
            TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_CRAFTS);

        var encodedPattern = stack.get(AEComponents.ENCODED_SMITHING_TABLE_PATTERN);
        if (encodedPattern != null) {
            tooltip.addInput(GenericStack.fromItemStack(encodedPattern.template()));
            tooltip.addInput(GenericStack.fromItemStack(encodedPattern.base()));
            tooltip.addInput(GenericStack.fromItemStack(encodedPattern.addition()));
            tooltip.addOutput(GenericStack.fromItemStack(encodedPattern.resultItem()));
            if (encodedPattern.canSubstitute()) {
                tooltip.addProperty(GuiText.PatternTooltipSubstitutions.text());
            }
            if (flags.isAdvanced()) {
                tooltip.addProperty(Component.literal("Recipe"),
                        Component.literal(encodedPattern.recipeId().toString()));
            }
        }
        return tooltip;
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
