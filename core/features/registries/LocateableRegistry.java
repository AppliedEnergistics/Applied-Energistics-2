package appeng.core.features.registries;

import java.util.HashMap;

import net.minecraftforge.common.MinecraftForge;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
import appeng.api.features.ILocatableRegistry;
import appeng.util.Platform;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class LocateableRegistry implements ILocatableRegistry
{

	private HashMap<Long, ILocatable> set;

	@SubscribeEvent
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
