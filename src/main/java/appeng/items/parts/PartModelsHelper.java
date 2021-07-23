/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.items.parts;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

import appeng.api.parts.IPartModel;
import appeng.core.AELog;

/**
 * Helps with the reflection magic needed to gather all models for AE2 cable bus parts.
 */
public class PartModelsHelper {

    public static List<ResourceLocation> createModels(Class<?> clazz) {
        List<ResourceLocation> locations = new ArrayList<>();

        // Check all static fields for used models
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(PartModels.class) == null) {
                continue;
            }

            if (!Modifier.isStatic(field.getModifiers())) {
                AELog.error("The @PartModels annotation can only be used on static fields or methods. Was seen on: "
                        + field);
                continue;
            }

            Object value;
            try {
                field.setAccessible(true);
                value = field.get(null);
            } catch (IllegalAccessException e) {
                AELog.error(e, "Cannot access field annotated with @PartModels: " + field);
                continue;
            }

            convertAndAddLocation(field, value, locations);
        }

        // Check all static methods for the annotation
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(PartModels.class) == null) {
                continue;
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                AELog.error("The @PartModels annotation can only be used on static fields or methods. Was seen on: "
                        + method);
                continue;
            }

            // Check for parameter count
            if (method.getParameters().length != 0) {
                AELog.error(
                        "The @PartModels annotation can only be used on static methods without parameters. Was seen on: "
                                + method);
                continue;
            }

            // Make sure we can handle the return type
            Class<?> returnType = method.getReturnType();
            if (!ResourceLocation.class.isAssignableFrom(returnType)
                    && !Collection.class.isAssignableFrom(returnType)) {
                AELog.error(
                        "The @PartModels annotation can only be used on static methods that return a ResourceLocation or Collection of "
                                + "ResourceLocations. Was seen on: " + method);
                continue;
            }

            Object value;
            try {
                method.setAccessible(true);
                value = method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                AELog.error(e, "Failed to invoke the @PartModels annotated method " + method);
                continue;
            }

            convertAndAddLocation(method, value, locations);
        }

        if (clazz.getSuperclass() != null) {
            locations.addAll(createModels(clazz.getSuperclass()));
        }

        return locations;
    }

    private static void convertAndAddLocation(Object source, Object value, List<ResourceLocation> locations) {
        if (value == null) {
            return;
        }

        if (value instanceof ResourceLocation) {
            locations.add((net.minecraft.resources.ResourceLocation) value);
        } else if (value instanceof IPartModel) {
            locations.addAll(((IPartModel) value).getModels());
        } else if (value instanceof Collection) {
            // Check that each object is an IPartModel
            Collection<?> values = (Collection<?>) value;
            for (Object candidate : values) {
                if (!(candidate instanceof IPartModel)) {
                    AELog.error("List of locations obtained from {} contains a non resource location: {}", source,
                            candidate);
                    continue;
                }

                locations.addAll(((IPartModel) candidate).getModels());
            }
        }
    }

}
