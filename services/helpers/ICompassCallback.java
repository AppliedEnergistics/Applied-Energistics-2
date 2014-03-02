package appeng.services.helpers;

public interface ICompassCallback
{

	/**
	 * Called from another thread.
	 * 
	 * @param hasResult
	 * @param spin
	 * @param radians
	 */
	public void calculatedDirection(boolean hasResult, boolean spin, double radians, double dist);

}
