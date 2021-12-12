package appeng.me;

import javax.annotation.Nonnull;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;

/**
 * Utility class to provide infinite power storage to the grid.
 */
public class InfinitePowerStorage implements IAEPowerStorage {
    @Override
    public double injectAEPower(double amt, @Nonnull Actionable mode) {
        return 0;
    }

    @Override
    public double getAEMaxPower() {
        return 0;
    }

    @Override
    public double getAECurrentPower() {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Nonnull
    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ;
    }

    @Override
    public double extractAEPower(double amt, @Nonnull Actionable mode, @Nonnull PowerMultiplier usePowerMultiplier) {
        return amt;
    }
}
