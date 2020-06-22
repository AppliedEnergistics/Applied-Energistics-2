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

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.biome.Biome.TempCategory;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

import appeng.core.worlddata.WorldData;
import appeng.worldgen.meteorite.fallout.FalloutMode;

public class MeteoriteStructurePiece extends StructurePiece {

    public static final IStructurePieceType TYPE = IStructurePieceType.register(MeteoriteStructurePiece::new,
            "AE2MTRT");

    private PlacedMeteoriteSettings settings;

    protected MeteoriteStructurePiece(BlockPos center, float coreRadius) {
        super(TYPE, 0);
        this.settings = new PlacedMeteoriteSettings(center, coreRadius, null, null, false);

        // Since we don't know yet if the meteorite will be underground or not,
        // we have to assume maximum size
        int range = (int) Math.ceil((coreRadius * 2 + 5) * 1.25f);

        this.boundingBox = new MutableBoundingBox(center.getX() - range, center.getY(), center.getZ() - range,
                center.getX() + range, center.getY(), center.getZ() + range);
    }

    public MeteoriteStructurePiece(TemplateManager templateManager, CompoundNBT tag) {
        super(TYPE, tag);

        // Mandatory fields
        BlockPos center = BlockPos.fromLong(tag.getLong(Constants.TAG_POS));
        float coreRadius = tag.getFloat(Constants.TAG_RADIUS);
        CraterType craterType = null;
        FalloutMode fallout = null;
        boolean pureCrater = false;

        if (tag.contains(Constants.TAG_CRATER)) {
            craterType = CraterType.values()[tag.getByte(Constants.TAG_CRATER)];
            fallout = FalloutMode.values()[tag.getByte(Constants.TAG_FALLOUT)];
            pureCrater = tag.getBoolean(Constants.TAG_PURE);
        }

        this.settings = new PlacedMeteoriteSettings(center, coreRadius, craterType, fallout, pureCrater);
    }

    public boolean isFinalized() {
        return settings.getCraterType() != null;
    }

    public PlacedMeteoriteSettings getSettings() {
        return settings;
    }

    @Override
    protected void readAdditional(CompoundNBT tag) {
        tag.putFloat(Constants.TAG_RADIUS, settings.getMeteoriteRadius());
        tag.putLong(Constants.TAG_POS, settings.getPos().toLong());
        if (isFinalized()) {
            tag.putByte(Constants.TAG_CRATER, (byte) settings.getCraterType().ordinal());
            tag.putByte(Constants.TAG_FALLOUT, (byte) settings.getFallout().ordinal());
        }
    }

    @Override
    public boolean create(IWorld world, ChunkGenerator<?> chunkGeneratorIn, Random rand, MutableBoundingBox bounds,
            ChunkPos chunkPos) {

        // The parent structure synchronizes on the list of components, so this
        // placement should be
        // mutually exclusive with any other chunk the structure is placed in. This
        // allows us to
        // finalize some of the placement parameters now that we have access to an
        // actual world object
        if (!isFinalized()) {
            MeteoriteSpawner spawner = new MeteoriteSpawner();
            BlockPos center = settings.getPos();
            float coreRadius = settings.getMeteoriteRadius();
            CraterType craterType = determineCraterType(world, center, rand);
            boolean pureCrater = rand.nextFloat() > .5f;
            settings = spawner.trySpawnMeteoriteAtSuitableHeight(world, center, coreRadius, craterType, pureCrater,
                    true);
            if (settings == null) {
                return false;
            }
        }

        MeteoritePlacer placer = new MeteoritePlacer(world, settings, bounds);
        placer.place();

        WorldData.instance().compassData().service().updateArea(world, chunkPos); // FIXME: We know the y-range here...
        return true;
    }

    private CraterType determineCraterType(IWorld world, BlockPos center, Random rand) {
        final Biome biome = world.getBiome(center);
        final TempCategory temp = biome.getTempCategory();
        final Category category = biome.getCategory();

        // No craters in oceans
        if (category == Category.OCEAN) {
            return CraterType.NONE;
        }

        // 50% chance for a special meteor
        final boolean specialMeteor = rand.nextFloat() > .5f;

        // Just a normal one
        if (!specialMeteor) {
            return CraterType.NORMAL;
        }

        // Warm biomes, higher chance for lava
        if (temp == TempCategory.WARM) {

            // 50% chance to actually spawn as lava
            final boolean lava = rand.nextFloat() > .5f;

            switch (biome.getPrecipitation()) {
                // No rainfall, only lava
                case NONE:
                    return lava ? CraterType.LAVA : CraterType.NORMAL;

                // 25% chance to convert a lava to obsidian
                case RAIN:
                    final boolean obsidian = rand.nextFloat() > .75f;
                    final CraterType alternativObsidian = obsidian ? CraterType.OBSIDIAN : CraterType.LAVA;
                    return lava ? alternativObsidian : CraterType.NORMAL;

                // Nothing for now.
                default:
                    break;
            }
        }

        // Temperate biomes. Water or maybe lava
        if (temp == TempCategory.MEDIUM) {
            // 75% chance to actually spawn with a crater lake
            final boolean lake = rand.nextFloat() > .25f;
            // 20% to spawn with lava
            final boolean lava = rand.nextFloat() > .8f;

            switch (biome.getPrecipitation()) {
                // No rainfall, water how?
                case NONE:
                    return lava ? CraterType.LAVA : CraterType.NORMAL;
                // Rainfall, can also turn lava to obsidian
                case RAIN:
                    final boolean obsidian = rand.nextFloat() > .75f;
                    final CraterType alternativObsidian = obsidian ? CraterType.OBSIDIAN : CraterType.LAVA;
                    final CraterType craterLake = lake ? CraterType.WATER : CraterType.NORMAL;
                    return lava ? alternativObsidian : craterLake;
                // No lava, but snow
                case SNOW:
                    final boolean snow = rand.nextFloat() > .75f;
                    final CraterType water = lake ? CraterType.WATER : CraterType.NORMAL;
                    return snow ? CraterType.SNOW : water;
            }

        }

        // Cold biomes, Snow or Ice, maybe water and very rarely lava.
        if (temp == TempCategory.COLD) {
            // 75% chance to actually spawn with a crater lake
            final boolean lake = rand.nextFloat() > .25f;
            // 5% to spawn with lava
            final boolean lava = rand.nextFloat() > .95f;
            // 75% chance to freeze
            final boolean frozen = rand.nextFloat() > .25f;

            switch (biome.getPrecipitation()) {
                // No rainfall, water how?
                case NONE:
                    return lava ? CraterType.LAVA : CraterType.NORMAL;
                case RAIN:
                    final CraterType frozenLake = frozen ? CraterType.ICE : CraterType.WATER;
                    final CraterType craterLake = lake ? frozenLake : CraterType.NORMAL;
                    return lava ? CraterType.LAVA : craterLake;
                case SNOW:
                    final CraterType snowCovered = lake ? CraterType.SNOW : CraterType.NORMAL;
                    return lava ? CraterType.LAVA : snowCovered;
            }

        }

        return CraterType.NORMAL;
    }

}
