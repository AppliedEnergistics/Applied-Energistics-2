package appeng.api.networking.crafting;

import appeng.api.storage.data.IAEItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

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

		// TODO: how is parameter count handled?
		@Nullable IAEItemStack getContainerItem(IAEItemStack input);
	}
}
