package appeng.tile.powersink;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.integration.Interface;
import cofh.api.energy.IEnergyHandler;

@Interface(iname = "RF", iface = "cofh.api.energy.IEnergyHandler")
public abstract class RedstoneFlux extends RotaryCraft implements IEnergyHandler
{

	@Override
	final public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if ( simulate )
		{
			double demand = getExternalPowerDemand( PowerUnits.RF, maxReceive );
			if ( demand > maxReceive )
				return maxReceive;
			return (int) Math.floor( maxReceive - demand );
		}
		else
		{
			int demand = (int) Math.floor( getExternalPowerDemand( PowerUnits.RF, maxReceive ) );

			int ignored = 0;
			int insertAmt = maxReceive;

			if ( insertAmt > demand )
			{
				ignored = insertAmt - demand;
				insertAmt = demand;
			}

			double overFlow = injectExternalPower( PowerUnits.RF, insertAmt );
			double ox = Math.floor( overFlow );
			internalCurrentPower += PowerUnits.RF.convertTo( PowerUnits.AE, overFlow - ox );
			return maxReceive - ((int) ox + ignored);
		}
	}

	@Override
	final public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	final public boolean canConnectEnergy(ForgeDirection from)
	{
		return internalCanAcceptPower && getPowerSides().contains( from );
	}

	@Override
	final public int getEnergyStored(ForgeDirection from)
	{
		return (int) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, getAECurrentPower() ) );
	}

	@Override
	final public int getMaxEnergyStored(ForgeDirection from)
	{
		return (int) Math.floor( PowerUnits.AE.convertTo( PowerUnits.RF, getAEMaxPower() ) );
	}

}
