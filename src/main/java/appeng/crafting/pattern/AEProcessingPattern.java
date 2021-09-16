package appeng.crafting.pattern;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.storage.data.IAEStack;

public class AEProcessingPattern implements IAEPatternDetails {
    private final ItemStack definition;
    private final IAEStack[] sparseInputs, sparseOutputs;
    private final Input[] inputs;
    private final IAEStack[] condensedOutputs;

    public AEProcessingPattern(ItemStack definition) {
        this.definition = definition;
        this.sparseInputs = AEPatternHelper.getProcessingInputs(definition.getTag());
        this.sparseOutputs = AEPatternHelper.getProcessingOutputs(definition.getTag());
        var condensedInputs = AEPatternHelper.condenseStacks(sparseInputs);
        this.inputs = new Input[condensedInputs.length];
        for (int i = 0; i < inputs.length; ++i) {
            inputs[i] = new Input(condensedInputs[i]);
        }
        this.condensedOutputs = AEPatternHelper.condenseStacks(sparseOutputs);
    }

    @Override
    public ItemStack copyDefinition() {
        return definition.copy();
    }

    @Override
    public boolean isCrafting() {
        return false;
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

        @Override
        public boolean allowFuzzyMatch() {
            return false;
        }

        @Nullable
        @Override
        public IAEStack getContainerItem(IAEStack template) {
            return null;
        }
    }
}
