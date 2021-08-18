/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020, AlgorithmX2, All rights reserved.
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
package appeng.worldgen.meteorite;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

import appeng.services.compass.CompassService;
import appeng.worldgen.meteorite.fallout.FalloutMode;

public class MeteoriteStructurePiece extends StructurePiece {

    public static final StructurePieceType TYPE = StructurePieceType.setPieceId(MeteoriteStructurePiece::new,
            "ae2mtrt");

    public static void register() {
        // THIS MUST BE CALLED otherwise the static initializer above will not run,
        // unless level generation is actually invoked, which means that chunks may
        // be loaded without this being registered as a structure piece!
    }

    private final PlacedMeteoriteSettings settings;

    protected MeteoriteStructurePiece(BlockPos center, float coreRadius, CraterType craterType, FalloutMode fallout,
            boolean pureCrater, boolean craterLake) {
        super(TYPE, 0, createBoundingBox(center));
        this.settings = new PlacedMeteoriteSettings(center, coreRadius, craterType, fallout, pureCrater, craterLake);
    }

    private static BoundingBox createBoundingBox(BlockPos origin) {
        // Assume a normal max height of 128 blocks for most biomes,
        // meteors spawned at about y64 are 9x9 chunks large at most.
        int range = 4 * 16;

        ChunkPos chunkPos = new ChunkPos(origin);

        return new BoundingBox(chunkPos.getMinBlockX() - range, origin.getY(),
                chunkPos.getMinBlockZ() - range, chunkPos.getMaxBlockX() + range, origin.getY(),
                chunkPos.getMaxBlockZ() + range);
    }

    public MeteoriteStructurePiece(ServerLevel level, CompoundTag tag) {
        super(TYPE, tag);

        BlockPos center = BlockPos.of(tag.getLong(Constants.TAG_POS));
        float coreRadius = tag.getFloat(Constants.TAG_RADIUS);
        CraterType craterType = CraterType.values()[tag.getByte(Constants.TAG_CRATER)];
        FalloutMode fallout = FalloutMode.values()[tag.getByte(Constants.TAG_FALLOUT)];
        boolean pureCrater = tag.getBoolean(Constants.TAG_PURE);
        boolean craterLake = tag.getBoolean(Constants.TAG_LAKE);

        this.settings = new PlacedMeteoriteSettings(center, coreRadius, craterType, fallout, pureCrater, craterLake);
    }

    public boolean isFinalized() {
        return settings.getCraterType() != null;
    }

    public PlacedMeteoriteSettings getSettings() {
        return settings;
    }

    @Override
    protected void addAdditionalSaveData(ServerLevel level, CompoundTag tag) {
        tag.putFloat(Constants.TAG_RADIUS, settings.getMeteoriteRadius());
        tag.putLong(Constants.TAG_POS, settings.getPos().asLong());
        tag.putByte(Constants.TAG_CRATER, (byte) settings.getCraterType().ordinal());
        tag.putByte(Constants.TAG_FALLOUT, (byte) settings.getFallout().ordinal());
        tag.putBoolean(Constants.TAG_PURE, settings.isPureCrater());
        tag.putBoolean(Constants.TAG_LAKE, settings.isCraterLake());
    }

    @Override
    public boolean postProcess(WorldGenLevel level, StructureFeatureManager p_230383_2_,
            ChunkGenerator chunkGeneratorIn,
            Random rand, BoundingBox bounds, ChunkPos chunkPos, BlockPos p_230383_7_) {
        MeteoritePlacer placer = new MeteoritePlacer(level, settings, bounds, rand);
        placer.place();

        CompassService.updateArea(level.getLevel(), level.getChunk(chunkPos.x, chunkPos.z));
        return true;
    }
}
