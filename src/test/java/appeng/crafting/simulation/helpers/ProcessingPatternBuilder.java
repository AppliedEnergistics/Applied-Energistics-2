package appeng.crafting.simulation.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ProcessingPatternBuilder {
    private final IAEStack[] outputs;
    private final List<IPatternDetails.IInput> inputs = new ArrayList<>();

    public ProcessingPatternBuilder(IAEStack... outputs) {
        this.outputs = outputs;
    }

    public ProcessingPatternBuilder addPreciseInput(long multiplier, IAEStack... possibleInputs) {
        return addPreciseInput(multiplier, false, possibleInputs);
    }

    public ProcessingPatternBuilder addPreciseInput(long multiplier, boolean containerItems,
            IAEStack... possibleInputs) {
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

            @Nullable
            @Override
            public IAEStack getContainerItem(IAEStack template) {
                if (containerItems && template.getChannel() == StorageChannels.items()) {
                    assertThat(template.getStackSize()).isEqualTo(1);
                    return Platform.getContainerItem(template.cast(StorageChannels.items()));
                }
                return null;
            }
        });
        return this;
    }

    public ProcessingPatternBuilder addDamageableInput(Item item) {
        var possibleInputs = new IAEStack[] { AEItemStack.fromItemStack(new ItemStack(item)) };
        inputs.add(new IPatternDetails.IInput() {
            @Override
            public IAEStack[] getPossibleInputs() {
                return possibleInputs;
            }

            @Override
            public long getMultiplier() {
                return 1;
            }

            @Override
            public boolean isValid(IAEStack input, Level level) {
                if (input.getChannel() == StorageChannels.items()) {
                    var itemStack = input.cast(StorageChannels.items());
                    return itemStack.getItem() == item;
                }
                return false;
            }

            @Nullable
            @Override
            public IAEStack getContainerItem(IAEStack template) {
                if (template.getChannel() == StorageChannels.items()) {
                    assertThat(template.getStackSize()).isEqualTo(1);
                    ItemStack stack = template.cast(StorageChannels.items()).createItemStack();
                    stack.setDamageValue(stack.getDamageValue() - 1);
                    if (stack.getDamageValue() >= stack.getMaxDamage()) {
                        return null;
                    }
                    return AEItemStack.fromItemStack(stack);
                }
                return null;
            }
        });
        return this;
    }

    public IPatternDetails build() {
        return new IPatternDetails() {
            @Override
            public ItemStack copyDefinition() {
                throw new UnsupportedOperationException();
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
