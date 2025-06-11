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

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
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

    private final BlockState skyStone;
    private final List<BlockState> quartzBlocks;
    private final List<BlockState> quartzBuds;
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
        this.squaredMeteoriteSize = this.meteoriteSize * this.meteoriteSize;

        double realCrater = this.meteoriteSize * 2 + 5;
        this.crater = realCrater * realCrater;

        this.quartzBlocks = getQuartzBudList();
        this.quartzBuds = Stream.of(
                AEBlocks.SMALL_QUARTZ_BUD,
                AEBlocks.MEDIUM_QUARTZ_BUD,
                AEBlocks.LARGE_QUARTZ_BUD).map(def -> def.block().defaultBlockState()).toList();
        this.skyStone = AEBlocks.SKY_STONE_BLOCK.block().defaultBlockState();

        this.type = getFallout(level, boundingBox.getCenter(), settings.getFallout());
    }

    private List<BlockState> getQuartzBudList() {
        if (AEConfig.instance().isSpawnFlawlessOnlyEnabled()) {
            return Stream.of(AEBlocks.FLAWLESS_BUDDING_QUARTZ).map(def -> def.block().defaultBlockState()).toList();
        }
        return Stream.of(
                AEBlocks.QUARTZ_BLOCK,
                AEBlocks.DAMAGED_BUDDING_QUARTZ,
                AEBlocks.CHIPPED_BUDDING_QUARTZ,
                AEBlocks.FLAWED_BUDDING_QUARTZ,
                AEBlocks.FLAWLESS_BUDDING_QUARTZ).map(def -> def.block().defaultBlockState()).toList();
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
        final int maxY = level.getMaxBuildHeight();
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

                        if (craterType != CraterType.NORMAL && j < y && currentBlock.isSolid()) {
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
            this.putter.put(level, pos, AEBlocks.MYSTERIOUS_CUBE.block().defaultBlockState());
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
                        // Leave a tiny room in the center
                        if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dz) <= 1) {
                            if (dy == -1) {
                                // Certus
                                var certusIndex = random.nextInt(quartzBlocks.size());
                                this.putter.put(level, pos, quartzBlocks.get(certusIndex));
                                // Add a bud on top if it's not a regular certus block (index 0), and not the center.
                                // (70% chance)
                                if (certusIndex != 0 && (dx != 0 || dz != 0) && random.nextFloat() <= 0.7) {
                                    var bud = Util.getRandom(quartzBuds, random);
                                    var budState = bud.setValue(AmethystClusterBlock.FACING, Direction.UP);
                                    this.putter.put(level, pos.offset(0, 1, 0), budState);
                                }
                            }
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
                    if (state.canBeReplaced()) {
                        if (!level.isEmptyBlock(blockPosUp)) {
                            final BlockState stateUp = level.getBlockState(blockPosUp);
                            level.setBlock(blockPos, stateUp, Block.UPDATE_ALL);
                        } else if (randomShit < 100 * this.crater) {
                            final double dx = i - x;
                            final double dy = j - y;
                            final double dz = k - z;
                            final double dist = dx * dx + dy * dy + dz * dz;

                            final BlockState xf = level.getBlockState(blockPosDown);
                            if (!xf.canBeReplaced()) {
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

                        // Don't touch the center room!
                        if (!(Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dz) <= 1) && dr2 < this.crater * 1.6) {
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
        ChunkAccess currentChunk;

        for (int currentX = boundingBox.minX(); currentX <= boundingBox.maxX(); currentX++) {
            blockPos.setX(currentX);

            for (int currentZ = boundingBox.minZ(); currentZ <= boundingBox.maxZ(); currentZ++) {
                blockPos.setZ(currentZ);
                currentChunk = level.getChunk(blockPos);

                for (int currentY = y - 5; currentY <= maxY; currentY++) {
                    blockPos.setY(currentY);

                    final double dx = currentX - x;
                    final double dz = currentZ - z;
                    final double h = y - this.meteoriteSize + 1 + this.type.adjustCrater();

                    final double distanceFrom = dx * dx + dz * dz;

                    if (currentY > h + distanceFrom * 0.02) {
                        BlockState currentBlock = currentChunk.getBlockState(blockPos);
                        if (currentBlock.getBlock() == Blocks.AIR) {
                            this.putter.put(level, blockPos, Blocks.WATER.defaultBlockState());

                            if (currentY == maxY) {
                                level.scheduleTick(blockPos, Fluids.WATER, 0);
                            }
                        }
                    } else if (maxY + (maxY - currentY) * 2 + 2 > h + distanceFrom * 0.02) {
                        pillarDownSlopeBlocks(currentChunk, blockPos);
                    }
                }
            }
        }
    }

    private void pillarDownSlopeBlocks(ChunkAccess currentChunk, MutableBlockPos blockPos) {
        MutableBlockPos enclosingBlockPos = new MutableBlockPos();
        enclosingBlockPos.set(blockPos);

        for (int i = 0; i < 20; i++) {
            if (placeEnclosingBlock(currentChunk, enclosingBlockPos)) {
                break;
            }
            enclosingBlockPos.move(Direction.DOWN);
        }
    }

    private boolean placeEnclosingBlock(ChunkAccess currentChunk, MutableBlockPos enclosingBlockPos) {
        BlockState currentState = currentChunk.getBlockState(enclosingBlockPos);
        if (currentState.getBlock() == Blocks.AIR ||
                (currentState.getFluidState().isEmpty() &&
                        (currentState.canBeReplaced() || currentState.is(BlockTags.REPLACEABLE)))) {

            if (craterType == CraterType.LAVA && level.getRandom().nextFloat() < 0.075f) {
                this.putter.put(level, enclosingBlockPos, Blocks.MAGMA_BLOCK.defaultBlockState());
            } else {
                this.type.getRandomFall(level, enclosingBlockPos);
            }
        } else {
            return true;
        }
        return false;
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
