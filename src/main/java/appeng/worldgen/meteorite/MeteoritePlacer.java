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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.LevelAccessor;

import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.worlddata.WorldData;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.worldgen.meteorite.fallout.Fallout;
import appeng.worldgen.meteorite.fallout.FalloutCopy;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import appeng.worldgen.meteorite.fallout.FalloutSand;
import appeng.worldgen.meteorite.fallout.FalloutSnow;
import net.minecraft.world.entity.item.ItemEntity;

public final class MeteoritePlacer {
    private static final double PRESSES_SPAWN_CHANCE = 0.7;
    private static final int SKYSTONE_SPAWN_LIMIT = 12;
    private final BlockDefinition skyChestDefinition;
    private final BlockState skyStone;
    private final Item skyStoneItem;
    private final MeteoriteBlockPutter putter = new MeteoriteBlockPutter();
    private final LevelAccessor world;
    private final Fallout type;
    private final BlockPos pos;
    private final int x;
    private final int y;
    private final int z;
    private final double meteoriteSize;
    private final double realCrater;
    private final double squaredMeteoriteSize;
    private final double crater;
    private final boolean placeCrater;
    private final CraterType craterType;
    private final boolean pureCrater;
    private final boolean craterLake;
    private final BoundingBox boundingBox;

    public MeteoritePlacer(LevelAccessor world, PlacedMeteoriteSettings settings, BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        this.world = world;
        this.pos = settings.getPos();
        this.x = settings.getPos().getX();
        this.y = settings.getPos().getY();
        this.z = settings.getPos().getZ();
        this.meteoriteSize = settings.getMeteoriteRadius();
        this.realCrater = this.meteoriteSize * 2 + 5;
        this.placeCrater = settings.shouldPlaceCrater();
        this.craterType = settings.getCraterType();
        this.pureCrater = settings.isPureCrater();
        this.craterLake = settings.isCraterLake();
        this.squaredMeteoriteSize = this.meteoriteSize * this.meteoriteSize;
        this.crater = this.realCrater * this.realCrater;

        this.skyChestDefinition = AEBlocks.SKY_STONE_CHEST;
        this.skyStone = AEBlocks.SKY_STONE_BLOCK.block().defaultBlockState();
        this.skyStoneItem = AEBlocks.SKY_STONE_BLOCK.asItem();

        this.type = getFallout(world, settings.getPos(), settings.getFallout());
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
        if (x < boundingBox.x0) {
            return boundingBox.x0;
        } else if (x > boundingBox.x1) {
            return boundingBox.x1;
        }
        return x;
    }

    private int minZ(int x) {
        if (x < boundingBox.z0) {
            return boundingBox.z0;
        } else if (x > boundingBox.z1) {
            return boundingBox.z1;
        }
        return x;
    }

    private int maxX(int x) {
        if (x < boundingBox.x0) {
            return boundingBox.x0;
        } else if (x > boundingBox.x1) {
            return boundingBox.x1;
        }
        return x;
    }

    private int maxZ(int x) {
        if (x < boundingBox.z0) {
            return boundingBox.z0;
        } else if (x > boundingBox.z1) {
            return boundingBox.z1;
        }
        return x;
    }

    private void placeCrater() {
        final int maxY = 255;
        MutableBlockPos blockPos = new MutableBlockPos();
        BlockState filler = craterType.getFiller().defaultBlockState();

        for (int j = y - 5; j <= maxY; j++) {
            blockPos.setY(j);

            for (int i = boundingBox.x0; i <= boundingBox.x1; i++) {
                blockPos.setX(i);

                for (int k = boundingBox.z0; k <= boundingBox.z1; k++) {
                    blockPos.setZ(k);
                    final double dx = i - x;
                    final double dz = k - z;
                    final double h = y - this.meteoriteSize + 1 + this.type.adjustCrater();

                    final double distanceFrom = dx * dx + dz * dz;

                    if (j > h + distanceFrom * 0.02) {
                        BlockState currentBlock = world.getBlockState(blockPos);

                        if (craterType != CraterType.NORMAL && j < y && currentBlock.getMaterial().isSolid()) {
                            if (j > h + distanceFrom * 0.02) {
                                this.putter.put(world, blockPos, filler);
                            }
                        } else {
                            this.putter.put(world, blockPos, Blocks.AIR.defaultBlockState());
                        }

                    }
                }
            }
        }

        for (final Object o : world.getEntitiesOfClass(ItemEntity.class,
                new AABB(minX(x - 30), y - 5, minZ(z - 30), maxX(x + 30), y + 30, maxZ(z + 30)))) {
            final Entity e = (Entity) o;
            e.remove();
        }
    }

    private void placeMeteorite() {
        // spawn meteor
        this.placeMeteoriteSkyStone();

        placeChest();
    }

    private void placeChest() {
        if (AEConfig.instance().isSpawnPressesInMeteoritesEnabled()) {
            this.putter.put(world, pos, this.skyChestDefinition.block().defaultBlockState());

            final BlockEntity te = world.getBlockEntity(pos); // FIXME: this is also probably a band-aid for another issue
            final InventoryAdaptor ap = InventoryAdaptor.getAdaptor(te, Direction.UP);
            if (ap != null && !ap.containsItems()) // FIXME: band-aid for meteorites being generated multiple times
            {
                // TODO: loot tables would be better
                int primary = Math.max(1, (int) (Math.random() * 4));

                if (primary > 3) // in case math breaks...
                {
                    primary = 3;
                }

                for (int zz = 0; zz < primary; zz++) {
                    int r;
                    boolean duplicate;

                    do {
                        duplicate = false;

                        if (Math.random() > PRESSES_SPAWN_CHANCE) {
                            r = WorldData.instance().storageData().getNextOrderedValue("presses", 0);
                        } else {
                            r = (int) (Math.random() * 1000);
                        }

                        ItemStack toAdd = ItemStack.EMPTY;

                        switch (r % 4) {
                            case 0:
                                toAdd = AEItems.CALCULATION_PROCESSOR_PRESS.stack();
                                break;
                            case 1:
                                toAdd = AEItems.ENGINEERING_PROCESSOR_PRESS.stack();
                                break;
                            case 2:
                                toAdd = AEItems.LOGIC_PROCESSOR_PRESS.stack();
                                break;
                            case 3:
                                toAdd = AEItems.SILICON_PRESS.stack();
                                break;
                            default:
                        }

                        if (!toAdd.isEmpty()) {
                            if (ap.simulateRemove(1, toAdd, null).isEmpty()) {
                                ap.addItems(toAdd);
                            } else {
                                duplicate = true;
                            }
                        }
                    } while (duplicate);
                }

                final int secondary = Math.max(1, (int) (Math.random() * 3));
                for (int zz = 0; zz < secondary; zz++) {
                    switch ((int) (Math.random() * 1000) % 3) {
                        case 0:
                            final int amount = (int) (Math.random() * SKYSTONE_SPAWN_LIMIT + 1);
                            ap.addItems(new ItemStack(skyStoneItem, amount));
                            break;
                        case 1:
                            final List<ItemStack> possibles = new ArrayList<>();
                            possibles.add(new ItemStack(Items.GOLD_NUGGET));

                            ItemStack nugget = Platform.pickRandom(possibles);
                            if (nugget != null && !nugget.isEmpty()) {
                                nugget = nugget.copy();
                                nugget.setCount((int) (Math.random() * 12) + 1);
                                ap.addItems(nugget);
                            }
                            break;
                    }
                }
            }
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
                    final double dx = i - x;
                    final double dy = j - y;
                    final double dz = k - z;

                    if (dx * dx * 0.7 + dy * dy * (j > y ? 1.4 : 0.8) + dz * dz * 0.7 < this.squaredMeteoriteSize) {
                        this.putter.put(world, pos, skyStone);
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
                    BlockState state = world.getBlockState(blockPos);
                    Block blk = world.getBlockState(blockPos).getBlock();

                    if (this.pureCrater && blk == craterType.getFiller()) {
                        continue;
                    }

                    // TODO reconsider
                    if (state.getMaterial().isReplaceable()) {
                        if (!world.isEmptyBlock(blockPosUp)) {
                            final BlockState stateUp = world.getBlockState(blockPosUp);
                            world.setBlock(blockPos, stateUp, 3);
                        } else if (randomShit < 100 * this.crater) {
                            final double dx = i - x;
                            final double dy = j - y;
                            final double dz = k - z;
                            final double dist = dx * dx + dy * dy + dz * dz;

                            final BlockState xf = world.getBlockState(blockPosDown);
                            if (!xf.getMaterial().isReplaceable()) {
                                final double extraRange = Math.random() * 0.6;
                                final double height = this.crater * (extraRange + 0.2)
                                        - Math.abs(dist - this.crater * 1.7);

                                if (!xf.isAir() && height > 0 && Math.random() > 0.6) {
                                    randomShit++;
                                    this.type.getRandomFall(world, blockPos);
                                }
                            }
                        }
                    } else // decay.
                    if (world.isEmptyBlock(blockPosUp) && Math.random() > 0.4) {
                        final double dx = i - x;
                        final double dy = j - y;
                        final double dz = k - z;

                        if (dx * dx + dy * dy + dz * dz < this.crater * 1.6) {
                            this.type.getRandomInset(world, blockPos);
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
        final int maxY = world.getSeaLevel() - 1;
        MutableBlockPos blockPos = new MutableBlockPos();

        for (int j = y - 5; j <= maxY; j++) {
            blockPos.setY(j);

            for (int i = boundingBox.x0; i <= boundingBox.x1; i++) {
                blockPos.setX(i);

                for (int k = boundingBox.z0; k <= boundingBox.z1; k++) {
                    blockPos.setZ(k);
                    final double dx = i - x;
                    final double dz = k - z;
                    final double h = y - this.meteoriteSize + 1 + this.type.adjustCrater();

                    final double distanceFrom = dx * dx + dz * dz;

                    if (j > h + distanceFrom * 0.02) {
                        BlockState currentBlock = world.getBlockState(blockPos);
                        if (currentBlock.getBlock() == Blocks.AIR) {
                            this.putter.put(world, blockPos, Blocks.WATER.defaultBlockState());
                        }

                    }
                }
            }
        }
    }

    private Fallout getFallout(LevelAccessor w, BlockPos pos, FalloutMode mode) {
        switch (mode) {
            case SAND:
                return new FalloutSand(w, pos, this.putter, this.skyStone);
            case TERRACOTTA:
                return new FalloutCopy(w, pos, this.putter, this.skyStone);
            case ICE_SNOW:
                return new FalloutSnow(w, pos, this.putter, this.skyStone);
            case DEFAULT:
            default:
                return new Fallout(this.putter, this.skyStone);
        }
    }

}
