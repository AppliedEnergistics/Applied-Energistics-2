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

package appeng.core;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import org.objectweb.asm.Type;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

import appeng.api.AEInjectable;
import appeng.api.AEPlugin;

/**
 * Loads AE plugins on startup and provides them with access to various
 * components of the AE API.
 */
class PluginLoader {

    public void loadPlugins(Collection<Object> injectables) {
        Map<Class<?>, Object> injectableMap = mapInjectables(injectables);
        findAndInstantiatePlugins(injectableMap);
    }

    private static void findAndInstantiatePlugins(Map<Class<?>, Object> injectableMap) {
        Type aType = Type.getType(AEPlugin.class);
        Set<ModFileScanData.AnnotationData> allAnnotated = ModList.get().getAllScanData().stream()
                .map(ModFileScanData::getAnnotations).flatMap(Collection::stream)
                .filter(a -> a.getAnnotationType().equals(aType)).filter(a -> a.getTargetType() == ElementType.TYPE)
                .collect(Collectors.toSet());

        for (ModFileScanData.AnnotationData candidate : allAnnotated) {

            String cName = candidate.getMemberName();
            Class<?> aClass;
            try {
                aClass = Class.forName(cName);
            } catch (ClassNotFoundException e) {
                AELog.error(e, "Couldn't find annotated AE plugin class " + cName);
                throw new RuntimeException("Couldn't find annotated AE plugin class " + cName, e);
            }

            // Try instantiating the plugin
            try {
                Object plugin = instantiatePlugin(aClass, injectableMap);
                AELog.info("Loaded AE2 Plugin {}", plugin.getClass());
            } catch (Exception e) {
                AELog.error(e, "Unable to instantiate AE plugin " + cName);
                throw new RuntimeException("Unable to instantiate AE plugin " + cName, e);
            }
        }
    }

    private static Object instantiatePlugin(Class<?> aClass, Map<Class<?>, Object> injectableMap) throws Exception {

        Constructor<?>[] constructors = aClass.getDeclaredConstructors();

        if (constructors.length == 0) {
            // This is the default no-arg constructor, although it seems pointless to
            // instantiate anything but not take
            // any AE dependencies as parameters
            return aClass.newInstance();
        } else if (constructors.length != 1) {
            throw new IllegalArgumentException("Expected a single constructor, but found: " + constructors.length);
        }

        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);

        Object[] args = findInjectables(constructor, injectableMap);

        return constructor.newInstance(args);
    }

    private static Object[] findInjectables(Constructor<?> constructor, Map<Class<?>, Object> injectableMap) {

        Class<?>[] types = constructor.getParameterTypes();
        Object[] args = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            args[i] = injectableMap.get(types[i]);
            if (args[i] == null) {
                throw new IllegalArgumentException("Constructor has parameter of type " + types[i]
                        + " which is not an injectable type." + " Please see the documentation for @AEPlugin.");
            }
        }

        return args;
    }

    private static Map<Class<?>, Object> mapInjectables(Collection<Object> injectables) {
        ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();

        for (Object injectable : injectables) {
            // Get all super-interfaces that were annotated with @AEInjectable
            Set<Class<?>> injectableIfs = getInjectableInterfaces(injectable.getClass());
            for (Class<?> injectableIf : injectableIfs) {
                builder.put(injectableIf, injectable);
            }
        }

        return builder.build();
    }

    private static Set<Class<?>> getInjectableInterfaces(Class<?> aClass) {
        Set<Class<?>> hierarchy = new HashSet<>();
        getFullHierarchy(aClass, hierarchy);

        return hierarchy.stream().filter(c -> c.getAnnotation(AEInjectable.class) != null).collect(Collectors.toSet());
    }

    // Recursively gather all superclasses and superinterfaces of the given class
    // and put them into the given collection
    private static void getFullHierarchy(Class<?> aClass, Set<Class<?>> classes) {
        classes.add(aClass);
        for (Class<?> anIf : aClass.getInterfaces()) {
            getFullHierarchy(anIf, classes);
        }
        if (aClass.getSuperclass() != null) {
            getFullHierarchy(aClass.getSuperclass(), classes);
        }
    }
}
