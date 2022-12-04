package appeng.helpers.iface;

import org.jetbrains.annotations.Nullable;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;

/**
 * Interface implemented by machines connected to the network if they support storing
 * {@link appeng.crafting.pattern.EncodedPatternItem}, such as the pattern provider.
 */
public interface PatternContainer {
    /**
     * Get the grid the container is currently connected to. Used to track if the container disconnects, so it can be
     * removed from the terminal.
     */
    @Nullable
    IGrid getGrid();

    /**
     * @return True if this container should be shown in the pattern access terminal.
     */
    default boolean isVisibleInTerminal() {
        return true;
    }

    /**
     * @return The inventory to store patterns in.
     */
    InternalInventory getTerminalPatternInventory();

    /**
     * Order for sorting providers in the terminal. Providers are sorted in ascending order.
     */
    default long getTerminalSortOrder() {
        return 0;
    }

    /**
     * @return The group in which this pattern container should be grouped visually in the pattern access terminal.
     */
    PatternContainerGroup getTerminalGroup();
}
