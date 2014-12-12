package appeng.api.events;

import appeng.api.features.ILocatable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Input Event:
 * 
 * Used to Notify the Location Registry of objects, and their availability.
 */
public class LocatableEventAnnounce extends Event
{

	public enum LocatableEvent
	{
		Register, // Adds the locatable to the registry
		Unregister // Removes the locatable from the registry
	}

	final public ILocatable target;
	final public LocatableEvent change;

	public LocatableEventAnnounce(ILocatable o, LocatableEvent ev) {
		target = o;
		change = ev;
	}

}
