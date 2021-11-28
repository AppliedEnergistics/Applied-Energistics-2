package appeng.me.service.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Iterators;

import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

/**
 * Keeps track of the crafting patterns in the network, and related information.
 */
public class NetworkCraftingProviders {
    private final Map<IGridNode, ProviderState> craftingProviders = new HashMap<>();
    private final Map<IPatternDetails, CraftingProviderList> craftingMethods = new HashMap<>();
    private final Map<AEKey, Map<IPatternDetails, Integer>> craftableItems = new HashMap<>();
    /**
     * Used for looking up craftable alternatives using fuzzy search (i.e. ignore NBT).
     */
    private final KeyCounter craftableItemsList = new KeyCounter();
    private final Map<AEKey, Integer> emitableItems = new HashMap<>();

    public void addProvider(IGridNode node) {
        var provider = node.getService(ICraftingProvider.class);
        if (provider != null) {
            if (craftingProviders.containsKey(node)) {
                throw new IllegalArgumentException("Duplicate crafting provider registration for node " + node);
            }
            var state = new ProviderState(provider);
            state.mount(this);
            craftingProviders.put(node, state);
        }
    }

    public void removeProvider(IGridNode node) {
        var provider = node.getService(ICraftingProvider.class);
        if (provider != null) {
            var state = craftingProviders.remove(node);
            if (state != null) {
                state.unmount(this);
            }
        }
    }

    public Set<AEKey> getCraftables(AEKeyFilter filter) {
        var result = new HashSet<AEKey>();

        // add craftable items!
        for (var stack : this.craftableItems.keySet()) {
            if (filter.matches(stack)) {
                result.add(stack);
            }
        }

        for (var stack : this.emitableItems.keySet()) {
            if (filter.matches(stack)) {
                result.add(stack);
            }
        }

        return result;
    }

    public Collection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
        var patterns = this.craftableItems.get(whatToCraft);
        if (patterns != null) {
            return Collections.unmodifiableCollection(patterns.keySet());
        }
        return Collections.emptyList();
    }

    @Nullable
    public AEKey getFuzzyCraftable(AEKey whatToCraft, AEKeyFilter filter) {
        for (var fuzzy : craftableItemsList.findFuzzy(whatToCraft, FuzzyMode.IGNORE_ALL)) {
            if (filter.matches(fuzzy.getKey())) {
                return fuzzy.getKey();
            }
        }
        return null;
    }

    public boolean canEmitFor(AEKey someItem) {
        return this.emitableItems.containsKey(someItem);
    }

    public Iterable<ICraftingProvider> getMediums(IPatternDetails key) {
        var mediumList = this.craftingMethods.get(key);
        return Objects.requireNonNullElse(mediumList, Collections.emptyList());
    }

    private static class CraftingProviderList implements Iterable<ICraftingProvider> {
        private final List<ICraftingProvider> providers = new ArrayList<>();
        /**
         * Cycling iterator for round-robin. Has to be refreshed after every addition or removal to providers to prevent
         * CMEs.
         */
        private Iterator<ICraftingProvider> cycleIterator = Iterators.cycle(providers);

        private void add(ICraftingProvider provider) {
            providers.add(provider);
            cycleIterator = Iterators.cycle(providers);
        }

        private void remove(ICraftingProvider provider) {
            providers.remove(provider);
            cycleIterator = Iterators.cycle(providers);
        }

        @Override
        public Iterator<ICraftingProvider> iterator() {
            return Iterators.limit(cycleIterator, providers.size());
        }
    }

    private static class ProviderState {
        private final ICraftingProvider provider;
        private final Set<AEKey> emitableItems;
        private final List<IPatternDetails> patterns;

        private ProviderState(ICraftingProvider provider) {
            this.provider = provider;
            this.emitableItems = new HashSet<>(provider.getEmitableItems());
            this.patterns = new ArrayList<>(provider.getAvailablePatterns());
        }

        private void mount(NetworkCraftingProviders methods) {
            for (var emitable : emitableItems) {
                methods.emitableItems.merge(emitable, 1, Integer::sum);
            }
            for (var pattern : patterns) {
                // output -> pattern (for simulation)
                var primaryOutput = pattern.getPrimaryOutput();

                methods.craftableItemsList.add(primaryOutput.what(), 1);

                var patternMap = methods.craftableItems.computeIfAbsent(primaryOutput.what(), k -> new HashMap<>());
                patternMap.merge(pattern, 1, Integer::sum);

                // pattern -> method (for execution)
                methods.craftingMethods.computeIfAbsent(pattern, d -> new CraftingProviderList()).add(provider);
            }
        }

        private void unmount(NetworkCraftingProviders methods) {
            for (var emitable : emitableItems) {
                methods.emitableItems.compute(emitable, (key, cnt) -> cnt == 1 ? null : cnt - 1);
            }
            for (var pattern : patterns) {
                // can in theory leak a bit of memory over time if never cleaned up, but it shouldn't be too bad.
                var primaryOutput = pattern.getPrimaryOutput();

                methods.craftableItemsList.remove(primaryOutput.what(), 1);

                var patternMap = methods.craftableItems.get(primaryOutput.what());
                patternMap.merge(pattern, -1, Integer::sum);

                methods.craftingMethods.get(pattern).remove(provider);
            }
        }
    }
}
