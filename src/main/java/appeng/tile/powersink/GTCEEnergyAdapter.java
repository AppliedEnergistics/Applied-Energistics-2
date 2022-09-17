package appeng.tile.powersink;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.util.EnumFacing;


public class GTCEEnergyAdapter implements IEnergyContainer {
    private final IExternalPowerSink sink;

    double EUBuffer;

    GTCEEnergyAdapter(IExternalPowerSink sink) {
        this.sink = sink;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing enumFacing, long voltage, long amperage) {
        final double power = voltage * amperage;
        final double oldBuffer = EUBuffer;

        EUBuffer = this.sink.injectExternalPower(PowerUnits.GTEU, power + EUBuffer, Actionable.MODULATE);

        // if the overflow went down, all inputs were consumed
        if (EUBuffer <= oldBuffer) {
            return amperage;
        }
        // if overflow is greater than the inputs, nothing was consumed
        if (EUBuffer >= power) {
            EUBuffer -= power;
            return 0;
        }
        // determine how many amps are being used
        final double usedEU = power + oldBuffer - EUBuffer;
        final long ampsUsed = (long) Math.ceil(usedEU / ((double) voltage));
        // adjust the overflow
        EUBuffer += usedEU % voltage;

        return ampsUsed;
    }

    @Override
    public boolean inputsEnergy(EnumFacing enumFacing) {
        return true;
    }

    @Override
    public long changeEnergy(long l) {
        return 0;
    }

    @Override
    public long getEnergyStored() {
        return (long) Math.floor(PowerUnits.AE.convertTo(PowerUnits.GTEU, this.sink.getAECurrentPower()));
    }

    @Override
    public long getEnergyCapacity() {
        return (long) Math.floor(PowerUnits.AE.convertTo(PowerUnits.GTEU, this.sink.getAEMaxPower()));
    }

    @Override
    public long getInputAmperage() {
        return 0;
    }

    @Override
    public long getInputVoltage() {
        return 0;
    }

    @Override
    public long getEnergyCanBeInserted() {
        return this.getEnergyCapacity() - this.getEnergyStored();
    }
}
