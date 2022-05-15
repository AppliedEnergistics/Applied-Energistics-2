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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.dimension.DimensionType;

import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * Adds the storage cell level dimension type as a built-in dimension type. This can be registered as a JSON file as
 * well, but doing so will trigger an experimental feature warning when the level is being loaded.
 */
@Mixin(value = DimensionTypes.class)
public class DimensionTypesMixin {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(Registry<DimensionType> registry, CallbackInfoReturnable<?> cir) {
        var dimensionType = new DimensionType(
                OptionalLong.of(12000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                false, // piglinSafe
                false, // bedWorks
                false, // respawnAnchorWorks
                false, // hasRaids
                SpatialStorageChunkGenerator.MIN_Y, // minY
                SpatialStorageChunkGenerator.HEIGHT, // height
                SpatialStorageChunkGenerator.HEIGHT, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                SpatialStorageDimensionIds.SKY_PROPERTIES_ID, // effectsLocation
                1.0f // ambientLight
        );
        Registry.register(registry, SpatialStorageDimensionIds.DIMENSION_TYPE_ID.location(), dimensionType);
    }
}
