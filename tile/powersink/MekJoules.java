package appeng.tile.powersink;

import mekanism.api.energy.IStrictEnergyAcceptor;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.integration.Interface;

@Interface(iname = "Mekanism", iface = "mekanism.api.energy.IStrictEnergyAcceptor")
public abstract class MekJoules extends RedstoneFlux implements IStrictEnergyAcceptor   {

	@Override
	public double getEnergy() {
		return 0;
	}

	@Override
	public void setEnergy(double energy) {
		double extra = injectExternalPower( PowerUnits.MK, energy );
		internalCurrentPower += PowerUnits.MK.convertTo(PowerUnits.AE, extra );
	}

	@Override
	public double getMaxEnergy() {
		return this.getExternalPowerDemand( PowerUnits.MK, 100000 );
	}

	@Override
	public double transferEnergyToAcceptor(ForgeDirection side, double amount)
	{
		double demand = getExternalPowerDemand( PowerUnits.MK, Double.MAX_VALUE );
		if ( amount > demand )
			amount = demand;

		double overflow = injectExternalPower( PowerUnits.MK, amount );
		return amount - overflow;
	}

	@Override
	public boolean canReceiveEnergy(ForgeDirection side) {
		return getPowerSides().contains(side);
	}

}
