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

package appeng.worldgen;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.server.ServerWorld;

import appeng.api.features.AEFeature;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.features.registries.WorldGenRegistry;
import appeng.core.worlddata.WorldData;
import appeng.util.Platform;

public final class MeteoriteWorldGen extends Feature<NoFeatureConfig> {

    public static final MeteoriteWorldGen INSTANCE = new MeteoriteWorldGen();

    private MeteoriteWorldGen() {
        super(NoFeatureConfig::deserialize);
        setRegistryName(AppEng.MOD_ID, "meteorite");
    }

    private final MeteoriteSpawner spawner = new MeteoriteSpawner();

    @ParametersAreNonnullByDefault
    @Override
    public boolean place(IWorld w, ChunkGenerator<? extends GenerationSettings> generator, Random r, BlockPos pos,
            NoFeatureConfig config) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        if (!WorldGenRegistry.INSTANCE.isWorldGenEnabled(WorldGenType.METEORITES, w.getWorld())) {
            return false;
        }

        double minSqDist = Double.MAX_VALUE;

        MeteoriteSpawnData spawnData = MeteoriteSpawnData.get(w);

        // near by meteorites!
        for (final PlacedMeteoriteSettings data : spawnData.getNearByMeteorites(chunkPos.x, chunkPos.z)) {
            minSqDist = Math.min(minSqDist, getSqDistance(data, pos));
        }

        final boolean isCluster = (minSqDist < 30 * 30)
                && Platform.getRandomFloat() < AEConfig.instance().getMeteoriteClusterChance();

        if (minSqDist > AEConfig.instance().getMinMeteoriteDistanceSq() || isCluster) {
            final int x = r.nextInt(16) + (chunkPos.x << 4);
            final int y = AEConfig.instance().getMeteoriteMaximumSpawnHeight() + r.nextInt(20);
            final int z = r.nextInt(16) + (chunkPos.z << 4);

            PlacedMeteoriteSettings settings = spawner.trySpawnMeteoriteAtSuitableHeight(w, x, y, z);
            if (settings != null) {
                // Double check that no other chunk generated a meteorite within range while we
                // were working
                int checkRange = isCluster ? (30 * 30) : AEConfig.instance().getMinMeteoriteDistanceSq();
                if (spawnData.tryAddSpawnedMeteorite(settings, checkRange)) {
                    MeteoritePlacer placer = new MeteoritePlacer(w, settings);
                    placer.place();
                    WorldData.instance().compassData().service().updateArea(w, chunkPos.x, chunkPos.z);
                }
            }
        }

        spawnData.setGenerated(chunkPos.x, chunkPos.z);
        return true;
    }

    private static double getSqDistance(PlacedMeteoriteSettings placed, BlockPos pos) {
        final int chunkX = placed.getPos().getX() - pos.getX();
        final int chunkZ = placed.getPos().getZ() - pos.getZ();
        return chunkX * chunkX + chunkZ * chunkZ;
    }

}
