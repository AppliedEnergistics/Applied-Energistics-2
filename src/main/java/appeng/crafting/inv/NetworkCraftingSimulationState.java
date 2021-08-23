package appeng.crafting.inv;

import java.util.Collection;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public class NetworkCraftingSimulationState<T extends IAEStack<T>> extends CraftingSimulationState<T> {
    private final IItemList<T> list;

    public NetworkCraftingSimulationState(IMEMonitor<T> monitor, IActionSource src) {
        super(monitor.getChannel());
        this.list = monitor.getChannel().createList();

        // Cache the whole inventory for now :(
        // TODO: We need to find a way to safely refer to the current storage grid of the crafting requester,
        // TODO: at least for machine-started jobs.
        for (T stack : monitor.getStorageList()) {
            this.list.addStorage(monitor.extractItems(stack, Actionable.SIMULATE, src));
        }
    }

    @Override
    protected T simulateExtractParent(T input) {
        T precise = list.findPrecise(input);
        if (precise == null)
            return null;
        else
            return input.copyWithStackSize(Math.min(input.getStackSize(), precise.getStackSize()));
    }

    @Override
    protected Collection<T> findFuzzyParent(T input) {
        return list.findFuzzy(input, FuzzyMode.IGNORE_ALL);
    }
}
