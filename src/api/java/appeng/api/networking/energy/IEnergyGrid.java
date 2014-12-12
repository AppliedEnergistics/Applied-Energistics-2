package appeng.api.networking.energy;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridCache;
import appeng.api.networking.events.MENetworkPowerStatusChange;

/**
 * AE's Power system.
 */
public interface IEnergyGrid extends IGridCache, IEnergySource, IEnergyGridProvider
{

	/**
	 * @return the current calculated idle energy drain each tick, is used internally to drain power for each tick.
	 */
	public double getIdlePowerUsage();

	/**
	 * @return the average power drain over the past 10 ticks, includes idle usage during this time, and all use of
	 *         extractPower.
	 */
	public double getAvgPowerUsage();

	/**
	 * @return the average energy injected into the system per tick, for the last 10 ticks.
	 */
	public double getAvgPowerInjection();

	/**
	 * AE maintains an idle draw of power separate from active power draw, it condenses this into a single operation
	 * that determines the networks "powered state" if the network is considered off-line, your machines should not
	 * function.
	 * 
	 * {@link MENetworkPowerStatusChange} events are posted when this value changes if you need to be notified of the
	 * change, most machines can simply test the value when they operate.
	 * 
	 * @return if the network is powered or not.
	 */
	public boolean isNetworkPowered();

	/**
	 * AE will accept any power, and store it, to maintain sanity please don't send more then 10,000 at a time.
	 * 
	 * IMPORTANT: Network power knows no bounds, for less spamy power flow, networks can store more then their allotted
	 * storage, however, it should be kept to a minimum, to help with this, this method returns the networks current
	 * OVERFLOW, this is not energy you can store some where else, its already stored in the network, you can extract it
	 * if you want, however it it owned by the network, this is different then IAEEnergyStore
	 * 
	 * Another important note, is that if a network that had overflow is deleted, its power is gone, this is one of the
	 * reasons why keeping overflow to a minimum is important.
	 * 
	 * @param amt
	 *            power to inject into the network
	 * @param mode
	 *            should the action be simulated or performed?
	 * @return the amount of power that the network has OVER the limit.
	 */
	public double injectPower(double amt, Actionable mode);

	/**
	 * this is should be considered an estimate, and not relied upon for real calculations.
	 * 
	 * @return estimated available power.
	 */
	public double getStoredPower();

	/**
	 * this is should be considered an estimate, and not relied upon for real calculations.
	 * 
	 * @return estimated available power.
	 */
	double getMaxStoredPower();

	/**
	 * Calculation will be capped at maxRequired, this improves performance by limiting the number of nodes needed to
	 * calculate the demand.
	 * 
	 * @return Amount of power required to charge the grid, in AE.
	 */
	public double getEnergyDemand(double maxRequired);

}
