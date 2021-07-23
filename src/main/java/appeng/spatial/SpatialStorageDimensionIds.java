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

package appeng.spatial;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import appeng.core.AppEng;

/**
 * IDs for the spatial storage world related dimension objects.
 */
public final class SpatialStorageDimensionIds {

    /**
     * ID of the {@link DimensionType} used for the spatial storage world.
     * <p>
     * This is defined in {@link appeng.mixins.spatial.DimensionTypeMixin}.
     */
    public static final RegistryKey<DimensionType> DIMENSION_TYPE_ID = RegistryKey
            .create(Registry.DIMENSION_TYPE_REGISTRY, AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link net.minecraft.world.gen.ChunkGenerator} used for the spatial storage world.
     */
    public static final ResourceLocation CHUNK_GENERATOR_ID = AppEng.makeId("spatial_storage");

    /**
     * ID of the {@link net.minecraft.world.biome.Biome} used for the spatial storage world.
     */
    public static final RegistryKey<Biome> BIOME_KEY = RegistryKey.create(Registry.BIOME_REGISTRY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link Dimension} used for the spatial storage dimension.
     * <p>
     * This is defined in {@link appeng.mixins.spatial.DimensionTypeMixin}.
     */
    public static final RegistryKey<Dimension> DIMENSION_ID = RegistryKey.create(Registry.LEVEL_STEM_REGISTRY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link World} that is instantiated from the dimension/dimension type.
     */
    public static final RegistryKey<World> WORLD_ID = RegistryKey.create(Registry.DIMENSION_REGISTRY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link net.minecraft.client.world.DimensionRenderInfo} used for the spatial storage world.
     */
    public static ResourceLocation SKY_PROPERTIES_ID = AppEng.makeId("spatial_storage");

    private SpatialStorageDimensionIds() {
    }

}
