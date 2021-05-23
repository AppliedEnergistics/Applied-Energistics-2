package appeng.tile.powersink;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;

/**
 * Adapts an {@link IExternalPowerSink} to FTL's {@link EnergyIo}.
 */
public class FtlEnergyAdapter implements EnergyIo {

    private final IExternalPowerSink sink;

    FtlEnergyAdapter(IExternalPowerSink sink) {
        this.sink = sink;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public double insert(double maxAmount, Simulation simulation) {
        return this.sink.injectExternalPower(PowerUnits.TR, maxAmount,
                simulation.isActing() ? Actionable.MODULATE : Actionable.SIMULATE);
    }

    @Override
    public double getEnergy() {
        return this.sink.getAECurrentPower();
    }

    @Override
    public double getEnergyCapacity() {
        return this.sink.getAEMaxPower();
    }

}
