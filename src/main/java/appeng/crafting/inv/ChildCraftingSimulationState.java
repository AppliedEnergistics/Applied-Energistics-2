package appeng.crafting.inv;

import java.util.Collection;

import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEStack;

public class ChildCraftingSimulationState extends CraftingSimulationState {
    private final ICraftingInventory parent;

    public ChildCraftingSimulationState(ICraftingInventory parent) {
        this.parent = parent;
    }

    @Override
    protected IAEStack<?> simulateExtractParent(IAEStack<?> input) {
        return parent.extractItems(input, Actionable.SIMULATE);
    }

    @Override
    protected Collection<IAEStack<?>> findFuzzyParent(IAEStack<?> input) {
        return parent.findFuzzyTemplates(input);
    }
}
