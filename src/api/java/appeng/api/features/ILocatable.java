package appeng.api.features;

import appeng.api.events.LocatableEventAnnounce;

/**
 * A registration record for the {@link ILocatableRegistry} use the {@link LocatableEventAnnounce} event on the Forge
 * Event bus to update the registry.
 */
public interface ILocatable
{

	/**
	 * @return the serial for a locatable object
	 */
	long getLocatableSerial();

}