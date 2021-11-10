/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.services.compass;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import appeng.core.AELog;

/**
 * A compass region stores information about the occurrence of skystone blocks in a region of 1024x1024 chunks.
 */
final class CompassRegion extends SavedData {

    /**
     * The number of chunks that get saved in a region on each axis.
     */
    private static final int CHUNKS_PER_REGION = 1024;

    private static final int BITMAP_LENGTH = CHUNKS_PER_REGION * CHUNKS_PER_REGION;

    // Key is the section index, see ChunkAccess.getSections()
    private final Map<Integer, BitSet> sections = new HashMap<>();

    /**
     * Gets the name of the save data for a region that has the given coordinates.
     */
    private static String getRegionSaveName(int regionX, int regionZ) {
        return "ae2_compass_" + regionX + "_" + regionZ;
    }

    /**
     * Retrieve the compass region that serves the given chunk position.
     */
    public static CompassRegion get(ServerLevel level, ChunkPos chunkPos) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(chunkPos, "chunkPos");

        var regionX = chunkPos.x / CHUNKS_PER_REGION;
        var regionZ = chunkPos.z / CHUNKS_PER_REGION;

        return level.getDataStorage().computeIfAbsent(
                CompassRegion::load,
                CompassRegion::new,
                getRegionSaveName(regionX, regionZ));
    }

    public static CompassRegion load(CompoundTag nbt) {
        var result = new CompassRegion();
        for (String key : nbt.getAllKeys()) {
            if (key.startsWith("section")) {
                try {
                    var sectionIndex = Integer.parseInt(key.substring("section".length()));
                    result.sections.put(sectionIndex, BitSet.valueOf(nbt.getByteArray(key)));
                } catch (NumberFormatException e) {
                    AELog.warn("Compass region contains invalid NBT tag %s", key);
                }
            } else {
                AELog.warn("Compass region contains unknown NBT tag %s", key);
            }
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        for (var entry : sections.entrySet()) {
            var key = "section" + entry.getKey();
            if (entry.getValue().isEmpty()) {
                continue;
            }
            compound.putByteArray(key, entry.getValue().toByteArray());
        }
        return compound;
    }

    boolean hasSkyStone(int cx, int cz) {
        var bitmapIndex = getBitmapIndex(cx, cz);
        for (BitSet bitmap : sections.values()) {
            if (bitmap.get(bitmapIndex)) {
                return true;
            }
        }
        return false;
    }

    boolean hasSkyStone(int cx, int cz, int sectionIndex) {
        var bitmapIndex = getBitmapIndex(cx, cz);
        var section = sections.get(sectionIndex);
        if (section != null) {
            return section.get(bitmapIndex);
        }
        return false;
    }

    void setHasSkyStone(int cx, int cz, int sectionIndex, boolean hasSkyStone) {
        var bitmapIndex = getBitmapIndex(cx, cz);
        var section = sections.get(sectionIndex);
        if (section == null) {
            if (hasSkyStone) {
                section = new BitSet(BITMAP_LENGTH);
                section.set(bitmapIndex);
                sections.put(sectionIndex, section);
                setDirty();
            }
        } else {
            if (section.get(bitmapIndex) != hasSkyStone) {
                setDirty();
            }
            // There already was data on this y-section in this region
            if (!hasSkyStone) {
                section.clear(bitmapIndex);
                if (section.isEmpty()) {
                    sections.remove(sectionIndex);
                }
                setDirty();
            } else {
                section.set(bitmapIndex);
            }
        }
    }

    private static int getBitmapIndex(int cx, int cz) {
        cx &= CHUNKS_PER_REGION - 1;
        cz &= CHUNKS_PER_REGION - 1;
        return cx + cz * CHUNKS_PER_REGION;
    }

}
