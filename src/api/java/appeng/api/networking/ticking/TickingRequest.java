package appeng.api.networking.ticking;

/**
 * 
 * Describes how your tiles ticking is executed.
 * 
 */
public class TickingRequest
{

	/**
	 * the minimum number of ticks that must pass between ticks.
	 * 
	 * Valid Values are : 1+
	 * 
	 * Suggested is 5-20
	 * 
	 */
	public final int minTickRate;

	/**
	 * the maximum number of ticks that can pass between ticks, if this value is
	 * exceeded the tile must tick.
	 * 
	 * Valid Values are 1+
	 * 
	 * Suggested is 20-40
	 * 
	 */
	public final int maxTickRate;

	/**
	 * 
	 * Determines the current expected state of your node, if your node expects
	 * to be sleeping, then return true.
	 * 
	 */
	public final boolean isSleeping;

	/**
	 * 
	 * True only if you call {@link ITickManager}.alertDevice( IGridNode );
	 * 
	 */
	public final boolean canBeAlerted;

	public TickingRequest(int min, int max, boolean sleep, boolean alertable) {
		minTickRate = min;
		maxTickRate = max;
		isSleeping = sleep;
		canBeAlerted = alertable;
	}

}
