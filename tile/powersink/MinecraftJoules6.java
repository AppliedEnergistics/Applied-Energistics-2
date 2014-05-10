package appeng.tile.powersink;

import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.InterfaceList;
import appeng.transformer.annotations.integration.Method;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.IBatteryProvider;

@InterfaceList(value = { @Interface(iname = "MJ6", iface = "buildcraft.api.mj.IBatteryProvider"),
		@Interface(iname = "MJ6", iface = "buildcraft.api.mj.IBatteryObject") })
public abstract class MinecraftJoules6 extends AERootPoweredTile implements IBatteryProvider, IBatteryObject
{

	@Override
	@Method(iname = "MJ6")
	public double getEnergyRequested()
	{
		return getExternalPowerDemand( PowerUnits.MJ );
	}

	@Override
	@Method(iname = "MJ6")
	public double addEnergy(double amount)
	{
		double overflow = PowerUnits.MJ.convertTo( PowerUnits.AE, injectExternalPower( PowerUnits.MJ, amount ) );
		return amount - overflow;
	}

	@Override
	@Method(iname = "MJ6")
	public double addEnergy(double amount, boolean ignoreCycleLimit)
	{
		double overflow = PowerUnits.MJ.convertTo( PowerUnits.AE, injectExternalPower( PowerUnits.MJ, amount ) );
		return amount - overflow;
	}

	@Override
	@Method(iname = "MJ6")
	public double getEnergyStored()
	{
		return PowerUnits.AE.convertTo( PowerUnits.MJ, internalCurrentPower );
	}

	@Override
	@Method(iname = "MJ6")
	public void setEnergyStored(double mj)
	{
		internalCurrentPower = PowerUnits.MJ.convertTo( PowerUnits.AE, mj );
	}

	@Override
	@Method(iname = "MJ6")
	public double maxCapacity()
	{
		return PowerUnits.AE.convertTo( PowerUnits.MJ, internalMaxPower );
	}

	@Override
	@Method(iname = "MJ6")
	public double minimumConsumption()
	{
		return 0.1;
	}

	@Override
	@Method(iname = "MJ6")
	public double maxReceivedPerCycle()
	{
		return 999999.0;
	}

	@Override
	@Method(iname = "MJ6")
	public IBatteryObject reconfigure(double maxCapacity, double maxReceivedPerCycle, double minimumConsumption)
	{
		return getMjBattery();
	}

	@Override
	@Method(iname = "MJ6")
	public IBatteryObject getMjBattery()
	{
		return this;
	}

}
