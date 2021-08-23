package appeng.crafting.inv;

import java.util.Collection;

import appeng.api.config.Actionable;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

public class ChildCraftingSimulationState<T extends IAEStack<T>> extends CraftingSimulationState<T> {
    private final ICraftingInventory<T> parent;

    public ChildCraftingSimulationState(IStorageChannel<T> chan, ICraftingInventory<T> parent) {
        super(chan);
        this.parent = parent;
    }

    @Override
    protected T simulateExtractParent(T input) {
        return parent.extractItems(input, Actionable.SIMULATE);
    }

    @Override
    protected Collection<T> findFuzzyParent(T input) {
        return parent.findFuzzyTemplates(input);
    }
}
