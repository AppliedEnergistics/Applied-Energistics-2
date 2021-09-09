package appeng.crafting.inv;

import java.util.*;

import javax.annotation.Nullable;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.IPatternDetails;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingPlan;

public abstract class CraftingSimulationState<T extends IAEStack<T>> implements ICraftingSimulationState<T> {
    /**
     * Partial cache of the parent's items, never modified.
     */
    private final IItemList<T> unmodifiedCache;
    /**
     * Partial cache of the parent's items, but modifiable. The different between this cache and the unmodified cache is
     * the items that were injected/extracted.
     */
    private final IItemList<T> modifiableCache;
    /**
     * List of items to emit.
     */
    private final IItemList<T> emittedItems;
    /**
     * Byte count.
     */
    private long bytes = 0;
    private final Map<IPatternDetails, Long> crafts = new HashMap<>();
    /**
     * Minimum amount of each item that needs to be extracted from the network. This is the maximum of (unmodified -
     * modifiable).
     */
    private final IItemList<T> requiredExtract;

    protected CraftingSimulationState(IStorageChannel<T> chan) {
        this.unmodifiedCache = chan.createList();
        this.modifiableCache = chan.createList();
        this.emittedItems = chan.createList();
        this.requiredExtract = chan.createList();
    }

    protected abstract T simulateExtractParent(T input);

    protected abstract Collection<T> findFuzzyParent(T input);

    private void cacheFuzzy(T stack) {
        if (unmodifiedCache.findFuzzy(stack, FuzzyMode.IGNORE_ALL).isEmpty()) {
            Collection<T> toCache = findFuzzyParent(stack);
            boolean insertedAny = false;

            for (T stackToCache : toCache) {
                // not cached yet.
                T extracted = simulateExtractParent(stackToCache);
                if (extracted != null) {
                    insertedAny = true;
                    // we set craftable to true to ensure the entries never get removed from the list.
                    extracted.setCountRequestable(0).setCraftable(true);
                }
                modifiableCache.add(extracted);
                unmodifiedCache.add(extracted);
            }

            if (!insertedAny) {
                // make sure we don't requery the parent next time
                unmodifiedCache.addCrafting(stack.copy().setCraftable(true));
            }
        }
    }

    @Override
    public void injectItems(T input, Actionable mode) {
        if (input == null)
            return;
        cacheFuzzy(input);

        if (mode == Actionable.MODULATE) {
            modifiableCache.add(input.copy().setCountRequestable(0).setCraftable(true));
        }
    }

    private long getAmount(IItemList<T> list, T template) {
        T precise = list.findPrecise(template);
        if (precise == null)
            return 0;
        return precise.getStackSize();
    }

    private void updateRequiredExtract(T template) {
        long amountUnmodified = getAmount(unmodifiedCache, template);
        long amountModified = getAmount(modifiableCache, template);
        long amountDifference = amountUnmodified - amountModified;

        if (amountDifference > 0) {
            long alreadyRequired = getAmount(this.requiredExtract, template);

            if (alreadyRequired < amountDifference) {
                this.requiredExtract.addStorage(template.copyWithStackSize(amountDifference - alreadyRequired));
            }
        }
    }

    @Nullable
    @Override
    public T extractItems(T input, Actionable mode) {
        if (input == null)
            return null;
        cacheFuzzy(input);

        T precise = modifiableCache.findPrecise(input);
        if (precise == null)
            return null;

        long extracted = Math.min(precise.getStackSize(), input.getStackSize());
        if (mode == Actionable.MODULATE) {
            precise.decStackSize(extracted);
        }

        updateRequiredExtract(input);

        return input.copyWithStackSize(extracted);
    }

    @Nullable
    @Override
    public Collection<T> findFuzzyTemplates(T input) {
        if (input == null)
            return Collections.emptyList();
        cacheFuzzy(input);

        return modifiableCache.findFuzzy(input, FuzzyMode.IGNORE_ALL);
    }

    @Override
    public void emitItems(T what) {
        this.emittedItems.add(what);
    }

    @Override
    public void addBytes(long bytes) {
        this.bytes += bytes;
    }

    @Override
    public void addCrafting(IPatternDetails details, long crafts) {
        this.crafts.merge(details, crafts, Long::sum);
    }

    public void ignore(T stack) {
        cacheFuzzy(stack);
        unmodifiedCache.findPrecise(stack).setStackSize(0);

        T modifiablePrecise = modifiableCache.findPrecise(stack);
        if (modifiablePrecise != null)
            modifiablePrecise.setStackSize(0);
    }

    public void applyDiff(CraftingSimulationState<T> parent) {
        for (T stack : modifiableCache) {
            T unmodified = unmodifiedCache.findPrecise(stack);
            long sizeDelta = (unmodified == null ? 0 : unmodified.getStackSize()) - stack.getStackSize();

            if (sizeDelta > 0) {
                parent.injectItems(stack.copyWithStackSize(sizeDelta), Actionable.MODULATE);
            } else if (sizeDelta < 0) {
                T reallyExtracted = parent.extractItems(stack.copyWithStackSize(-sizeDelta), Actionable.MODULATE);

                if (reallyExtracted == null || reallyExtracted.getStackSize() != -sizeDelta) {
                    throw new IllegalStateException("Failed to extract from parent. This is a bug!");
                }
            }
        }

        for (T toEmit : emittedItems) {
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

    public static CraftingPlan buildCraftingPlan(CraftingSimulationState<IAEItemStack> state,
            CraftingCalculation calculation) {
        return new CraftingPlan(
                calculation.getOutput(),
                state.bytes,
                calculation.isSimulation(),
                state.requiredExtract,
                state.emittedItems,
                calculation.getMissingItems(),
                state.crafts);
    }
}
