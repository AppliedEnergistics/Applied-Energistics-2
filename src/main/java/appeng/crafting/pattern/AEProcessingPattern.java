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

import java.util.Arrays;
import java.util.Objects;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsTooltip;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

public class AEProcessingPattern implements IPatternDetails {
    private static final String NBT_INPUTS = "in";
    private static final String NBT_OUTPUTS = "out";

    public static final int MAX_INPUT_SLOTS = 9 * 9;
    public static final int MAX_OUTPUT_SLOTS = 3 * 9;

    private final AEItemKey definition;
    private final GenericStack[] sparseInputs, sparseOutputs;
    private final Input[] inputs;
    private final GenericStack[] condensedOutputs;

    public AEProcessingPattern(AEItemKey definition) {
        this.definition = definition;
        var tag = Objects.requireNonNull(definition.getTag());

        this.sparseInputs = PatternNbtUtils.getRequiredGenericStackList(tag, NBT_INPUTS,
                AEProcessingPattern.MAX_INPUT_SLOTS);
        this.sparseOutputs = PatternNbtUtils.getRequiredGenericStackList(tag, NBT_OUTPUTS,
                AEProcessingPattern.MAX_OUTPUT_SLOTS);
        var condensedInputs = AEPatternHelper.condenseStacks(sparseInputs);
        this.inputs = new Input[condensedInputs.length];
        for (int i = 0; i < inputs.length; ++i) {
            inputs[i] = new Input(condensedInputs[i]);
        }

        // Ordering is preserved by condenseStacks
        this.condensedOutputs = AEPatternHelper.condenseStacks(sparseOutputs);
    }

    public static void encode(CompoundTag tag, GenericStack[] sparseInputs, GenericStack[] sparseOutputs) {
        if (Arrays.stream(sparseInputs).noneMatch(Objects::nonNull)) {
            throw new IllegalArgumentException("At least one input must be non-null.");
        }
        Objects.requireNonNull(sparseOutputs[0],
                "The first (primary) output must be non-null.");

        tag.put(NBT_INPUTS, encodeStackList(sparseInputs));
        tag.put(NBT_OUTPUTS, encodeStackList(sparseOutputs));
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
    public GenericStack[] getOutputs() {
        return condensedOutputs;
    }

    public GenericStack[] getSparseInputs() {
        return sparseInputs;
    }

    public GenericStack[] getSparseOutputs() {
        return sparseOutputs;
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        if (sparseInputs.length == inputs.length) {
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

    public static PatternDetailsTooltip getInvalidPatternTooltip(CompoundTag tag, Level level,
            @Nullable Exception cause, TooltipFlag flags) {
        var tooltip = new PatternDetailsTooltip(PatternDetailsTooltip.OUTPUT_TEXT_PRODUCES);

        PatternNbtUtils.getGenericStackListFaultTolerant(tag, NBT_INPUTS, tooltip::addInput);
        PatternNbtUtils.getGenericStackListFaultTolerant(tag, NBT_OUTPUTS, tooltip::addOutput);

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

    private static ListTag encodeStackList(GenericStack[] stacks) {
        ListTag tag = new ListTag();
        boolean foundStack = false;
        for (var stack : stacks) {
            tag.add(GenericStack.writeTag(stack));
            if (stack != null && stack.amount() > 0) {
                foundStack = true;
            }
        }
        Preconditions.checkArgument(foundStack, "List passed to pattern must contain at least one stack.");
        return tag;
    }
}
