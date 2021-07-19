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

package appeng.core.registries;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.IGridServiceRegistry;

public final class GridServiceRegistry implements IGridServiceRegistry {

    // This must not be re-sorted because of interdependencies between the registrations
    private final List<GridCacheRegistration<?>> registry = new ArrayList<>();

    @Override
    public synchronized final <T extends IGridServiceProvider> void register(Class<? super T> publicInterface,
            Class<T> implClass) {
        if (isRegistered(publicInterface)) {
            throw new IllegalArgumentException(
                    "Implementation for grid service " + publicInterface + " is already registered!");
        }

        var registration = new GridCacheRegistration<>(implClass, publicInterface);

        // Check if the registration has unmet dependencies. This frees us from dealing
        // with circular dependencies too.
        for (Class<?> dependency : registration.dependencies) {
            if (!isRegistered(dependency)) {
                throw new IllegalStateException("Missing dependency declared in constructor of "
                        + implClass + ": " + dependency);
            }
        }

        registry.add(registration);
    }

    private boolean isRegistered(Class<?> publicInterface) {
        return registry.stream().anyMatch(r -> r.publicInterface.equals(publicInterface));
    }

    public Map<Class<?>, IGridServiceProvider> createGridServices(final IGrid g) {
        var result = new HashMap<Class<?>, IGridServiceProvider>(registry.size());

        for (var registration : registry) {
            result.put(registration.publicInterface, registration.construct(g, result));
        }

        return result;
    }

    private static class GridCacheRegistration<T extends IGridServiceProvider> {

        private final Class<T> implClass;

        private final Class<?> publicInterface;

        private final Constructor<T> constructor;

        private final Class<?>[] constructorParameterTypes;

        private final Set<Class<?>> dependencies;

        @SuppressWarnings("unchecked")
        public GridCacheRegistration(Class<T> implClass, Class<?> publicInterface) {
            this.publicInterface = publicInterface;
            this.implClass = implClass;

            // Find the constructor
            var ctors = (Constructor<T>[]) implClass.getConstructors();
            if (ctors.length != 1) {
                throw new IllegalArgumentException("Grid service implementation " + implClass
                        + " has " + ctors.length + " public constructors. It needs exactly 1.");
            }
            this.constructor = ctors[0];
            this.constructorParameterTypes = this.constructor.getParameterTypes();
            this.dependencies = Arrays.stream(this.constructorParameterTypes)
                    .filter(t -> !t.equals(IGrid.class))
                    .collect(Collectors.toSet());
        }

        @NotNull
        public IGridServiceProvider construct(IGrid g, Map<Class<?>, IGridServiceProvider> createdServices) {
            // Fill the constructor arguments
            var ctorArgs = new Object[constructorParameterTypes.length];
            for (int i = 0; i < constructorParameterTypes.length; i++) {
                var paramType = constructorParameterTypes[i];
                if (paramType.equals(IGrid.class)) {
                    ctorArgs[i] = g;
                } else {
                    ctorArgs[i] = createdServices.get(paramType);
                    if (ctorArgs[i] == null) {
                        throw new IllegalStateException("Unsatisfied constructor dependency " + paramType + " in "
                                + constructor);
                    }
                }
            }

            // Finally call the constructor
            IGridServiceProvider provider;
            try {
                provider = constructor.newInstance(ctorArgs);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to create grid because grid service " + implClass
                        + " failed to construct.", e);
            }
            return provider;
        }

    }

}
