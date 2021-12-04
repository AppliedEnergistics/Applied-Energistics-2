package appeng.crafting.simulation.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.util.CraftingRemainders;

public class ProcessingPatternBuilder {
    private final GenericStack[] outputs;
    private final List<IPatternDetails.IInput> inputs = new ArrayList<>();

    public ProcessingPatternBuilder(GenericStack... outputs) {
        this.outputs = outputs;
    }

    public ProcessingPatternBuilder addPreciseInput(long multiplier, GenericStack... possibleInputs) {
        return addPreciseInput(multiplier, false, possibleInputs);
    }

    public ProcessingPatternBuilder addPreciseInput(long multiplier, boolean containerItems,
            GenericStack... possibleInputs) {
        inputs.add(new IPatternDetails.IInput() {
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
                for (var possibleInput : possibleInputs) {
                    if (possibleInput.what().equals(input)) {
                        return true;
                    }
                }
                return false;
            }

            @Nullable
            @Override
            public AEKey getContainerItem(AEKey template) {
                if (containerItems && template instanceof AEItemKey itemKey) {
                    return CraftingRemainders.getRemainder(itemKey);
                }
                return null;
            }
        });
        return this;
    }

    public ProcessingPatternBuilder addDamageableInput(Item item) {
        var possibleInputs = new GenericStack[] { GenericStack.fromItemStack(new ItemStack(item)) };
        inputs.add(new IPatternDetails.IInput() {
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
                if (input instanceof AEItemKey itemKey) {
                    return itemKey.getItem() == item;
                }
                return false;
            }

            @Nullable
            @Override
            public AEKey getContainerItem(AEKey template) {
                if (template instanceof AEItemKey itemKey) {
                    ItemStack stack = itemKey.toStack();
                    stack.setDamageValue(stack.getDamageValue() - 1);
                    if (stack.getDamageValue() >= stack.getMaxDamage()) {
                        return null;
                    }
                    return AEItemKey.of(stack);
                }
                return null;
            }
        });
        return this;
    }

    public IPatternDetails build() {
        return new IPatternDetails() {
            @Override
            public AEItemKey getDefinition() {
                throw new UnsupportedOperationException();
            }

            @Override
            public IInput[] getInputs() {
                return inputs.toArray(IInput[]::new);
            }

            @Override
            public GenericStack[] getOutputs() {
                return outputs;
            }
        };
    }
}
