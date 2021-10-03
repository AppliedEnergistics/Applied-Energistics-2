/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.networking;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry of grid services to extend grid functionality.
 */
@ThreadSafe
public final class GridServices {

    private GridServices() {
    }

    // This must not be re-sorted because of interdependencies between the registrations
    private static final List<GridCacheRegistration<?>> registry = new ArrayList<>();

    /**
     * Register a new grid service for use during operation, must be called during the loading phase.
     * <p/>
     * AE will automatically construct instances of the given implementation class by looking up a constructor. There
     * must be a single constructor.
     * <p/>
     * The following constructor parameter types are allowed:
     * <ul>
     * <li>Other grid services public interfaces (see interfaces extending {@link IGridService}).</li>
     * <li>{@link IGrid}, which will be the grid that the service is being constructed for.</li>
     * </ul>
     *
     * @param publicInterface The public facing interface of the grid service you want to register. This class or
     *                        interface will also be used to query the service from any grid via
     *                        {@link IGrid#getService(Class)}.
     * @param implClass       The class used to construct the grid service for each grid. Must have a single
     *                        constructor.
     */
    public static synchronized <T extends IGridServiceProvider> void register(Class<? super T> publicInterface,
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

    private static boolean isRegistered(Class<?> publicInterface) {
        return registry.stream().anyMatch(r -> r.publicInterface.equals(publicInterface));
    }

    /**
     * Constructs all registered services for the given grid.
     * <p/>
     * This is used by AE2 internally to initialize the services for a grid.
     */
    static Map<Class<?>, IGridServiceProvider> createServices(IGrid g) {
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

        @Nonnull
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
