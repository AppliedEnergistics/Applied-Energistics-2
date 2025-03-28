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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A compass region stores information about the occurrence of skystone blocks in a region of 1024x1024 chunks.
 */
final class CompassRegion extends SavedData {
    // Helper used to record it in the codec
    private record Section(int index, ByteBuffer bits) {
    }

    private static final Codec<Section> SECTION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("index").forGetter(Section::index),
            Codec.BYTE_BUFFER.fieldOf("index").forGetter(Section::bits)
    ).apply(builder, Section::new));

    private static final Codec<CompassRegion> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SECTION_CODEC.listOf().fieldOf("sections").forGetter(CompassRegion::sections)
    ).apply(builder, CompassRegion::new));

    private List<Section> sections() {
        return sections.entrySet().stream().map(
                entry -> new Section(entry.getKey(), ByteBuffer.wrap(entry.getValue().toByteArray()))
        ).toList();
    }

    private static final BiFunction<Integer, Integer, SavedDataType<CompassRegion>> TYPE = Util.memoize((regionX, regionZ) -> new SavedDataType<>(
            "ae2_compass_" + regionX + "_" + regionZ,
            CompassRegion::new,
            CODEC
    ));

    /**
     * The number of chunks that get saved in a region on each axis.
     */
    private static final int CHUNKS_PER_REGION = 1024;

    private static final int BITMAP_LENGTH = CHUNKS_PER_REGION * CHUNKS_PER_REGION;

    // Key is the section index, see ChunkAccess.getSections()
    private final Map<Integer, BitSet> sections = new HashMap<>();

    private CompassRegion() {
    }

    private CompassRegion(List<Section> sections) {
        for (var section : sections) {
            this.sections.put(section.index(), BitSet.valueOf(section.bits()));
        }
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
                TYPE.apply(regionX, regionZ)
        );
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
