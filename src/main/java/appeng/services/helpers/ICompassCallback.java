package appeng.services.helpers;

public interface ICompassCallback
{

	/**
	 * Called from another thread.
	 * 
	 * @param hasResult true if found a target
	 * @param spin true if should spin
	 * @param radians radians
	 * @param dist distance
	 */
	public void calculatedDirection(boolean hasResult, boolean spin, double radians, double dist);

}
