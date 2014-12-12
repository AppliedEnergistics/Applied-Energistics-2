package appeng.api.networking.pathing;

public enum ControllerState
{
	/**
	 * No controller blocks are present in the network.
	 */
	NO_CONTROLLER,

	/**
	 * Controller rules followed, booting enabled.
	 */
	CONTROLLER_ONLINE,

	/**
	 * Controller rules not followed, lock up while booting.
	 */
	CONTROLLER_CONFLICT
}
