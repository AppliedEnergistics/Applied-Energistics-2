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

package appeng.mixins.spatial;

import java.util.OptionalLong;

import com.mojang.serialization.Lifecycle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * Adds the storage cell level dimension type as a built-in dimension type. This can be registered as a JSON file as
 * well, but doing so will trigger an experimental feature warning when the level is being loaded.
 */
@Mixin(value = DimensionType.class)
public class DimensionTypeMixin {

    @Inject(method = "registerBuiltin", at = @At("TAIL"))
    private static void addRegistryDefaults(RegistryAccess registryAccess, CallbackInfoReturnable<?> cir) {
        var dimensionType = DimensionType.create(
                OptionalLong.of(12000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                false, // createDragonFight
                false, // piglinSafe
                false, // bedWorks
                false, // respawnAnchorWorks
                false, // hasRaids
                SpatialStorageChunkGenerator.MIN_Y, // minY
                SpatialStorageChunkGenerator.HEIGHT, // height
                SpatialStorageChunkGenerator.HEIGHT, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD.getName(), // infiniburn
                SpatialStorageDimensionIds.SKY_PROPERTIES_ID, // effectsLocation
                1.0f // ambientLight
        );
        var dimensionTypes = registryAccess.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry.register(dimensionTypes, SpatialStorageDimensionIds.DIMENSION_TYPE_ID.location(), dimensionType);
    }

    /**
     * Insert our custom dimension into the initial registry. <em>This is what will ultimately lead to the creation of a
     * new World.</em>
     */
    @Inject(method = "defaultDimensions", at = @At("RETURN"))
    private static void buildDimensionRegistry(RegistryAccess registryAccess, long seed,
            CallbackInfoReturnable<MappedRegistry<LevelStem>> cir) {
        MappedRegistry<LevelStem> simpleregistry = cir.getReturnValue();

        var dimensionTypes = registryAccess.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        var biomes = registryAccess.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);

        simpleregistry.register(SpatialStorageDimensionIds.DIMENSION_ID,
                new LevelStem(() -> dimensionTypes.getOrThrow(SpatialStorageDimensionIds.DIMENSION_TYPE_ID),
                        new SpatialStorageChunkGenerator(biomes)),
                Lifecycle.stable());

    }

}
