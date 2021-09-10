package appeng.api.networking.crafting;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.storage.data.IAEStack;

public interface IPatternDetails {
    ItemStack getDefinition();

    boolean isCrafting();

    IInput[] getInputs();

    default IAEStack<?> getPrimaryOutput() {
        return getOutputs()[0];
    }

    IAEStack<?>[] getOutputs();

    interface IInput {
        IAEStack<?>[] getPossibleInputs();

        long getMultiplier();

        boolean isValid(IAEStack<?> input, Level level);

        boolean allowFuzzyMatch();

        @Nullable
        IAEStack<?> getContainerItem(IAEStack<?> template);
    }
}
