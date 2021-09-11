package appeng.crafting.simulation.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IAEStack;

public class ProcessingPatternBuilder {
    private final IAEStack[] outputs;
    private final List<IPatternDetails.IInput> inputs = new ArrayList<>();

    public ProcessingPatternBuilder(IAEStack... outputs) {
        this.outputs = outputs;
    }

    public ProcessingPatternBuilder addPreciseInput(long multiplier, IAEStack... possibleInputs) {
        inputs.add(new IPatternDetails.IInput() {
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
                for (var possibleInput : possibleInputs) {
                    if (possibleInput.equals(input)) {
                        return true;
                    }
                }
                return false;
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
        });
        return this;
    }

    public IPatternDetails build() {
        return new IPatternDetails() {
            @Override
            public ItemStack getDefinition() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isCrafting() {
                return false;
            }

            @Override
            public IInput[] getInputs() {
                return inputs.toArray(IInput[]::new);
            }

            @Override
            public IAEStack[] getOutputs() {
                return outputs;
            }
        };
    }
}
