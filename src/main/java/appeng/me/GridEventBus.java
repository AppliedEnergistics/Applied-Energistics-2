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

import appeng.api.networking.IGrid;
import appeng.api.networking.events.GridEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class GridEventBus {
    private static final Map<Class<? extends GridEvent>, Subscriptions<?>> EVENTS = new HashMap<>();

    private static class Subscriptions<T extends GridEvent> {
        private final Class<T> eventClass;
        private final List<BiConsumer<IGrid, T>> handlers = new ArrayList<>();

        private Subscriptions(Class<T> eventClass) {
            this.eventClass = eventClass;
        }

        public void subscribe(BiConsumer<IGrid, T> handler) {
            handlers.add(handler);
        }

        public void invoke(IGrid grid, GridEvent event) {
            var typedEvent = eventClass.cast(event);
            for (var handler : handlers) {
                handler.accept(grid, typedEvent);
            }
        }
    }

    private GridEventBus() {
    }

    @SuppressWarnings("unchecked")
    private static <T extends GridEvent> Subscriptions<T> getSubscriptions(Class<T> eventClass) {
        return (Subscriptions<T>) EVENTS.computeIfAbsent(eventClass, Subscriptions::new);
    }

    public static <T extends GridEvent> void subscribe(Class<T> eventClass, BiConsumer<IGrid, T> handler) {
        getSubscriptions(eventClass).subscribe(handler);
    }

    public static void postEvent(Grid g, GridEvent e) {
        getSubscriptions(e.getClass()).invoke(g, e);
    }
}
