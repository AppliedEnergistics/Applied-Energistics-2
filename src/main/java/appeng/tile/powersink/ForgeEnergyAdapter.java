package appeng.tile.powersink;



import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.config.PowerUnits;


/**
 * Adapts an {@link IExternalPowerSink} to Forges {@link IEnergyStorage}.
 */
class ForgeEnergyAdapter implements IEnergyStorage
{

	private final IExternalPowerSink sink;

	ForgeEnergyAdapter( IExternalPowerSink sink )
	{
		this.sink = sink;
	}

	@Override
	public final int receiveEnergy( int maxReceive, boolean simulate )
	{
		final int networkDemand = (int) Math.floor( sink.getExternalPowerDemand( PowerUnits.RF, maxReceive ) );
		final int used = Math.min( maxReceive, networkDemand );

		if( !simulate )
		{
			sink.injectExternalPower( PowerUnits.RF, used );
		}

		return used;
	}

	@Override
	public final int getEnergyStored()
	{
		return (int) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, sink.getAECurrentPower() ) );
	}

	@Override
	public final int getMaxEnergyStored( )
	{
		return (int) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, sink.getAEMaxPower() ) );
	}

	@Override
	public int extractEnergy( int maxExtract, boolean simulate )
	{
		return 0;
	}

	@Override
	public boolean canExtract()
	{
		return false;
	}

	@Override
	public boolean canReceive()
	{
		return true;
	}

}
