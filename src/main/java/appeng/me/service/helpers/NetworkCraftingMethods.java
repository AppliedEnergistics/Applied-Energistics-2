package appeng.me.service.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Iterators;

import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

/**
 * Lists the crafting methods in the network, and related information.
 */
public class NetworkCraftingMethods {
    private final Map<IPatternDetails, CraftingMediumList> craftingMethods = new HashMap<>();
    private final Map<AEKey, Set<IPatternDetails>> craftableItems = new HashMap<>();
    /**
     * Used for looking up craftable alternatives using fuzzy search (i.e. ignore NBT).
     */
    private final KeyCounter<AEKey> craftableItemsList = new KeyCounter<>();
    private final Set<AEKey> emitableItems = new HashSet<>();

    public void rebuild(Set<ICraftingProvider> craftingProviders) {
        craftingMethods.clear();
        craftableItems.clear();
        craftableItemsList.clear();
        emitableItems.clear();

        // Gather all options
        var helper = new ProviderHelper();
        for (ICraftingProvider provider : craftingProviders) {
            provider.provideCrafting(helper);
        }
    }

    public <T extends AEKey> Set<T> getCraftables(IStorageChannel<T> channel) {
        var result = new HashSet<T>();

        // add craftable items!
        for (var stack : this.craftableItems.keySet()) {
            if (stack.getChannel() == channel) {
                result.add(stack.cast(channel));
            }
        }

        for (var stack : this.emitableItems) {
            if (stack.getChannel() == channel) {
                result.add(stack.cast(channel));
            }
        }

        return result;
    }

    public Collection<IPatternDetails> getCraftingFor(AEKey whatToCraft) {
        var patterns = this.craftableItems.get(whatToCraft);
        if (patterns != null) {
            return Collections.unmodifiableCollection(patterns);
        }
        return Collections.emptyList();
    }

    @Nullable
    public AEKey getFuzzyCraftable(AEKey whatToCraft, Predicate<AEKey> filter) {
        for (var fuzzy : craftableItemsList.findFuzzy(whatToCraft, FuzzyMode.IGNORE_ALL)) {
            if (filter.test(fuzzy.getKey())) {
                return fuzzy.getKey();
            }
        }
        return null;
    }

    public boolean canEmitFor(AEKey someItem) {
        return this.emitableItems.contains(someItem);
    }

    public Iterable<ICraftingMedium> getMediums(IPatternDetails key) {
        var mediumList = this.craftingMethods.get(key);
        if (mediumList != null) {
            return mediumList.cycleIterable;
        } else {
            return Collections.emptyList();
        }
    }

    private class ProviderHelper implements ICraftingProviderHelper {
        @Override
        public void addCraftingOption(ICraftingMedium medium, IPatternDetails pattern) {
            // output -> pattern (for simulation)
            var primaryOutput = pattern.getPrimaryOutput();
            craftableItemsList.add(primaryOutput.what(), 1);
            craftableItems.computeIfAbsent(primaryOutput.what(), k -> new HashSet<>()).add(pattern);

            // pattern -> method (for execution)
            craftingMethods.computeIfAbsent(pattern, d -> new CraftingMediumList()).mediums.add(medium);
        }

        @Override
        public void setEmitable(AEKey what) {
            emitableItems.add(what);
        }
    }

    private static class CraftingMediumList {
        private final List<ICraftingMedium> mediums = new ArrayList<>();
        /**
         * Cycling iterator for round-robin.
         */
        private final Iterator<ICraftingMedium> cycleIterator = Iterators.cycle(mediums);
        private final Iterable<ICraftingMedium> cycleIterable = () -> Iterators.limit(cycleIterator, mediums.size());
    }
}
