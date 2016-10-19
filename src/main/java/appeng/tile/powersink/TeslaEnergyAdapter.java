package appeng.tile.powersink;


import net.darkhax.tesla.api.ITeslaConsumer;

import appeng.api.config.PowerUnits;


/**
 * Adapts an {@link IExternalPowerSink} to Forges {@link net.darkhax.tesla.api.ITeslaConsumer}.
 */
class TeslaEnergyAdapter implements ITeslaConsumer
{

	private final IExternalPowerSink sink;

	TeslaEnergyAdapter( IExternalPowerSink sink )
	{
		this.sink = sink;
	}

	@Override
	public long givePower( long power, boolean simulated )
	{
		// Cut it down to what we can represent in a double
		double powerDbl = (double) power;

		double networkDemand = sink.getExternalPowerDemand( PowerUnits.RF, powerDbl );
		long used = (long) Math.min( powerDbl, networkDemand );

		if( !simulated )
		{
			sink.injectExternalPower( PowerUnits.RF, used );
		}

		return used;
	}

}
