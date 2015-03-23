/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.me;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.core.AELog;


public class NetworkEventBus
{
	private static final Collection<Class> READ_CLASSES = new HashSet<Class>();
	private static final Map<Class<? extends MENetworkEvent>, Map<Class, MENetworkEventInfo>> EVENTS = new HashMap<Class<? extends MENetworkEvent>, Map<Class, MENetworkEventInfo>>();

	public void readClass( Class listAs, Class c )
	{
		if( READ_CLASSES.contains( c ) )
			return;
		READ_CLASSES.add( c );

		try
		{
			for( Method m : c.getMethods() )
			{
				MENetworkEventSubscribe s = m.getAnnotation( MENetworkEventSubscribe.class );
				if( s != null )
				{
					Class[] types = m.getParameterTypes();
					if( types.length == 1 )
					{
						if( MENetworkEvent.class.isAssignableFrom( types[0] ) )
						{

							Map<Class, MENetworkEventInfo> classEvents = EVENTS.get( types[0] );
							if( classEvents == null )
								EVENTS.put( types[0], classEvents = new HashMap<Class, MENetworkEventInfo>() );

							MENetworkEventInfo thisEvent = classEvents.get( listAs );
							if( thisEvent == null )
								thisEvent = new MENetworkEventInfo();

							thisEvent.Add( types[0], c, m );

							classEvents.put( listAs, thisEvent );
						}
						else
							throw new IllegalStateException( "Invalid ME Network Event Subscriber, " + m.getName() + "s Parameter must extend MENetworkEvent." );
					}
					else
						throw new IllegalStateException( "Invalid ME Network Event Subscriber, " + m.getName() + " must have exactly 1 parameter." );
				}
			}
		}
		catch( Throwable t )
		{
			throw new IllegalStateException( "Error while adding " + c.getName() + " to event bus", t );
		}
	}

	public MENetworkEvent postEvent( Grid g, MENetworkEvent e )
	{
		Map<Class, MENetworkEventInfo> subscribers = EVENTS.get( e.getClass() );
		int x = 0;

		try
		{
			if( subscribers != null )
			{
				for( Entry<Class, MENetworkEventInfo> subscriber : subscribers.entrySet() )
				{
					MENetworkEventInfo target = subscriber.getValue();
					GridCacheWrapper cache = g.getCaches().get( subscriber.getKey() );
					if( cache != null )
					{
						x++;
						target.invoke( cache.myCache, e );
					}

					for( IGridNode obj : g.getMachines( subscriber.getKey() ) )
					{
						x++;
						target.invoke( obj.getMachine(), e );
					}
				}
			}
		}
		catch( NetworkEventDone done )
		{
			// Early out.
		}

		e.setVisitedObjects( x );
		return e;
	}

	public MENetworkEvent postEventTo( Grid grid, GridNode node, MENetworkEvent e )
	{
		Map<Class, MENetworkEventInfo> subscribers = EVENTS.get( e.getClass() );
		int x = 0;

		try
		{
			if( subscribers != null )
			{
				MENetworkEventInfo target = subscribers.get( node.getMachineClass() );
				if( target != null )
				{
					x++;
					target.invoke( node.getMachine(), e );
				}
			}
		}
		catch( NetworkEventDone done )
		{
			// Early out.
		}

		e.setVisitedObjects( x );
		return e;
	}

	static class NetworkEventDone extends Throwable
	{

		private static final long serialVersionUID = -3079021487019171205L;
	}


	class EventMethod
	{

		public final Class objClass;
		public final Method objMethod;
		public final Class objEvent;

		public EventMethod( Class Event, Class ObjClass, Method ObjMethod )
		{
			this.objClass = ObjClass;
			this.objMethod = ObjMethod;
			this.objEvent = Event;
		}

		public void invoke( Object obj, MENetworkEvent e ) throws NetworkEventDone
		{
			try
			{
				this.objMethod.invoke( obj, e );
			}
			catch( Throwable e1 )
			{
				AELog.severe( "[AppEng] Network Event caused exception:" );
				AELog.severe( "Offending Class: " + obj.getClass().getName() );
				AELog.severe( "Offending Object: " + obj.toString() );
				AELog.error( e1 );
				throw new IllegalStateException( e1 );
			}

			if( e.isCanceled() )
				throw new NetworkEventDone();
		}
	}


	class MENetworkEventInfo
	{

		private final List<EventMethod> methods = new ArrayList<EventMethod>();

		public void Add( Class Event, Class ObjClass, Method ObjMethod )
		{
			this.methods.add( new EventMethod( Event, ObjClass, ObjMethod ) );
		}

		public void invoke( Object obj, MENetworkEvent e ) throws NetworkEventDone
		{
			for( EventMethod em : this.methods )
				em.invoke( obj, e );
		}
	}
}
