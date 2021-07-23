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

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import appeng.core.AppEng;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * IDs for the spatial storage world related dimension objects.
 */
public final class SpatialStorageDimensionIds {

    /**
     * ID of the {@link DimensionType} used for the spatial storage world.
     * <p>
     * This is defined in {@link appeng.mixins.spatial.DimensionTypeMixin}.
     */
    public static final ResourceKey<DimensionType> DIMENSION_TYPE_ID = ResourceKey
            .create(Registry.DIMENSION_TYPE_REGISTRY, AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link ChunkGenerator} used for the spatial storage world.
     */
    public static final ResourceLocation CHUNK_GENERATOR_ID = AppEng.makeId("spatial_storage");

    /**
     * ID of the {@link Biome} used for the spatial storage world.
     */
    public static final ResourceKey<Biome> BIOME_KEY = ResourceKey.create(Registry.BIOME_REGISTRY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link LevelStem} used for the spatial storage dimension.
     * <p>
     * This is defined in {@link appeng.mixins.spatial.DimensionTypeMixin}.
     */
    public static final ResourceKey<LevelStem> DIMENSION_ID = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link Level} that is instantiated from the dimension/dimension type.
     */
    public static final ResourceKey<Level> WORLD_ID = ResourceKey.create(Registry.DIMENSION_REGISTRY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link DimensionSpecialEffects} used for the spatial storage world.
     */
    public static ResourceLocation SKY_PROPERTIES_ID = AppEng.makeId("spatial_storage");

    private SpatialStorageDimensionIds() {
    }

}
