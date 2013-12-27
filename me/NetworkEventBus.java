package appeng.me;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkEventSubscribe;
import cpw.mods.fml.common.FMLLog;

public class NetworkEventBus
{

	class NetworkEventDone extends Throwable
	{

		private static final long serialVersionUID = -3079021487019171205L;

	};

	class MENetworkEventInfo
	{

		public final Class objClass;
		public final Method objMethod;
		public final Class objEvent;

		public MENetworkEventInfo(Class Event, Class ObjClass, Method ObjMethod) {
			this.objClass = ObjClass;
			this.objMethod = ObjMethod;
			this.objEvent = Event;
		}

		public void invoke(Object obj, MENetworkEvent e) throws NetworkEventDone
		{
			try
			{
				objMethod.invoke( obj, e );
			}
			catch (Throwable e1)
			{
				FMLLog.severe( "[AppEng] Network Event caused exception:" );
				FMLLog.severe( "Offending Class: " + obj.getClass().getName() );
				FMLLog.severe( "Offending Object: " + obj.toString() );
				e1.printStackTrace();
				throw new RuntimeException( e1 );
			}

			if ( e.isCanceled() )
				throw new NetworkEventDone();
		}
	};

	private static Set<Class> readClasses = new HashSet();
	private static Hashtable<Class<? extends MENetworkEvent>, Hashtable<Class, MENetworkEventInfo>> events = new Hashtable();

	public void readClass(Class listAs, Class c)
	{
		if ( readClasses.contains( c ) )
			return;
		readClasses.add( c );

		for (Method m : c.getMethods())
		{
			MENetworkEventSubscribe s = m.getAnnotation( MENetworkEventSubscribe.class );
			if ( s != null )
			{
				Class types[] = m.getParameterTypes();
				if ( types.length == 1 )
				{
					if ( MENetworkEvent.class.isAssignableFrom( types[0] ) )
					{

						Hashtable<Class, MENetworkEventInfo> classEvents = events.get( types[0] );
						if ( classEvents == null )
							events.put( types[0], classEvents = new Hashtable() );

						classEvents.put( listAs, new MENetworkEventInfo( types[0], c, m ) );
					}
					else
						throw new RuntimeException( "Invalid ME Network Event Subscriber, " + m.getName() + "s Parameter must extend MENetworkEvent." );
				}
				else
					throw new RuntimeException( "Invalid ME Network Event Subscriber, " + m.getName() + " must have exactly 1 parameter." );
			}
		}

	}

	public MENetworkEvent postEvent(Grid g, MENetworkEvent e)
	{
		Hashtable<Class, MENetworkEventInfo> subscribers = events.get( e.getClass() );
		int x = 0;

		try
		{
			if ( subscribers != null )
			{
				for (Class o : subscribers.keySet())
				{
					MENetworkEventInfo target = subscribers.get( o );
					GridCacheWrapper cache = g.caches.get( o );
					if ( cache != null )
					{
						x++;
						target.invoke( cache.myCache, e );
					}

					for (IGridNode obj : g.getMachines( o ))
					{
						x++;
						target.invoke( obj.getMachine(), e );
					}
				}
			}
		}
		catch (NetworkEventDone done)
		{
			// Early out.
		}

		e.setVisitedObjects( x );
		return e;
	}

	public MENetworkEvent postEventTo(Grid grid, GridNode node, MENetworkEvent e)
	{
		Hashtable<Class, MENetworkEventInfo> subscribers = events.get( e.getClass() );
		int x = 0;

		try
		{
			if ( subscribers != null )
			{
				MENetworkEventInfo target = subscribers.get( node.getMachineClass() );
				if ( target != null )
				{
					x++;
					target.invoke( node.getMachine(), e );
				}
			}
		}
		catch (NetworkEventDone done)
		{
			// Early out.
		}

		e.setVisitedObjects( x );
		return e;
	}
}
