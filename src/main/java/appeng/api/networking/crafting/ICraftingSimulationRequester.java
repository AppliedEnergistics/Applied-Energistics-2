package appeng.api.networking.crafting;

import javax.annotation.Nullable;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;

/**
 * The source of a crafting simulation request. This allows the crafting simulation to keep track of the current grid
 * across multiple ticks to simulate item extraction or to explore more patterns.
 *
 * Returning null in one of the functions will just prevent extraction or exploration of patterns, likely leading to an
 * unsuccessful {@link ICraftingPlan}.
 */
public interface ICraftingSimulationRequester {
    /**
     * Return the current action source, used to extract items.
     */
    @Nullable
    IActionSource getActionSource();

    /**
     * Return the current grid node, used to access the current grid state.
     */
    @Nullable
    default IGridNode getGridNode() {
        var actionSource = getActionSource();
        if (actionSource != null) {
            return actionSource.machine().map(IActionHost::getActionableNode).orElse(null);
        }
        return null;
    }
}
