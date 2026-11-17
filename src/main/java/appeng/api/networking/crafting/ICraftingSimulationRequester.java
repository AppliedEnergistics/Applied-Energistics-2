package appeng.api.networking.crafting;

import org.jetbrains.annotations.Nullable;

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
     * {@code true} when the server tick should wait for this calculation to spend its time slice.
     * <p>
     * Player calculations keep the default. Automation that stores the future from
     * {@link ICraftingService#beginCraftingCalculation} and polls it on a later tick can return {@code false}. The
     * server tick still grants the slice, and the calculation still pauses itself when that slice is spent.
     */
    default boolean blocksServerTick() {
        return true;
    }

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
