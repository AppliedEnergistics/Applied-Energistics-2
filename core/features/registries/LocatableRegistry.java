package appeng.core.features.registries;

import java.util.HashMap;

import net.minecraftforge.common.MinecraftForge;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
import appeng.api.features.ILocatableRegistry;
import appeng.util.Platform;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class LocatableRegistry implements ILocatableRegistry
{

	private HashMap<Long, ILocatable> set;

	@SubscribeEvent
	public void updateLocatable(LocatableEventAnnounce e)
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

	public LocatableRegistry() {
		set = new HashMap();
		MinecraftForge.EVENT_BUS.register( this );
	}

	/**
	 * Find a locate-able object by its serial.
	 */
	@Override
	public Object findLocatableBySerial(long ser)
	{
		return set.get( ser );
	}

}
