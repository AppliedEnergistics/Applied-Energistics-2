package appeng.tile.powersink;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import gregtech.api.capability.IEnergyContainer;
import net.minecraft.util.EnumFacing;


public class GTCEEnergyAdapter implements IEnergyContainer
{
    private final IExternalPowerSink sink;

    GTCEEnergyAdapter( IExternalPowerSink sink )
    {
        this.sink = sink;
    }

    @Override
    public long acceptEnergyFromNetwork( EnumFacing enumFacing, long voltage, long amperage )
    {
        final double overflow = this.sink.injectExternalPower( PowerUnits.GTCEU, (double) voltage, Actionable.MODULATE );

        return (long) ( voltage - overflow );
    }

    @Override
    public boolean inputsEnergy( EnumFacing enumFacing )
    {
        return true;
    }

    @Override
    public long changeEnergy( long l )
    {
        return 0;
    }

    @Override
    public long getEnergyStored()
    {
        return (long) Math.floor( PowerUnits.AE.convertTo( PowerUnits.GTCEU, this.sink.getAECurrentPower() ) );
    }

    @Override
    public long getEnergyCapacity()
    {
        return (long) Math.floor( PowerUnits.AE.convertTo( PowerUnits.GTCEU, this.sink.getAEMaxPower() ) );
    }

    @Override
    public long getInputAmperage()
    {
        return 0;
    }

    @Override
    public long getInputVoltage()
    {
        return 0;
    }

    @Override
    public long getEnergyCanBeInserted()
    {
        return this.getEnergyCapacity() - this.getEnergyStored();
    }
}
