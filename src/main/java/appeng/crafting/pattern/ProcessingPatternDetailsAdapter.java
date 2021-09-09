package appeng.crafting.pattern;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import com.google.common.base.Preconditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ProcessingPatternDetailsAdapter implements IPatternDetails {
	private final Input[] inputs;
	private final IAEItemStack[] outputs;
	private final ICraftingPatternDetails legacy;

	ProcessingPatternDetailsAdapter(ICraftingPatternDetails legacy) {
		Preconditions.checkArgument(!legacy.isCraftable());
		this.legacy = legacy;

		inputs = new Input[legacy.getInputs().size()];
		for (int i = 0; i < legacy.getInputs().size(); ++i) {
			inputs[i] = new Input(legacy.getInputs().get(i));
		}

		outputs = legacy.getOutputs().toArray(IAEItemStack[]::new);
	}

	@Override
	public ItemStack getDefinition() {
		// TODO: do we really need to copy?
		return legacy.getPattern().copy();
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
	public IAEItemStack[] getOutputs() {
		return outputs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return ((ProcessingPatternDetailsAdapter) o).legacy.equals(this.legacy);
	}

	private static class Input implements IInput {
		private final IAEItemStack[] input;
		private final long multiplier;

		private Input(IAEItemStack input) {
			this.input = new IAEItemStack[]{ input.copyWithStackSize(1) };
			this.multiplier = input.getStackSize();
		}

		@Override
		public IAEItemStack[] getPossibleInputs() {
			return input;
		}

		@Override
		public long getMultiplier() {
			return multiplier;
		}

		@Override
		public boolean isValid(IAEItemStack input, Level level) {
			return true;
		}

		@Override
		public boolean allowFuzzyMatch() {
			return false;
		}

		@Nullable
		@Override
		public IAEItemStack getContainerItem(IAEItemStack input) {
			return null;
		}
	}
}
