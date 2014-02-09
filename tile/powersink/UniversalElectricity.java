package appeng.tile.powersink;

/*
import net.minecraftforge.common.util.ForgeDirection;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import appeng.api.config.PowerUnits;

public abstract class UniversalElectricity extends ThermalExpansion implements IElectrical
{

	@Override
	final public boolean canConnect(ForgeDirection direction)
	{
		return internalCanAcceptPower && getPowerSides().contains( direction );
	}

	@Override
	final public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
	{
		float accepted = 0;
		double recievedPower = receive.getWatts();

		if ( doReceive )
		{
			accepted = (float) (recievedPower - injectExternalPower( PowerUnits.KJ, recievedPower ));
		}
		else
		{
			double whatIWant = getExternalPowerDemand( PowerUnits.KJ );
			if ( whatIWant > recievedPower )
				accepted = (float) recievedPower;
			else
				accepted = (float) whatIWant;
		}

		return accepted;
	}

	@Override
	final public float getRequest(ForgeDirection direction)
	{
		return (float) getExternalPowerDemand( PowerUnits.KJ );
	}

	@Override
	final public float getVoltage()
	{
		return 120;
	}

	@Override
	final public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide)
	{
		return null; // cannot be dis-charged
	}

	@Override
	final public float getProvide(ForgeDirection direction)
	{
		return 0;
	}

}
*/