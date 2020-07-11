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

package appeng.core.features.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridCacheFactory;
import appeng.api.networking.IGridCacheRegistry;

public final class GridCacheRegistry implements IGridCacheRegistry {

    private final List<GridCacheRegistration<?>> registry = new ArrayList<>();

    @Override
    public synchronized <T extends IGridCache> void registerGridCache(@Nonnull Class<T> iface,
            @Nonnull IGridCacheFactory<T> factory) {

        if (registry.stream().anyMatch(r -> r.cacheClass.equals(iface))) {
            throw new IllegalArgumentException("Implementation for grid cache " + iface + " is already registered!");
        }

        registry.add(new GridCacheRegistration<>(iface, factory));

    }

    @Override
    public Map<Class<? extends IGridCache>, IGridCache> createCacheInstance(final IGrid g) {
        final Map<Class<? extends IGridCache>, IGridCache> map = new HashMap<>(registry.size());

        for (GridCacheRegistration<?> registration : registry) {
            map.put(registration.cacheClass, registration.factory.createCache(g));
        }

        return map;
    }

    private static class GridCacheRegistration<T extends IGridCache> {

        private final Class<T> cacheClass;

        private final IGridCacheFactory<T> factory;

        public GridCacheRegistration(Class<T> cacheClass, IGridCacheFactory<T> factory) {
            this.cacheClass = cacheClass;
            this.factory = factory;
        }

    }

}
