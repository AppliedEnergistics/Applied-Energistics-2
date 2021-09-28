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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedItemList;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;

public abstract class CraftingSimulationState implements ICraftingSimulationState {
    /**
     * Partial cache of the parent's items, never modified.
     */
    private final MixedItemList unmodifiedCache;
    /**
     * Partial cache of the parent's items, but modifiable. The different between this cache and the unmodified cache is
     * the items that were injected/extracted.
     */
    private final MixedItemList modifiableCache;
    /**
     * List of items to emit.
     */
    private final MixedItemList emittedItems;
    /**
     * Byte count.
     */
    private double bytes = 0;
    private final Map<IPatternDetails, Long> crafts = new HashMap<>();
    /**
     * Minimum amount of each item that needs to be extracted from the network. This is the maximum of (unmodified -
     * modifiable).
     */
    private final MixedItemList requiredExtract;

    protected CraftingSimulationState() {
        this.unmodifiedCache = new MixedItemList();
        this.modifiableCache = new MixedItemList();
        this.emittedItems = new MixedItemList();
        this.requiredExtract = new MixedItemList();
    }

    protected abstract IAEStack simulateExtractParent(IAEStack input);

    protected abstract Collection<IAEStack> findFuzzyParent(IAEStack input);

    private void cacheFuzzy(IAEStack stack) {
        if (unmodifiedCache.findFuzzy(stack, FuzzyMode.IGNORE_ALL).isEmpty()) {
            Collection<IAEStack> toCache = findFuzzyParent(stack);
            boolean insertedAny = false;

            for (IAEStack stackToCache : toCache) {
                // not cached yet.
                IAEStack extracted = simulateExtractParent(stackToCache);
                if (extracted != null) {
                    insertedAny = true;
                    // we set craftable to true to ensure the entries never get removed from the list.
                    extracted.setCountRequestable(0);
                    extracted.setCraftable(true);
                }
                modifiableCache.add(extracted);
                unmodifiedCache.add(extracted);
            }

            if (!insertedAny) {
                // make sure we don't requery the parent next time
                var craftable = (IAEStack) IAEStack.copy(stack);
                craftable.setCraftable(true);
                unmodifiedCache.addCrafting(craftable);
            }
        }
    }

    @Override
    public void injectItems(IAEStack input, Actionable mode) {
        if (input == null)
            return;
        cacheFuzzy(input);

        if (mode == Actionable.MODULATE) {
            var craftable = (IAEStack) IAEStack.copy(input);
            craftable.setCountRequestable(0);
            craftable.setCraftable(true);
            modifiableCache.add(craftable);
        }
    }

    private long getAmount(MixedItemList list, IAEStack template) {
        IAEStack precise = list.findPrecise(template);
        if (precise == null)
            return 0;
        return precise.getStackSize();
    }

    private void updateRequiredExtract(IAEStack template) {
        long amountUnmodified = getAmount(unmodifiedCache, template);
        long amountModified = getAmount(modifiableCache, template);
        long amountDifference = amountUnmodified - amountModified;

        if (amountDifference > 0) {
            long alreadyRequired = getAmount(this.requiredExtract, template);

            if (alreadyRequired < amountDifference) {
                this.requiredExtract.addStorage(IAEStack.copy(template, amountDifference - alreadyRequired));
            }
        }
    }

    @Nullable
    @Override
    public IAEStack extractItems(IAEStack input, Actionable mode) {
        if (input == null)
            return null;
        cacheFuzzy(input);

        IAEStack precise = modifiableCache.findPrecise(input);
        if (precise == null)
            return null;

        long extracted = Math.min(precise.getStackSize(), input.getStackSize());
        if (mode == Actionable.MODULATE) {
            precise.decStackSize(extracted);
        }

        updateRequiredExtract(input);

        return IAEStack.copy(input, extracted);
    }

    @Nullable
    @Override
    public Collection<IAEStack> findFuzzyTemplates(IAEStack input) {
        if (input == null)
            return Collections.emptyList();
        cacheFuzzy(input);

        return modifiableCache.findFuzzy(input, FuzzyMode.IGNORE_ALL);
    }

    @Override
    public void emitItems(IAEStack what) {
        this.emittedItems.add(what);
    }

    @Override
    public void addBytes(double bytes) {
        this.bytes += bytes;
    }

    @Override
    public void addCrafting(IPatternDetails details, long crafts) {
        this.crafts.merge(details, crafts, Long::sum);
    }

    public void ignore(IAEStack stack) {
        cacheFuzzy(stack);
        unmodifiedCache.findPrecise(stack).setStackSize(0);

        IAEStack modifiablePrecise = modifiableCache.findPrecise(stack);
        if (modifiablePrecise != null)
            modifiablePrecise.setStackSize(0);
    }

    public void applyDiff(CraftingSimulationState parent) {
        for (IAEStack stack : modifiableCache) {
            IAEStack unmodified = unmodifiedCache.findPrecise(stack);
            long sizeDelta = (unmodified == null ? 0 : unmodified.getStackSize()) - stack.getStackSize();

            if (sizeDelta > 0) {
                parent.injectItems(IAEStack.copy(stack, sizeDelta), Actionable.MODULATE);
            } else if (sizeDelta < 0) {
                long newStackSize = -sizeDelta;
                IAEStack reallyExtracted = parent.extractItems(IAEStack.copy(stack, newStackSize),
                        Actionable.MODULATE);

                if (reallyExtracted == null || reallyExtracted.getStackSize() != -sizeDelta) {
                    throw new IllegalStateException("Failed to extract from parent. This is a bug!");
                }
            }
        }

        for (IAEStack toEmit : emittedItems) {
            parent.emitItems(toEmit);
        }

        parent.addBytes(bytes);

        for (var entry : crafts.entrySet()) {
            parent.addCrafting(entry.getKey(), entry.getValue());
        }

        for (var required : requiredExtract) {
            parent.requiredExtract.addStorage(required);
        }
    }

    public static CraftingPlan buildCraftingPlan(CraftingSimulationState state,
            CraftingCalculation calculation) {
        return new CraftingPlan(
                calculation.getOutput(),
                (long) Math.ceil(state.bytes),
                calculation.isSimulation(),
                calculation.hasMultiplePaths(),
                state.requiredExtract,
                state.emittedItems,
                calculation.getMissingItems(),
                state.crafts);
    }
}
