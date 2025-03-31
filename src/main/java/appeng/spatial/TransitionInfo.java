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

import java.time.Instant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/**
 * Defines the source level and area of a transition into the spatial storage plot.
 */
public record TransitionInfo(ResourceLocation worldId, BlockPos min, BlockPos max, Instant timestamp) {

    public static final Codec<TransitionInfo> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("world_id").forGetter(TransitionInfo::worldId),
            BlockPos.CODEC.fieldOf("min").forGetter(TransitionInfo::min),
            BlockPos.CODEC.fieldOf("max").forGetter(TransitionInfo::max),
            ExtraCodecs.INSTANT_ISO8601.fieldOf("timestamp").forGetter(TransitionInfo::timestamp))
            .apply(builder, TransitionInfo::new));

    public TransitionInfo(ResourceLocation worldId, BlockPos min, BlockPos max, Instant timestamp) {
        this.worldId = worldId;
        this.min = min.immutable();
        this.max = max.immutable();
        this.timestamp = timestamp;
    }
}
