package appeng.api.networking;

import org.jetbrains.annotations.ApiStatus;

import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEKey;

/**
 * DO NOT IMPLEMENT. Will be injected when adding an {@link IStorageWatcherNode} or {@link ICraftingWatcherNode} to a
 * grid.
 */
@ApiStatus.NonExtendable
public interface IStackWatcher {
    /**
     * Request that ALL changes be broadcast to this watcher.
     *
     * @param watchAll true to enable watching all stacks
     */
    void setWatchAll(boolean watchAll);

    /**
     * Add a specific {@link AEKey} to watch.
     *
     * Supports multiple values, duplicate ones will not be added.
     */
    void add(AEKey stack);

    /**
     * Remove a specific {@link AEKey} from the watcher.
     */
    void remove(AEKey stack);

    /**
     * Removes all watched stacks and resets the watcher to a clean state.
     */
    void reset();
}
