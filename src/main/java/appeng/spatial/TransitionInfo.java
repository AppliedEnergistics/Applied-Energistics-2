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

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Defines the source world and area of a transition into the spatial storage plot.
 */
public final class TransitionInfo {

    public static final String TAG_WORLD_ID = "world_id";
    public static final String TAG_MIN = "min";
    public static final String TAG_MAX = "max";
    public static final String TAG_TIMESTAMP = "timestamp";

    private final ResourceLocation worldId;

    private final BlockPos min;

    private final BlockPos max;

    private final Instant timestamp;

    public TransitionInfo(ResourceLocation worldId, BlockPos min, BlockPos max, Instant timestamp) {
        this.worldId = worldId;
        this.min = min.toImmutable();
        this.max = max.toImmutable();
        this.timestamp = timestamp;
    }

    public ResourceLocation getWorldId() {
        return worldId;
    }

    public BlockPos getMin() {
        return min;
    }

    public BlockPos getMax() {
        return max;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public CompoundNBT toTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(TAG_WORLD_ID, worldId.toString());
        tag.put(TAG_MIN, NBTUtil.writeBlockPos(min));
        tag.put(TAG_MAX, NBTUtil.writeBlockPos(max));
        tag.putLong(TAG_TIMESTAMP, timestamp.toEpochMilli());
        return tag;
    }

    public static TransitionInfo fromTag(CompoundNBT tag) {
        ResourceLocation worldId = new ResourceLocation(tag.getString(TAG_WORLD_ID));
        BlockPos min = NBTUtil.readBlockPos(tag.getCompound(TAG_MIN));
        BlockPos max = NBTUtil.readBlockPos(tag.getCompound(TAG_MAX));
        Instant timestamp = Instant.ofEpochMilli(tag.getLong(TAG_TIMESTAMP));
        return new TransitionInfo(worldId, min, max, timestamp);
    }

}
