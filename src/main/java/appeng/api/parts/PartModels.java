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

package appeng.api.parts;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

/**
 * Allows registration of part models that can then be used in {@link IPart#getStaticModels()}.
 * <p/>
 * The models will automatically be added as dependencies to the model of the cable bus, and registered with
 * {@link ModelLoader#addSpecialModel(net.minecraft.resources.ResourceLocation)}.
 */
@ThreadSafe
public final class PartModels {

    private static final Set<ResourceLocation> models = new HashSet<>();

    private static volatile boolean frozen = false;

    private PartModels() {
    }

    static void freeze() {
        frozen = true;
    }

    static Set<ResourceLocation> getModels() {
        return models;
    }

    /**
     * Allows registration of part models that can then be used in {@link IPart#getStaticModels()}.
     * <p>
     * Models can be registered multiple times without causing issues.
     * <p>
     * This method must be called during the pre-init phase (as part of your plugin's constructor).
     */
    public synchronized static void registerModels(Collection<ResourceLocation> partModels) {
        if (frozen) {
            throw new IllegalStateException("Cannot register models after the pre-initialization phase!");
        }

        models.addAll(partModels);
    }

    /**
     * Convenience overload of {@link #registerModels(Collection)}
     */
    public static void registerModels(ResourceLocation... partModels) {
        registerModels(Arrays.asList(partModels));
    }
}
