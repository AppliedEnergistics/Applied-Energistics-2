package appeng.api.implementations;

/**
 * Defines the result of performing a transition from the world into a storage
 * cell, if its possible, and what the energy usage is.
 */
public class TransitionResult
{
	public final boolean success;
	public final double energyUsage;

	public TransitionResult(boolean _success, double power) {
		success = _success;
		energyUsage = power;
	}
}
