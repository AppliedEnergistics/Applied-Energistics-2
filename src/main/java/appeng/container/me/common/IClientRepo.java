package appeng.container.me.common;

import java.util.List;
import java.util.Set;

import appeng.api.storage.data.IAEStack;

/**
 * Represents a client-side only repository of {@link GridInventoryEntry} entries that represent the network content
 * currently known to the client. This is actively synchronized by the server via {@link IncrementalUpdateHelper}.
 */
public interface IClientRepo<T extends IAEStack<T>> {

    /**
     * Handle incoming updates from the server.
     *
     * @param fullUpdate Completely replace the repo contents.
     * @param entries    The updated entries.
     */
    void handleUpdate(boolean fullUpdate, List<GridInventoryEntry<T>> entries);

    /**
     * @return All entries in this repository, regardless of any filter.
     */
    Set<GridInventoryEntry<T>> getAllEntries();

}
