package appeng.crafting.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.storage.data.IAEStack;

public interface IAEPatternDetails extends IPatternDetails {
    IAEStack[] getSparseInputs();

    IAEStack[] getSparseOutputs();

    default boolean isCraftable() {
        return this instanceof AECraftingPattern;
    }

    default boolean canSubstitute() {
        return isCraftable() && ((AECraftingPattern) this).canSubstitute;
    }
}
