package appeng.tile.powersink;


import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import net.minecraftforge.energy.IEnergyStorage;


/**
 * Adapts an {@link IExternalPowerSink} to Forges {@link IEnergyStorage}.
 */
class ForgeEnergyAdapter implements IEnergyStorage {

    private final IExternalPowerSink sink;

    ForgeEnergyAdapter(IExternalPowerSink sink) {
        this.sink = sink;
    }

    @Override
    public final int receiveEnergy(int maxReceive, boolean simulate) {
        final double offered = maxReceive;
        final double overflow = this.sink.injectExternalPower(PowerUnits.RF, offered, simulate ? Actionable.SIMULATE : Actionable.MODULATE);

        return (int) (maxReceive - overflow);
    }

    @Override
    public final int getEnergyStored() {
        return (int) Math.floor(PowerUnits.AE.convertTo(PowerUnits.RF, this.sink.getAECurrentPower()));
    }

    @Override
    public final int getMaxEnergyStored() {
        return (int) Math.floor(PowerUnits.AE.convertTo(PowerUnits.RF, this.sink.getAEMaxPower()));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

}
