package appeng.server.testplots;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;

/**
 * Triggered to spawn additional testing tools into a container placed next to a spawned AE2 grid.
 */
@TestPlotClass
public class SpawnExtraGridTestTools extends Event {
    private final Identifier plotId;
    private final InternalInventory inventory;
    private final IGrid grid;

    public SpawnExtraGridTestTools(Identifier plotId, InternalInventory inventory, IGrid grid) {
        this.plotId = plotId;
        this.inventory = inventory;
        this.grid = grid;
    }

    public Identifier getPlotId() {
        return plotId;
    }

    public InternalInventory getInventory() {
        return inventory;
    }

    public IGrid getGrid() {
        return grid;
    }
}
