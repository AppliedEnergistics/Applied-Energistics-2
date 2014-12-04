package appeng.api.config;

public enum PowerMultiplier
{
	ONE, CONFIG;

	/**
	 * please do not edit this value, it is set when AE loads its config files.
	 */
	public double multiplier = 1.0;

	public double multiply(double in)
	{
		return in * multiplier;
	}

	public double divide(double in)
	{
		return in / multiplier;
	}
}
