package appeng.me.energy;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged;

/**
 * The inherent energy storage provided by a grid.
 */
public class GridEnergyStorage implements IAEPowerStorage {
    private final IGrid grid;
    private final StoredEnergyAmount stored = new StoredEnergyAmount(0, 800, this::emitPowerEvent);

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
}
