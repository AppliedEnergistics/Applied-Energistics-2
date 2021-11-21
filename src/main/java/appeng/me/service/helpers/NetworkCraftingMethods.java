package appeng.me.service.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

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
    private final Map<IPatternDetails, List<ICraftingMedium>> craftingMethods = new HashMap<>();
    /**
     * The sets here are linked hash sets to preserve highest -> lowest priority ordering.
     */
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
        // Sort options. Highest priority first.
        helper.offeredOptions.sort(Comparator.comparingInt(o -> -o.priority));

        for (var method : helper.offeredOptions) {
            // output -> pattern (for simulation)
            var primaryOutput = method.pattern.getPrimaryOutput();
            craftableItemsList.add(primaryOutput.what(), 1);
            craftableItems.computeIfAbsent(primaryOutput.what(), k -> new LinkedHashSet<>()).add(method.pattern);

            // pattern -> method (for execution)
            craftingMethods.computeIfAbsent(method.pattern, d -> new ArrayList<>()).add(method.medium);
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
        return this.craftingMethods.getOrDefault(key, Collections.emptyList());
    }

    private record OfferedCraftingOption(ICraftingMedium medium, IPatternDetails pattern, int priority) {
    }

    private class ProviderHelper implements ICraftingProviderHelper {
        private final List<OfferedCraftingOption> offeredOptions = new ArrayList<>();

        @Override
        public void addCraftingOption(ICraftingMedium medium, IPatternDetails pattern, int priority) {
            offeredOptions.add(new OfferedCraftingOption(medium, pattern, priority));
        }

        @Override
        public void setEmitable(AEKey what) {
            emitableItems.add(what); // write to network methods directly.
        }
    }
}
