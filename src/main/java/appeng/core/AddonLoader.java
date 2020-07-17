/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2020, AlgorithmX2, All rights reserved.
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

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.objectweb.asm.Type;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;

import appeng.api.AEAddon;
import appeng.api.IAEAddon;
import appeng.api.IAppEngApi;

/**
 * Loads AE addons on startup and provides them with an {@link IAppEngApi}
 * instance.
 */
class AddonLoader {

    public static void loadAddons(IAppEngApi api) {
        final Type annotationType = Type.getType(AEAddon.class);
        final Collection<ModFileScanData> allScanData = ModList.get().getAllScanData();

        allScanData.stream().map(ModFileScanData::getAnnotations).flatMap(Set::stream)
                .filter(a -> Objects.equals(a.getAnnotationType(), annotationType)).map(AnnotationData::getMemberName)
                .forEach(className -> {
                    try {
                        final Class<?> clazz = Class.forName(className);
                        final Class<? extends IAEAddon> instanceClass = clazz.asSubclass(IAEAddon.class);
                        final IAEAddon instance = instanceClass.newInstance();

                        instance.onAPIAvailable(api);
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                            | LinkageError e) {
                        AELog.error("Failed to load: %s", className, e);
                        throw new RuntimeException(e);
                    }
                });
    }
}
