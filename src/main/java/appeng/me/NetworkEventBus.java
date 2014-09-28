package appeng.me;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.core.AELog;

public class NetworkEventBus
{

	class NetworkEventDone extends Throwable
	{

		private static final long serialVersionUID = -3079021487019171205L;

	}

	class EventMethod
	{

		public final Class objClass;
		public final Method objMethod;
		public final Class objEvent;

		public EventMethod(Class Event, Class ObjClass, Method ObjMethod) {
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
				AELog.severe( "[AppEng] Network Event caused exception:" );
				AELog.severe( "Offending Class: " + obj.getClass().getName() );
				AELog.severe( "Offending Object: " + obj.toString() );
				AELog.error( e1 );
				throw new RuntimeException( e1 );
			}

			if ( e.isCanceled() )
				throw new NetworkEventDone();
		}
	}

	class MENetworkEventInfo
	{

		private ArrayList<EventMethod> methods = new ArrayList();

		public void Add(Class Event, Class ObjClass, Method ObjMethod)
		{
			methods.add( new EventMethod( Event, ObjClass, ObjMethod ) );
		}

		public void invoke(Object obj, MENetworkEvent e) throws NetworkEventDone
		{
			for (EventMethod em : methods)
				em.invoke( obj, e );
		}
	}

	private static Set<Class> readClasses = new HashSet();
	private static Hashtable<Class<? extends MENetworkEvent>, Hashtable<Class, MENetworkEventInfo>> events = new Hashtable();

	public void readClass(Class listAs, Class c)
	{
		if ( readClasses.contains( c ) )
			return;
		readClasses.add( c );

		try
		{
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
	
							MENetworkEventInfo thisEvent = classEvents.get( listAs );
							if ( thisEvent == null )
								thisEvent = new MENetworkEventInfo();
	
							thisEvent.Add( types[0], c, m );
	
							classEvents.put( listAs, thisEvent );
						}
						else
							throw new RuntimeException( "Invalid ME Network Event Subscriber, " + m.getName() + "s Parameter must extend MENetworkEvent." );
					}
					else
						throw new RuntimeException( "Invalid ME Network Event Subscriber, " + m.getName() + " must have exactly 1 parameter." );
				}
			}
		}
		catch(Throwable t )
		{
			throw new RuntimeException( "Error while adding "+c.getName()+" to event bus", t );
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
