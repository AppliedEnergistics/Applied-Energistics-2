package appeng.api.networking.events;

import appeng.api.networking.energy.IAEPowerStorage;

/**
 * informs the network, that a {@link IAEPowerStorage} block that had either run,
 * out of power, or was full, is no longer in that state.
 * 
 * failure to post this event when your {@link IAEPowerStorage} changes state will
 * result in your block not charging, or not-discharging.
 * 
 * you do not need to send this event when your node is added / removed from the grid.
 */
public class MENetworkPowerStorage extends MENetworkEvent
{

	public enum PowerEventType
	{
		/**
		 * informs the network this tile is ready to receive power again.
		 */
		REQUEST_POWER, 
		
		/**
		 * informs the network this tile is ready to provide power again.
		 */
		PROVIDE_POWER 
	}

	public final IAEPowerStorage storage;
	public final PowerEventType type;

	public MENetworkPowerStorage(IAEPowerStorage t, PowerEventType y) {
		storage = t;
		type = y;
	}

}
