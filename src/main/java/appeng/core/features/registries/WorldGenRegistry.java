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

import java.util.HashSet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

import appeng.api.features.IWorldGen;

public final class WorldGenRegistry implements IWorldGen {

    public static final WorldGenRegistry INSTANCE = new WorldGenRegistry();
    private final TypeSet[] types;

    private WorldGenRegistry() {

        this.types = new TypeSet[WorldGenType.values().length];

        for (final WorldGenType type : WorldGenType.values()) {
            this.types[type.ordinal()] = new TypeSet();
        }
    }

    @Override
    public void enableWorldGenForDimension(final WorldGenType type, final ResourceLocation dimensionID) {
        if (type == null) {
            throw new IllegalArgumentException("Bad Type Passed");
        }

        this.types[type.ordinal()].enabledDimensions.add(dimensionID);
    }

    @Override
    public void disableWorldGenForDimension(final WorldGenType type, final ResourceLocation dimensionID) {
        if (type == null) {
            throw new IllegalArgumentException("Bad Type Passed");
        }

        this.types[type.ordinal()].badDimensions.add(dimensionID);
    }

    @Override
    public boolean isWorldGenEnabled(final WorldGenType type, final ServerWorld w) {
        if (type == null) {
            throw new IllegalArgumentException("Bad Type Passed");
        }

        if (w == null) {
            throw new IllegalArgumentException("Bad Provider Passed");
        }

        ResourceLocation id = w.func_234923_W_().func_240901_a_();
        final boolean isBadDimension = this.types[type.ordinal()].badDimensions.contains(id);
        final boolean isGoodDimension = this.types[type.ordinal()].enabledDimensions.contains(id);

        if (isBadDimension) {
            return false;
        }

        if (!isGoodDimension && type == WorldGenType.METEORITES) {
            return false;
        }

        return true;
    }

    private static class TypeSet {

        final HashSet<ResourceLocation> badDimensions = new HashSet<>();
        final HashSet<ResourceLocation> enabledDimensions = new HashSet<>();
    }
}
