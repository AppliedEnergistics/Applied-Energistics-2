package appeng.api.networking.crafting;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.storage.data.IAEItemStack;

public interface IPatternDetails {
    ItemStack getDefinition();

    boolean isCrafting();

    IInput[] getInputs();

    default IAEItemStack getPrimaryOutput() {
        return getOutputs()[0];
    }

    IAEItemStack[] getOutputs();

    interface IInput {
        IAEItemStack[] getPossibleInputs();

        long getMultiplier();

        boolean isValid(IAEItemStack input, Level level);

        boolean allowFuzzyMatch();

        @Nullable
        IAEItemStack getContainerItem(IAEItemStack template);
    }
}
