package appeng.crafting.inv;

import java.util.Collection;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedItemList;

public class NetworkCraftingSimulationState extends CraftingSimulationState {
    private final MixedItemList list = new MixedItemList();

    public NetworkCraftingSimulationState(IStorageMonitorable monitorable, IActionSource src) {
        for (var channel : StorageChannels.getAll()) {
            collectChannelContents(channel, monitorable, src);
        }
    }

    private <T extends IAEStack> void collectChannelContents(IStorageChannel<T> channel,
            IStorageMonitorable monitorable, IActionSource src) {
        var monitor = monitorable.getInventory(channel);
        for (var stack : monitor.getStorageList()) {
            this.list.addStorage(monitor.extractItems(stack, Actionable.SIMULATE, src));
        }
    }

    @Override
    protected IAEStack simulateExtractParent(IAEStack input) {
        var precise = list.findPrecise(input);
        if (precise == null)
            return null;
        else
            return IAEStack.copy(input, Math.min(input.getStackSize(), precise.getStackSize()));
    }

    @Override
    protected Collection<IAEStack> findFuzzyParent(IAEStack input) {
        return list.findFuzzy(input, FuzzyMode.IGNORE_ALL);
    }
}
