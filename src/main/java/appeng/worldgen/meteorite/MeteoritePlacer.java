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

package appeng.worldgen.meteorite;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.worldgen.meteorite.fallout.Fallout;
import appeng.worldgen.meteorite.fallout.FalloutCopy;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import appeng.worldgen.meteorite.fallout.FalloutSand;
import appeng.worldgen.meteorite.fallout.FalloutSnow;

public final class MeteoritePlacer {
    public static void place(LevelAccessor level, PlacedMeteoriteSettings settings, BoundingBox boundingBox,
            RandomSource random) {
        var placer = new MeteoritePlacer(level, settings, boundingBox, random);
        placer.place();
    }

    private static final ResourceLocation METEORITE_CHEST_LOOTTABLE = AppEng.makeId("chests/meteorite");
    private final BlockDefinition<?> skyChestDefinition;
    private final BlockState skyStone;
    private final List<BlockState> quartzBlocks;
    private final BlockState fluixBlock;
    private final MeteoriteBlockPutter putter = new MeteoriteBlockPutter();
    private final LevelAccessor level;
    private final RandomSource random;
    private final Fallout type;
    private final BlockPos pos;
    private final int x;
    private final int y;
    private final int z;
    private final double meteoriteSize;
    private final double squaredMeteoriteSize;
    private final double crater;
    private final boolean placeCrater;
    private final CraterType craterType;
    private final boolean pureCrater;
    private final boolean craterLake;
    private final CrystalType crystalType;
    private final BoundingBox boundingBox;

    private MeteoritePlacer(LevelAccessor level, PlacedMeteoriteSettings settings, BoundingBox boundingBox,
            RandomSource random) {
        this.boundingBox = boundingBox;
        this.level = level;
        this.random = random;
        this.pos = settings.getPos();
        this.x = settings.getPos().getX();
        this.y = settings.getPos().getY();
        this.z = settings.getPos().getZ();
        this.meteoriteSize = settings.getMeteoriteRadius();
        this.placeCrater = settings.shouldPlaceCrater();
        this.craterType = settings.getCraterType();
        this.pureCrater = settings.isPureCrater();
        this.craterLake = settings.isCraterLake();
        this.crystalType = settings.getCrystalType();
        this.squaredMeteoriteSize = this.meteoriteSize * this.meteoriteSize;

        double realCrater = this.meteoriteSize * 2 + 5;
        this.crater = realCrater * realCrater;

        this.skyChestDefinition = AEBlocks.SKY_STONE_CHEST;
        this.fluixBlock = AEBlocks.FLUIX_BLOCK.block().defaultBlockState();
        this.quartzBlocks = Stream.of(
                AEBlocks.QUARTZ_BLOCK,
                AEBlocks.DAMAGED_BUDDING_QUARTZ,
                AEBlocks.CHIPPED_BUDDING_QUARTZ,
                AEBlocks.FLAWED_BUDDING_QUARTZ,
                AEBlocks.FLAWLESS_BUDDING_QUARTZ).map(def -> def.block().defaultBlockState()).toList();
        this.skyStone = AEBlocks.SKY_STONE_BLOCK.block().defaultBlockState();

        this.type = getFallout(level, settings.getPos(), settings.getFallout());
    }

    public void place() {
        // creator
        if (placeCrater) {
            this.placeCrater();
        }

        this.placeMeteorite();

        // collapse blocks...
        if (placeCrater) {
            this.decay();
        }
        if (craterLake) {
            this.placeCraterLake();
        }
    }

    private int minX(int x) {
        if (x < boundingBox.minX()) {
            return boundingBox.minX();
        } else if (x > boundingBox.maxX()) {
            return boundingBox.maxX();
        }
        return x;
    }

    private int minZ(int x) {
        if (x < boundingBox.minZ()) {
            return boundingBox.minZ();
        } else if (x > boundingBox.maxZ()) {
            return boundingBox.maxZ();
        }
        return x;
    }

    private int maxX(int x) {
        if (x < boundingBox.minX()) {
            return boundingBox.minX();
        } else if (x > boundingBox.maxX()) {
            return boundingBox.maxX();
        }
        return x;
    }

    private int maxZ(int x) {
        if (x < boundingBox.minZ()) {
            return boundingBox.minZ();
        } else if (x > boundingBox.maxZ()) {
            return boundingBox.maxZ();
        }
        return x;
    }

    private void placeCrater() {
        final int maxY = 255;
        MutableBlockPos blockPos = new MutableBlockPos();
        BlockState filler = craterType.getFiller().defaultBlockState();

        for (int j = y - 5; j <= maxY; j++) {
            blockPos.setY(j);

            for (int i = boundingBox.minX(); i <= boundingBox.maxX(); i++) {
                blockPos.setX(i);

                for (int k = boundingBox.minZ(); k <= boundingBox.maxZ(); k++) {
                    blockPos.setZ(k);
                    final double dx = i - x;
                    final double dz = k - z;
                    final double h = y - this.meteoriteSize + 1 + this.type.adjustCrater();

                    final double distanceFrom = dx * dx + dz * dz;

                    if (j > h + distanceFrom * 0.02) {
                        BlockState currentBlock = level.getBlockState(blockPos);

                        if (craterType != CraterType.NORMAL && j < y && currentBlock.getMaterial().isSolid()) {
                            if (j > h + distanceFrom * 0.02) {
                                this.putter.put(level, blockPos, filler);
                            }
                        } else {
                            this.putter.put(level, blockPos, Blocks.AIR.defaultBlockState());
                        }

                    }
                }
            }
        }

        for (var e : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(minX(x - 30), y - 5, minZ(z - 30), maxX(x + 30), y + 30, maxZ(z + 30)))) {
            e.discard();
        }
    }

    private void placeMeteorite() {
        // spawn meteor
        this.placeMeteoriteSkyStone();

        // If the meteorite's center is within the BB of the current placer, place the chest
        if (boundingBox.isInside(pos)) {
            placeChest();
        }
    }

    private void placeChest() {
        if (AEConfig.instance().isSpawnPressesInMeteoritesEnabled()) {
            this.putter.put(level, pos, this.skyChestDefinition.block().defaultBlockState());

            level.getBlockEntity(pos, AEBlockEntities.SKY_CHEST).ifPresent(chest -> {
                chest.setLootTable(METEORITE_CHEST_LOOTTABLE, random.nextLong());
            });
        }
    }

    private void placeMeteoriteSkyStone() {
        final int meteorXLength = minX(x - 8);
        final int meteorXHeight = maxX(x + 8);
        final int meteorZLength = minZ(z - 8);
        final int meteorZHeight = maxZ(z + 8);

        MutableBlockPos pos = new MutableBlockPos();
        for (int i = meteorXLength; i <= meteorXHeight; i++) {
            pos.setX(i);
            for (int j = y - 8; j < y + 8; j++) {
                pos.setY(j);
                for (int k = meteorZLength; k <= meteorZHeight; k++) {
                    pos.setZ(k);
                    var dx = i - x;
                    var dy = j - y;
                    var dz = k - z;

                    if (dx * dx * 0.7 + dy * dy * (j > y ? 1.4 : 0.8) + dz * dz * 0.7 < this.squaredMeteoriteSize) {
                        int drSquared = dx * dx + dy * dy + dz * dz;
                        if (crystalType == CrystalType.FLUIX && drSquared <= 1) {
                            this.putter.put(level, pos, fluixBlock);
                        } else if (crystalType == CrystalType.CERTUS_QUARTZ && drSquared <= 1) {
                            this.putter.put(level, pos, quartzBlocks.get(random.nextInt(quartzBlocks.size())));
                        } else {
                            this.putter.put(level, pos, skyStone);
                        }
                    }
                }
            }
        }
    }

    private void decay() {
        double randomShit = 0;

        final int meteorXLength = minX(x - 30);
        final int meteorXHeight = maxX(x + 30);
        final int meteorZLength = minZ(z - 30);
        final int meteorZHeight = maxZ(z + 30);

        MutableBlockPos blockPos = new MutableBlockPos();
        MutableBlockPos blockPosUp = new MutableBlockPos();
        MutableBlockPos blockPosDown = new MutableBlockPos();
        for (int i = meteorXLength; i <= meteorXHeight; i++) {
            blockPos.setX(i);
            blockPosUp.setX(i);
            blockPosDown.setX(i);
            for (int k = meteorZLength; k <= meteorZHeight; k++) {
                blockPos.setZ(k);
                blockPosUp.setZ(k);
                blockPosDown.setZ(k);
                for (int j = y - 9; j < y + 30; j++) {
                    blockPos.setY(j);
                    blockPosUp.setY(j + 1);
                    blockPosDown.setY(j - 1);
                    BlockState state = level.getBlockState(blockPos);
                    Block blk = level.getBlockState(blockPos).getBlock();

                    if (this.pureCrater && blk == craterType.getFiller()) {
                        continue;
                    }

                    // TODO reconsider
                    if (state.getMaterial().isReplaceable()) {
                        if (!level.isEmptyBlock(blockPosUp)) {
                            final BlockState stateUp = level.getBlockState(blockPosUp);
                            level.setBlock(blockPos, stateUp, Block.UPDATE_ALL);
                        } else if (randomShit < 100 * this.crater) {
                            final double dx = i - x;
                            final double dy = j - y;
                            final double dz = k - z;
                            final double dist = dx * dx + dy * dy + dz * dz;

                            final BlockState xf = level.getBlockState(blockPosDown);
                            if (!xf.getMaterial().isReplaceable()) {
                                final double extraRange = random.nextDouble() * 0.6;
                                final double height = this.crater * (extraRange + 0.2)
                                        - Math.abs(dist - this.crater * 1.7);

                                if (!xf.isAir() && height > 0 && random.nextDouble() > 0.6) {
                                    randomShit++;
                                    this.type.getRandomFall(level, blockPos);
                                }
                            }
                        }
                    } else if (level.isEmptyBlock(blockPosUp) && random.nextDouble() > 0.4) { // decay.
                        final double dx = i - x;
                        final double dy = j - y;
                        final double dz = k - z;
                        double dr2 = dx * dx + dy * dy + dz * dz;

                        if (/* this.squaredMeteoriteSize < dr2 && */dr2 < this.crater * 1.6) {
                            this.type.getRandomInset(level, blockPos);
                        }
                    }
                }
            }
        }
    }

    /**
     * If it finds a single water block at y62, it will replace any air blocks below the sea level with water.
     */
    private void placeCraterLake() {
        final int maxY = level.getSeaLevel() - 1;
        MutableBlockPos blockPos = new MutableBlockPos();

        for (int j = y - 5; j <= maxY; j++) {
            blockPos.setY(j);

            for (int i = boundingBox.minX(); i <= boundingBox.maxX(); i++) {
                blockPos.setX(i);

                for (int k = boundingBox.minZ(); k <= boundingBox.maxZ(); k++) {
                    blockPos.setZ(k);
                    final double dx = i - x;
                    final double dz = k - z;
                    final double h = y - this.meteoriteSize + 1 + this.type.adjustCrater();

                    final double distanceFrom = dx * dx + dz * dz;

                    if (j > h + distanceFrom * 0.02) {
                        BlockState currentBlock = level.getBlockState(blockPos);
                        if (currentBlock.getBlock() == Blocks.AIR) {
                            this.putter.put(level, blockPos, Blocks.WATER.defaultBlockState());
                        }

                    }
                }
            }
        }
    }

    private Fallout getFallout(LevelAccessor level, BlockPos pos, FalloutMode mode) {
        return switch (mode) {
            case SAND -> new FalloutSand(level, pos, this.putter, this.skyStone, random);
            case TERRACOTTA -> new FalloutCopy(level, pos, this.putter, this.skyStone, random);
            case ICE_SNOW -> new FalloutSnow(level, pos, this.putter, this.skyStone, random);
            default -> new Fallout(this.putter, this.skyStone, random);
        };
    }

}
