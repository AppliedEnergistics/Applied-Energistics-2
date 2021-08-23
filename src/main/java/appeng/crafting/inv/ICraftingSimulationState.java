package appeng.crafting.inv;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEStack;

/**
 * Extended version of {@link ICraftingInventory} to keep track of other simulation state that is not directly related
 * to inventory contents.
 */
public interface ICraftingSimulationState<T extends IAEStack<T>> extends ICraftingInventory<T> {
    void emitItems(T what);

    void addBytes(long bytes);

    void addCrafting(ICraftingPatternDetails details, long crafts);
}
