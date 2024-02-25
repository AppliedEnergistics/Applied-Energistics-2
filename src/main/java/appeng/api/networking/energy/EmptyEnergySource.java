package appeng.api.networking.energy;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;

final class EmptyEnergySource implements IEnergySource {
    static final IEnergySource INSTANCE = new EmptyEnergySource();

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        return 0;
    }
}
