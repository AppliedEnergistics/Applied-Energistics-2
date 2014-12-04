package appeng.api.implementations;

/**
 * This is intended for use on the client side to provide details to WAILA.
 */
public interface IPowerChannelState
{

	/**
	 * @return true if the part/tile is powered.
	 */
	boolean isPowered();

	/**
	 * @return true if the part/tile isActive
	 */
	boolean isActive();

}
