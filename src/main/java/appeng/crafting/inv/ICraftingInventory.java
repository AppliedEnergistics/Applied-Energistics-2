package appeng.crafting.inv;

import java.util.Collection;

import javax.annotation.Nullable;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStack;

/**
 * Simplified inventory with unbounded capacity. Used for crafting, both for the simulation and for the CPU's own
 * inventory.
 */
public interface ICraftingInventory<T extends IAEStack<T>> {
    /**
     * Inject items. Can never fail.
     */
    void injectItems(T input, Actionable mode);

    /**
     * Extract items.
     */
    @Nullable
    T extractItems(T input, Actionable mode);

    /**
     * Return a list of templates that match the input with {@link FuzzyMode#IGNORE_ALL}. Never edit the return value,
     * and use {@link #extractItems} to query the exact amount that is available.
     */
    Collection<T> findFuzzyTemplates(T input);
}
