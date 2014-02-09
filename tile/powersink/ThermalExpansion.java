package appeng.tile.powersink;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.Optional.Interface;

@Interface(modid = "ThermalExpansion", iface = "cofh.api.energy.IEnergyHandler")
public abstract class ThermalExpansion extends RotaryCraft implements IEnergyHandler
{

	@Override
	final public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if ( simulate )
		{
			double demand = getExternalPowerDemand( PowerUnits.RF );
			if ( demand > maxReceive )
				return maxReceive;
			return (int) Math.floor( maxReceive - demand );
		}
		else
		{
			double overFlow = injectExternalPower( PowerUnits.RF, maxReceive );
			double ox = Math.floor( overFlow );
			internalCurrentPower += PowerUnits.RF.convertTo( PowerUnits.AE, overFlow - ox );
			return maxReceive - (int) ox;
		}
	}

	@Override
	final public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	final public boolean canInterface(ForgeDirection from)
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
