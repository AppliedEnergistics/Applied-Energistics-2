package appeng.api.storage;

import javax.annotation.Nullable;

import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEStack;

/**
 * Allows ME interfaces to inject crafting inputs regardless of the storage channel. Obtained from the storage channel.
 */
public interface IForeignInventory<T extends IAEStack> {
    /**
     * @see IMEInventory#injectItems
     */
    @Nullable
    T injectItems(T input, Actionable type);

    /**
     * Return true if blocking mode should skip the inventory.
     */
    boolean isBusy();
}
