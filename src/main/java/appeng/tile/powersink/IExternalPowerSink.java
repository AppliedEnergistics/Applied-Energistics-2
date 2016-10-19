package appeng.tile.powersink;


import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;


interface IExternalPowerSink extends IAEPowerStorage
{

	double injectExternalPower( PowerUnits input, double amt );

	double getExternalPowerDemand( PowerUnits externalUnit, double maxPowerRequired );

}
