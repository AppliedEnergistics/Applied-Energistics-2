package appeng.crafting.pattern;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;

public class CraftingPatternDetailsAdapter implements IPatternDetails {

    private final ICraftingPatternDetails legacy;
    private final Input[] inputs;
    private final IAEItemStack[] output;
    private final boolean containerItems;

    CraftingPatternDetailsAdapter(ICraftingPatternDetails legacy) {
        this.legacy = legacy;
        this.output = new IAEItemStack[] { legacy.getOutputs().get(0) };

        List<Input> inputs = new ArrayList<>();
        IAEItemStack[] sparseInputs = legacy.getSparseInputs();
        this.containerItems = PatternDetailsAdapter.hasContainerItems(legacy);
        if (containerItems) {
            // One input per non-null slot!
            for (int x = 0; x < sparseInputs.length; ++x) {
                if (sparseInputs[x] != null) {
                    inputs.add(new Input(x, 1));
                }
            }
        } else {
            // Try to group slots together.
            for (IAEItemStack input : legacy.getInputs()) {
                for (int x = 0; x < sparseInputs.length; ++x) {
                    if (sparseInputs[x] != null && sparseInputs[x].equals(input)) {
                        inputs.add(new Input(x, input.getStackSize()));
                        break;
                    }
                }
            }
        }
        this.inputs = inputs.toArray(Input[]::new);
    }

    /**
     * Find IInput index corresponding to the passed sparse input index, or -1 if there is no input.
     */
    public int getInputIndexFromSparseIndex(int slot) {
        if (containerItems) {
            IAEItemStack[] sparseInputs = legacy.getSparseInputs();
            for (int x = 0; x < this.inputs.length; ++x) {
                if (inputs[x].slot == slot) {
                    return x;
                }
            }
            return -1;
        } else {
            IAEItemStack input = legacy.getSparseInputs()[slot];
            for (int x = 0; x < this.inputs.length; ++x) {
                if (inputs[x].possibleInputs[0].equals(input)) {
                    return x;
                }
            }
            return -1;
        }
    }

    @Override
    public ItemStack getDefinition() {
        return legacy.getPattern();
    }

    @Override
    public boolean isCrafting() {
        return true;
    }

    @Override
    public IInput[] getInputs() {
        return inputs;
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return output;
    }

    public ICraftingPatternDetails getLegacy() {
        return legacy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return ((CraftingPatternDetailsAdapter) o).legacy.equals(this.legacy);
    }

    private class Input implements IInput {
        private final IAEItemStack[] possibleInputs;
        private final int slot;
        private final long multiplier;

        private Input(int slot, long multiplier) {
            this.slot = slot;
            this.multiplier = multiplier;

            if (legacy.canSubstitute()) {
                this.possibleInputs = legacy.getSubstituteInputs(slot).toArray(IAEItemStack[]::new);
            } else {
                this.possibleInputs = new IAEItemStack[] { legacy.getSparseInputs()[slot] };
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
        public boolean isValid(IAEStack stack, Level level) {
            return stack instanceof IAEItemStack input && legacy.isValidItemForSlot(slot, input.getDefinition(), level);
        }

        @Override
        public boolean allowFuzzyMatch() {
            return legacy.canSubstitute();
        }

        @Nullable
        @Override
        public IAEStack getContainerItem(IAEStack input) {
            return Platform.getContainerItem((IAEItemStack) input);
        }
    }
}
