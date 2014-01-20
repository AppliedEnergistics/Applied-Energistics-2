package appeng.core.features.registries;

import java.util.HashMap;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
import appeng.api.features.ILocateableRegistry;
import appeng.util.Platform;

public class LocateableRegistry implements ILocateableRegistry
{

	private HashMap<Long, ILocatable> set;

	@ForgeSubscribe
	public void updateLocateable(LocatableEventAnnounce e)
	{
		if ( Platform.isClient() )
			return; // IGNORE!

		if ( e.change == LocatableEvent.Register )
		{
			set.put( e.target.getLocatableSerial(), e.target );
		}
		else if ( e.change == LocatableEvent.Unregister )
		{
			set.remove( e.target.getLocatableSerial() );
		}
	}

	public LocateableRegistry() {
		set = new HashMap();
		MinecraftForge.EVENT_BUS.register( this );
	}

	/**
	 * Find a locate-able object by its serial.
	 */
	@Override
	public Object findLocateableBySerial(long ser)
	{
		return set.get( ser );
	}

}
