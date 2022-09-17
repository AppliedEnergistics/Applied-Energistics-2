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


import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.core.AELog;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;


public class NetworkEventBus {
    private static final Collection<Class> READ_CLASSES = new HashSet<>();
    private static final Map<Class<? extends MENetworkEvent>, Map<Class, MENetworkEventInfo>> EVENTS = new HashMap<>();

    void readClass(final Class listAs, final Class c) {
        if (READ_CLASSES.contains(c)) {
            return;
        }
        READ_CLASSES.add(c);

        try {
            for (final Method m : c.getMethods()) {
                final MENetworkEventSubscribe s = m.getAnnotation(MENetworkEventSubscribe.class);
                if (s != null) {
                    final Class[] types = m.getParameterTypes();
                    if (types.length == 1) {
                        if (MENetworkEvent.class.isAssignableFrom(types[0])) {

                            Map<Class, MENetworkEventInfo> classEvents = EVENTS.get(types[0]);
                            if (classEvents == null) {
                                EVENTS.put(types[0], classEvents = new HashMap<>());
                            }

                            MENetworkEventInfo thisEvent = classEvents.get(listAs);
                            if (thisEvent == null) {
                                thisEvent = new MENetworkEventInfo();
                            }

                            thisEvent.Add(types[0], c, m);

                            classEvents.put(listAs, thisEvent);
                        } else {
                            throw new IllegalStateException("Invalid ME Network Event Subscriber, " + m
                                    .getName() + "s Parameter must extend MENetworkEvent.");
                        }
                    } else {
                        throw new IllegalStateException("Invalid ME Network Event Subscriber, " + m.getName() + " must have exactly 1 parameter.");
                    }
                }
            }
        } catch (final Throwable t) {
            throw new IllegalStateException("Error while adding " + c.getName() + " to event bus", t);
        }
    }

    MENetworkEvent postEvent(final Grid g, final MENetworkEvent e) {
        final Map<Class, MENetworkEventInfo> subscribers = EVENTS.get(e.getClass());
        int x = 0;

        try {
            if (subscribers != null) {
                for (final Entry<Class, MENetworkEventInfo> subscriber : subscribers.entrySet()) {
                    final MENetworkEventInfo target = subscriber.getValue();
                    final GridCacheWrapper cache = g.getCaches().get(subscriber.getKey());
                    if (cache != null) {
                        x++;
                        target.invoke(cache.getCache(), e);
                    }

                    // events may create or remove grid nodes in rare cases
                    final IMachineSet machines = g.getMachines(subscriber.getKey());
                    final List<IGridNode> work = new ArrayList<>(machines.size());
                    machines.forEach(work::add);

                    for (final IGridNode obj : work) {
                        // stil part of grid?
                        if (machines.contains(obj)) {
                            x++;
                            target.invoke(obj.getMachine(), e);
                        }
                    }
                }
            }
        } catch (final NetworkEventDone done) {
            // Early out.
        }

        e.setVisitedObjects(x);
        return e;
    }

    MENetworkEvent postEventTo(final Grid grid, final GridNode node, final MENetworkEvent e) {
        final Map<Class, MENetworkEventInfo> subscribers = EVENTS.get(e.getClass());
        int x = 0;

        try {
            if (subscribers != null) {
                final MENetworkEventInfo target = subscribers.get(node.getMachineClass());
                if (target != null) {
                    x++;
                    target.invoke(node.getMachine(), e);
                }
            }
        } catch (final NetworkEventDone done) {
            // Early out.
        }

        e.setVisitedObjects(x);
        return e;
    }

    private static class NetworkEventDone extends Throwable {

        private static final long serialVersionUID = -3079021487019171205L;
    }

    private class EventMethod {

        private final Class objClass;
        private final Method objMethod;
        private final Class objEvent;

        public EventMethod(final Class Event, final Class ObjClass, final Method ObjMethod) {
            this.objClass = ObjClass;
            this.objMethod = ObjMethod;
            this.objEvent = Event;
        }

        private void invoke(final Object obj, final MENetworkEvent e) throws NetworkEventDone {
            try {
                this.objMethod.invoke(obj, e);
            } catch (final Throwable e1) {
                AELog.error("[AppEng] Network Event caused exception:");
                AELog.error("Class: %1s, Object: %2s", obj.getClass().getName(), obj.toString());
                AELog.info(e1);
                throw new IllegalStateException(e1);
            }

            if (e.isCanceled()) {
                throw new NetworkEventDone();
            }
        }
    }

    private class MENetworkEventInfo {

        private final List<EventMethod> methods = new ArrayList<>();

        private void Add(final Class Event, final Class ObjClass, final Method ObjMethod) {
            this.methods.add(new EventMethod(Event, ObjClass, ObjMethod));
        }

        private void invoke(final Object obj, final MENetworkEvent e) throws NetworkEventDone {
            for (final EventMethod em : this.methods) {
                em.invoke(obj, e);
            }
        }
    }
}
