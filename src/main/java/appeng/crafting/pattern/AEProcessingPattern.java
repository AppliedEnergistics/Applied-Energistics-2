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

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

public class AEProcessingPattern implements IPatternDetails {
    public static final int MAX_INPUT_SLOTS = 9 * 9;
    public static final int MAX_OUTPUT_SLOTS = 3 * 9;

    private final AEItemKey definition;
    private final List<GenericStack> sparseInputs, sparseOutputs;
    private final Input[] inputs;
    private final List<GenericStack> condensedOutputs;

    public AEProcessingPattern(AEItemKey definition) {
        this.definition = definition;

        var encodedPattern = definition.get(AEComponents.ENCODED_PROCESSING_PATTERN);
        if (encodedPattern == null) {
            throw new IllegalArgumentException("Given item does not encode a processing pattern: " + definition);
        } else if (encodedPattern.containsMissingContent()) {
            throw new IllegalArgumentException("Pattern references missing content");
        }

        this.sparseInputs = encodedPattern.sparseInputs();
        this.sparseOutputs = encodedPattern.sparseOutputs();
        var condensedInputs = AEPatternHelper.condenseStacks(sparseInputs);
        this.inputs = new Input[condensedInputs.size()];
        for (int i = 0; i < inputs.length; ++i) {
            inputs[i] = new Input(condensedInputs.get(i));
        }

        // Ordering is preserved by condenseStacks
        this.condensedOutputs = AEPatternHelper.condenseStacks(sparseOutputs);
    }

    public static void encode(ItemStack stack, List<GenericStack> sparseInputs, List<GenericStack> sparseOutputs) {
        if (sparseInputs.stream().noneMatch(Objects::nonNull)) {
            throw new IllegalArgumentException("At least one input must be non-null.");
        }
        Objects.requireNonNull(sparseOutputs.get(0),
                "The first (primary) output must be non-null.");

        stack.set(AEComponents.ENCODED_PROCESSING_PATTERN, new EncodedProcessingPattern(
                sparseInputs, sparseOutputs));
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && ((AEProcessingPattern) obj).definition.equals(definition);
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
        return condensedOutputs;
    }

    public List<GenericStack> getSparseInputs() {
        return sparseInputs;
    }

    public List<GenericStack> getSparseOutputs() {
        return sparseOutputs;
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        if (sparseInputs.size() == inputs.length) {
            // No compression -> no need to reorder
            IPatternDetails.super.pushInputsToExternalInventory(inputHolder, inputSink);
            return;
        }

        var allInputs = new KeyCounter();
        for (var counter : inputHolder) {
            allInputs.addAll(counter);
        }

        // Push according to sparse input order
        for (var sparseInput : sparseInputs) {
            if (sparseInput == null) {
                continue;
            }

            var key = sparseInput.what();
            var amount = sparseInput.amount();
            long available = allInputs.get(key);

            if (available < amount) {
                throw new RuntimeException("Expected at least %d of %s when pushing pattern, but only %d available"
                        .formatted(amount, key, available));
            }

            inputSink.pushInput(key, amount);
            allInputs.remove(key, amount);
        }
    }

    public static PatternDetailsTooltip getInvalidPatternTooltip(ItemStack stack, Level level,
            @Nullable Exception cause, TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_PRODUCES);

        var encodedPattern = stack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
        if (encodedPattern != null) {
            encodedPattern.sparseInputs().stream().filter(Objects::nonNull).forEach(tooltip::addInput);
            encodedPattern.sparseOutputs().stream().filter(Objects::nonNull).forEach(tooltip::addOutput);
        }

        return tooltip;
    }

    private static class Input implements IInput {
        private final GenericStack[] template;
        private final long multiplier;

        private Input(GenericStack stack) {
            this.template = new GenericStack[] { new GenericStack(stack.what(), 1) };
            this.multiplier = stack.amount();
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return template;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return input.matches(template[0]);
        }

        @Nullable
        @Override
        public AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
