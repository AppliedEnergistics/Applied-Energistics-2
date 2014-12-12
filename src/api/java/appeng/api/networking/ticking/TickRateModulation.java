package appeng.api.networking.ticking;

public enum TickRateModulation
{
	/**
	 * same as idle, but also puts the node to sleep.
	 */
	SLEEP,

	/**
	 * set tick rate to maximum.
	 */
	IDLE,

	/**
	 * decrease the tick rate marginally.
	 */
	SLOWER,

	/**
	 * continue at current rate.
	 */
	SAME,

	/**
	 * increase the tick rate marginally.
	 */
	FASTER,

	/**
	 * changes the tick rate to the minimum tick rate.
	 */
	URGENT
}
