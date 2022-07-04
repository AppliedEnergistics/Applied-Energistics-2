/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.crafting.inv;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;

public abstract class CraftingSimulationState implements ICraftingSimulationState {
    /**
     * Partial cache of the parent's items, never modified.
     */
    private final KeyCounter unmodifiedCache;
    /**
     * Partial cache of the parent's items, but modifiable. The different between this cache and the unmodified cache is
     * the items that were injected/extracted.
     */
    private final KeyCounter modifiableCache;
    /**
     * List of items to emit.
     */
    private final KeyCounter emittedItems;
    /**
     * Byte count.
     */
    private double bytes = 0;
    private final Map<IPatternDetails, Long> crafts = new HashMap<>();
    /**
     * Minimum amount of each item that needs to be extracted from the network. This is the maximum of (unmodified -
     * modifiable).
     */
    private final KeyCounter requiredExtract;

    protected CraftingSimulationState() {
        this.unmodifiedCache = new KeyCounter();
        this.modifiableCache = new KeyCounter();
        this.emittedItems = new KeyCounter();
        this.requiredExtract = new KeyCounter();
    }

    protected abstract long simulateExtractParent(AEKey what, long amount);

    protected abstract Iterable<AEKey> findFuzzyParent(AEKey input);

    private void cacheFuzzy(AEKey what) {
        if (unmodifiedCache.findFuzzy(what, FuzzyMode.IGNORE_ALL).isEmpty()) {
            boolean insertedAny = false;

            for (var keyToCache : findFuzzyParent(what)) {
                // not cached yet.
                var extracted = simulateExtractParent(keyToCache, Long.MAX_VALUE);
                if (extracted != 0) {
                    insertedAny = true;
                }
                modifiableCache.add(keyToCache, extracted);
                unmodifiedCache.add(keyToCache, extracted);
            }

            if (!insertedAny) {
                unmodifiedCache.add(what, 0);
            }
        }
    }

    @Override
    public void insert(AEKey what, long amount, Actionable mode) {
        cacheFuzzy(what);

        if (mode == Actionable.MODULATE) {
            modifiableCache.add(what, amount);
        }
    }

    private void updateRequiredExtract(AEKey key, long delta) {
        if (delta > 0) {
            long max = Math.max(delta, this.requiredExtract.get(key));
            this.requiredExtract.set(key, max);
        }
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode) {
        cacheFuzzy(what);

        var cachedAmount = modifiableCache.get(what);
        if (cachedAmount == 0)
            return 0;

        long extracted = Math.min(cachedAmount, amount);
        if (mode == Actionable.MODULATE) {
            modifiableCache.remove(what, extracted);
        }

        updateRequiredExtract(what, unmodifiedCache.get(what) - modifiableCache.get(what));

        return extracted;
    }

    @Nullable
    @Override
    public Iterable<AEKey> findFuzzyTemplates(AEKey input) {
        if (input == null)
            return Collections.emptyList();
        cacheFuzzy(input);

        return Iterables.transform(modifiableCache.findFuzzy(input, FuzzyMode.IGNORE_ALL), Map.Entry::getKey);
    }

    @Override
    public void emitItems(AEKey what, long amount) {
        this.emittedItems.add(what, amount);
    }

    @Override
    public void addBytes(double bytes) {
        this.bytes += bytes;
    }

    @Override
    public void addCrafting(IPatternDetails details, long crafts) {
        this.crafts.merge(details, crafts, Long::sum);
    }

    public void ignore(AEKey stack) {
        cacheFuzzy(stack);
        unmodifiedCache.set(stack, 0);
        modifiableCache.set(stack, 0);
    }

    public void applyDiff(CraftingSimulationState parent) {
        // It's important to apply this here to ensure that the extract below doesn't make us count some stacks twice.
        for (var entry : requiredExtract) {
            var key = entry.getKey();
            // To compute the new parent max difference during the processing of the child's queries:
            // Take current parent difference, and add required extract (= max difference observed in the child).
            long delta = parent.unmodifiedCache.get(key) - parent.modifiableCache.get(key) + entry.getLongValue();
            parent.updateRequiredExtract(key, delta);
        }

        for (var entry : modifiableCache) {
            var unmodified = unmodifiedCache.get(entry.getKey());
            long sizeDelta = entry.getLongValue() - unmodified;

            if (sizeDelta > 0) {
                parent.insert(entry.getKey(), sizeDelta, Actionable.MODULATE);
            } else if (sizeDelta < 0) {
                long newStackSize = -sizeDelta;
                var reallyExtracted = parent.extract(entry.getKey(), newStackSize, Actionable.MODULATE);

                if (reallyExtracted != -sizeDelta) {
                    throw new IllegalStateException("Failed to extract from parent. This is a bug!");
                }
            }
        }

        for (var toEmit : emittedItems) {
            parent.emitItems(toEmit.getKey(), toEmit.getLongValue());
        }

        parent.addBytes(bytes);

        for (var entry : crafts.entrySet()) {
            parent.addCrafting(entry.getKey(), entry.getValue());
        }
    }

    public static CraftingPlan buildCraftingPlan(CraftingSimulationState state,
            CraftingCalculation calculation, long calculatedAmount) {
        return new CraftingPlan(
                new GenericStack(calculation.getOutput(), calculatedAmount),
                (long) Math.ceil(state.bytes),
                calculation.isSimulation(),
                calculation.hasMultiplePaths(),
                state.requiredExtract,
                state.emittedItems,
                calculation.getMissingItems(),
                state.crafts);
    }
}
