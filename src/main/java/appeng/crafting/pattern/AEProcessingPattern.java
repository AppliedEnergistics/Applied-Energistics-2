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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.storage.data.IAEStack;
import appeng.core.definitions.AEItems;

public class AEProcessingPattern implements IAEPatternDetails {
    private final CompoundTag definition;
    private final IAEStack[] sparseInputs, sparseOutputs;
    private final Input[] inputs;
    private final IAEStack[] condensedOutputs;

    public AEProcessingPattern(CompoundTag definition) {
        this.definition = definition;
        this.sparseInputs = AEPatternHelper.getProcessingInputs(definition);
        this.sparseOutputs = AEPatternHelper.getProcessingOutputs(definition);
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
    public ItemStack copyDefinition() {
        var result = new ItemStack(AEItems.PROCESSING_PATTERN);
        result.setTag(definition.copy());
        return result;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public IAEStack[] getOutputs() {
        return condensedOutputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AEProcessingPattern that = (AEProcessingPattern) o;
        return Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definition);
    }

    @Override
    public IAEStack[] getSparseInputs() {
        return sparseInputs;
    }

    @Override
    public IAEStack[] getSparseOutputs() {
        return sparseOutputs;
    }

    private static class Input implements IInput {
        private final IAEStack[] template;
        private final long multiplier;

        private Input(IAEStack stack) {
            this.template = new IAEStack[] { IAEStack.copy(stack, 1) };
            this.multiplier = stack.getStackSize();
        }

        @Override
        public IAEStack[] getPossibleInputs() {
            return template;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(IAEStack input, Level level) {
            return input.equals(template[0]);
        }

        @Nullable
        @Override
        public IAEStack getContainerItem(IAEStack template) {
            return null;
        }
    }
}
