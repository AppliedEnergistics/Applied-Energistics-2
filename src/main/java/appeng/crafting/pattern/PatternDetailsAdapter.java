package appeng.crafting.pattern;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.data.IAEItemStack;

public class PatternDetailsAdapter {
	public static IPatternDetails adapt(ICraftingPatternDetails details) {
		if (details.isCraftable()) {
			return new CraftingPatternDetailsAdapter(details);
		} else {
			return new ProcessingPatternDetailsAdapter(details);
		}
	}

	static boolean hasContainerItems(ICraftingPatternDetails legacy) {
		for (IAEItemStack input : legacy.getInputs()) {
			if (input.createItemStack().hasContainerItem()) {
				return true;
			}
		}
		return false;
	}
}
