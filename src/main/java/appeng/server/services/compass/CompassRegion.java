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

package appeng.server.services.compass;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import appeng.core.AELog;
import appeng.core.worlddata.AESavedData;

/**
 * A compass region stores information about the occurrence of skystone blocks in a region of 1024x1024 chunks.
 */
final class CompassRegion extends AESavedData {
    private static final SavedData.Factory<CompassRegion> FACTORY = new Factory<>(
            CompassRegion::new,
            CompassRegion::load,
            null);

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
        return "ae2_compass_v2_" + regionX + "_" + regionZ;
    }

    /**
     * Legacy regions were selected using truncating division, which put chunks with negative coordinates into the wrong
     * region and made chunks on both sides of the origin overlap in region 0.
     */
    private static String getLegacyRegionSaveName(int regionX, int regionZ) {
        return "ae2_compass_" + regionX + "_" + regionZ;
    }

    /**
     * Retrieve the compass region that serves the given chunk position.
     */
    public static CompassRegion get(ServerLevel level, ChunkPos chunkPos) {
        Objects.requireNonNull(chunkPos, "chunkPos");

        return get(level, chunkPos.x, chunkPos.z);
    }

    /**
     * Retrieve the compass region that serves the given chunk position.
     */
    public static CompassRegion get(ServerLevel level, int cx, int cz) {
        Objects.requireNonNull(level, "level");

        var regionX = Math.floorDiv(cx, CHUNKS_PER_REGION);
        var regionZ = Math.floorDiv(cz, CHUNKS_PER_REGION);

        var storage = level.getDataStorage();
        var saveName = getRegionSaveName(regionX, regionZ);
        var region = storage.get(FACTORY, saveName);
        if (region == null) {
            region = migrateLegacyRegions(storage, regionX, regionZ);
            storage.set(saveName, region);
        }
        return region;
    }

    static boolean hasCompassTarget(ServerLevel level, int cx, int cz) {
        return get(level, cx, cz).hasCompassTarget(cx, cz);
    }

    /**
     * Creates a new region and populates it from the legacy regions that held data for chunks in this region.
     * <p>
     * Legacy region 0 on each axis held the data of chunks on both sides of the origin, so we cannot tell which side
     * the data belonged to. It is copied to both sides, which preserves the previous behavior for that data.
     */
    private static CompassRegion migrateLegacyRegions(DimensionDataStorage storage, int regionX, int regionZ) {
        var result = new CompassRegion();

        for (var legacyRegionX : getLegacySourceRegions(regionX)) {
            for (var legacyRegionZ : getLegacySourceRegions(regionZ)) {
                var legacyRegion = storage.get(FACTORY, getLegacyRegionSaveName(legacyRegionX, legacyRegionZ));
                if (legacyRegion == null) {
                    continue;
                }

                // The index within a region is the same for legacy and current regions
                for (var entry : legacyRegion.sections.entrySet()) {
                    var bits = entry.getValue();
                    for (var i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                        var x = i % CHUNKS_PER_REGION;
                        var z = i / CHUNKS_PER_REGION;
                        if (getLegacySourceRegion(regionX, x) == legacyRegionX
                                && getLegacySourceRegion(regionZ, z) == legacyRegionZ) {
                            result.setHasCompassTarget(x, z, entry.getKey(), true);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * The legacy regions on one axis that may contain data for the given region.
     */
    private static int[] getLegacySourceRegions(int region) {
        return region >= 0 ? new int[] { region } : new int[] { region, region + 1 };
    }

    /**
     * The legacy region on one axis that held the data for the chunk at the given offset within the given region.
     */
    private static int getLegacySourceRegion(int region, int offset) {
        // Chunk coordinate is region * CHUNKS_PER_REGION + offset, which legacy regions divided with truncation
        return region >= 0 || offset == 0 ? region : region + 1;
    }

    public static CompassRegion load(CompoundTag nbt, HolderLookup.Provider registries) {
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
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        for (var entry : sections.entrySet()) {
            var key = "section" + entry.getKey();
            if (entry.getValue().isEmpty()) {
                continue;
            }
            compound.putByteArray(key, entry.getValue().toByteArray());
        }
        return compound;
    }

    boolean hasCompassTarget(int cx, int cz) {
        var bitmapIndex = getBitmapIndex(cx, cz);
        for (BitSet bitmap : sections.values()) {
            if (bitmap.get(bitmapIndex)) {
                return true;
            }
        }
        return false;
    }

    boolean hasCompassTarget(int cx, int cz, int sectionIndex) {
        var bitmapIndex = getBitmapIndex(cx, cz);
        var section = sections.get(sectionIndex);
        if (section != null) {
            return section.get(bitmapIndex);
        }
        return false;
    }

    void setHasCompassTarget(int cx, int cz, int sectionIndex, boolean hasTarget) {
        var bitmapIndex = getBitmapIndex(cx, cz);
        var section = sections.get(sectionIndex);
        if (section == null) {
            if (hasTarget) {
                section = new BitSet(BITMAP_LENGTH);
                section.set(bitmapIndex);
                sections.put(sectionIndex, section);
                setDirty();
            }
        } else {
            if (section.get(bitmapIndex) != hasTarget) {
                setDirty();
            }
            // There already was data on this y-section in this region
            if (!hasTarget) {
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
