package appeng.api.networking.energy;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;

public interface IEnergySource
{

	/**
	 * Extract power from the network.
	 * 
	 * @param amt extracted power
	 * @param mode should the action be simulated or performed?
	 * @return returns extracted power.
	 */
	public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier);

}
