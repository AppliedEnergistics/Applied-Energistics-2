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

import com.google.common.base.Preconditions;

import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

public class AEProcessingPattern implements IAEPatternDetails {
    private final AEItemKey definition;
    private final GenericStack[] sparseInputs, sparseOutputs;
    private final Input[] inputs;
    private final GenericStack[] condensedOutputs;

    public AEProcessingPattern(AEItemKey definition) {
        this.definition = definition;
        var tag = Objects.requireNonNull(definition.getTag());

        this.sparseInputs = ProcessingPatternEncoding.getProcessingInputs(tag);
        this.sparseOutputs = ProcessingPatternEncoding.getProcessingOutputs(tag);
        var condensedInputs = AEPatternHelper.condenseStacks(sparseInputs);
        this.inputs = new Input[condensedInputs.length];
        for (int i = 0; i < inputs.length; ++i) {
            inputs[i] = new Input(condensedInputs[i]);
        }

        var primaryOutput = this.sparseOutputs[0];
        this.condensedOutputs = AEPatternHelper.condenseStacks(sparseOutputs);
        // Ensure the primary output is the first in the list, even if it has a smaller stack size.
        int primaryOutputIndex = -1;
        for (int i = 0; i < condensedOutputs.length; ++i) {
            if (primaryOutput.equals(condensedOutputs[i])) {
                primaryOutputIndex = i;
            }
        }
        Preconditions.checkState(primaryOutputIndex >= 0, "Could not find primary output after condensing stacks.");
        if (primaryOutputIndex > 0) {
            var condensedPrimaryOutput = condensedOutputs[primaryOutputIndex];
            // Place the primary output at the beginning of the array.
            System.arraycopy(condensedOutputs, 0, condensedOutputs, 1, primaryOutputIndex);
            condensedOutputs[0] = condensedPrimaryOutput;
        }
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

    @Override
    public GenericStack[] getSparseInputs() {
        return sparseInputs;
    }

    @Override
    public GenericStack[] getSparseOutputs() {
        return sparseOutputs;
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
        public AEKey getContainerItem(AEKey template) {
            return null;
        }
    }
}
