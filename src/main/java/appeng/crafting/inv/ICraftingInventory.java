package appeng.crafting.inv;

import java.util.Collection;

import javax.annotation.Nullable;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStack;

/**
 * Simplified inventory with unbounded capacity and support for multiple IAEStacks. Used for crafting, both for the
 * simulation and for the CPU's own inventory.
 */
public interface ICraftingInventory {
    /**
     * Inject items. Can never fail.
     */
    void injectItems(IAEStack<?> input, Actionable mode);

    /**
     * Extract items.
     */
    @Nullable
    IAEStack<?> extractItems(IAEStack<?> input, Actionable mode);

    /**
     * Return a list of templates that match the input with {@link FuzzyMode#IGNORE_ALL}. Never edit the return value,
     * and use {@link #extractItems} to query the exact amount that is available.
     */
    Collection<IAEStack<?>> findFuzzyTemplates(IAEStack<?> input);
}
