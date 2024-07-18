package appeng.me.energy;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.core.AEConfig;

/**
 * The inherent energy storage provided by a grid.
 */
public class GridEnergyStorage implements IAEPowerStorage {
    private final IGrid grid;
    private final StoredEnergyAmount stored = new StoredEnergyAmount(0, 0, this::emitPowerEvent);
    private int nodeCount;

    public GridEnergyStorage(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public double injectAEPower(double amt, Actionable mode) {
        return amt - this.stored.insert(amt, mode == Actionable.MODULATE);
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        return this.stored.extract(amt, mode == Actionable.MODULATE);
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public double getAEMaxPower() {
        return this.stored.getMaximum();
    }

    @Override
    public double getAECurrentPower() {
        return this.stored.getAmount();
    }

    @Override
    public int getPriority() {
        // MIN_VALUE to push it to the back
        return Integer.MIN_VALUE;
    }

    private void emitPowerEvent(GridPowerStorageStateChanged.PowerEventType type) {
        grid.postEvent(new GridPowerStorageStateChanged(this, type));
    }

    public void addNode() {
        this.nodeCount++;
        updateMaximum();
    }

    public double getNodeEnergyShare() {
        if (this.nodeCount == 0) {
            return 0;
        }
        return this.stored.getAmount() / this.nodeCount;
    }

    public void removeNode() {
        if (this.nodeCount < 1) {
            throw new IllegalStateException("Removing a node from energy storage while it has no nodes");
        }

        // Deduct the storage of the node proportionally
        var deduction = getNodeEnergyShare();
        if (deduction > 0) {
            this.stored.extract(deduction, true);
        }

        this.nodeCount--;
        updateMaximum();
    }

    private void updateMaximum() {
        // Update maximum power
        this.stored.setMaximum(AEConfig.instance().getGridEnergyStoragePerNode() * nodeCount);
    }
}
