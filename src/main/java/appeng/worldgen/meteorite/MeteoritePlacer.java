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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.worlddata.WorldData;
import appeng.tile.storage.SkyChestBlockEntity;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorFixedInv;
import appeng.worldgen.meteorite.fallout.Fallout;
import appeng.worldgen.meteorite.fallout.FalloutCopy;
import appeng.worldgen.meteorite.fallout.FalloutMode;
import appeng.worldgen.meteorite.fallout.FalloutSand;
import appeng.worldgen.meteorite.fallout.FalloutSnow;

public final class MeteoritePlacer {
    private static final double PRESSES_SPAWN_CHANCE = 0.7;
    private static final int SKYSTONE_SPAWN_LIMIT = 12;
    private final IBlockDefinition skyChestDefinition;
    private final BlockState skyStone;
    private final Item skyStoneItem;
    private final MeteoriteBlockPutter putter = new MeteoriteBlockPutter();
    private final IWorld world;
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
    private final MutableBoundingBox boundingBox;

    public MeteoritePlacer(IWorld world, PlacedMeteoriteSettings settings, MutableBoundingBox boundingBox) {
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

        final IBlocks blocks = Api.instance().definitions().blocks();

        this.skyChestDefinition = blocks.skyStoneChest();
        this.skyStone = blocks.skyStoneBlock().block().getDefaultState();
        this.skyStoneItem = blocks.skyStoneBlock().item();

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
        if (x < boundingBox.minX) {
            return boundingBox.minX;
        } else if (x > boundingBox.maxX) {
            return boundingBox.maxX;
        }
        return x;
    }

    private int minZ(int x) {
        if (x < boundingBox.minZ) {
            return boundingBox.minZ;
        } else if (x > boundingBox.maxZ) {
            return boundingBox.maxZ;
        }
        return x;
    }

    private int maxX(int x) {
        if (x < boundingBox.minX) {
            return boundingBox.minX;
        } else if (x > boundingBox.maxX) {
            return boundingBox.maxX;
        }
        return x;
    }

    private int maxZ(int x) {
        if (x < boundingBox.minZ) {
            return boundingBox.minZ;
        } else if (x > boundingBox.maxZ) {
            return boundingBox.maxZ;
        }
        return x;
    }

    private void placeCrater() {
        final int maxY = 255;
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        BlockState filler = craterType.getFiller().getDefaultState();

        for (int j = y - 5; j <= maxY; j++) {
            blockPos.setY(j);

            for (int i = boundingBox.minX; i <= boundingBox.maxX; i++) {
                blockPos.setX(i);

                for (int k = boundingBox.minZ; k <= boundingBox.maxZ; k++) {
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
                            this.putter.put(world, blockPos, Blocks.AIR.getDefaultState());
                        }

                    }
                }
            }
        }

        for (final Object o : world.getEntitiesWithinAABB(ItemEntity.class,
                new AxisAlignedBB(minX(x - 30), y - 5, minZ(z - 30), maxX(x + 30), y + 30, maxZ(z + 30)), null)) {
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
        if (AEConfig.instance().isFeatureEnabled(AEFeature.SPAWN_PRESSES_IN_METEORITES)) {
            this.putter.put(world, pos, this.skyChestDefinition.block().getDefaultState());

            final SkyChestBlockEntity te = (SkyChestBlockEntity) world.getTileEntity(pos);
            InventoryAdaptor ap = null;
            if (te != null) {
                ap = new AdaptorFixedInv(te.getInternalInventory());
            }

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
                        final IMaterials materials = Api.instance().definitions().materials();

                        switch (r % 4) {
                            case 0:
                                toAdd = materials.calcProcessorPress().maybeStack(1).orElse(ItemStack.EMPTY);
                                break;
                            case 1:
                                toAdd = materials.engProcessorPress().maybeStack(1).orElse(ItemStack.EMPTY);
                                break;
                            case 2:
                                toAdd = materials.logicProcessorPress().maybeStack(1).orElse(ItemStack.EMPTY);
                                break;
                            case 3:
                                toAdd = materials.siliconPress().maybeStack(1).orElse(ItemStack.EMPTY);
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
                            final int amount = (int) ((Math.random() * SKYSTONE_SPAWN_LIMIT) + 1);
                            ap.addItems(new ItemStack(skyStoneItem, amount));
                            break;
                        case 1:
                            final List<ItemStack> possibles = new ArrayList<>();
                            possibles.add(new ItemStack(net.minecraft.item.Items.GOLD_NUGGET));

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

        BlockPos.Mutable pos = new BlockPos.Mutable();
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

        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        BlockPos.Mutable blockPosUp = new BlockPos.Mutable();
        BlockPos.Mutable blockPosDown = new BlockPos.Mutable();
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
                        if (!world.isAirBlock(blockPosUp)) {
                            final BlockState stateUp = world.getBlockState(blockPosUp);
                            world.setBlockState(blockPos, stateUp, 3);
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
                    } else {
                        // decay.
                        if (world.isAirBlock(blockPosUp)) {
                            if (Math.random() > 0.4) {
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
        }
    }

    /**
     * If it finds a single water block at y62, it will replace any air blocks below the sea level with water.
     */
    private void placeCraterLake() {
        final int maxY = world.getSeaLevel() - 1;
        BlockPos.Mutable blockPos = new BlockPos.Mutable();

        for (int j = y - 5; j <= maxY; j++) {
            blockPos.setY(j);

            for (int i = boundingBox.minX; i <= boundingBox.maxX; i++) {
                blockPos.setX(i);

                for (int k = boundingBox.minZ; k <= boundingBox.maxZ; k++) {
                    blockPos.setZ(k);
                    final double dx = i - x;
                    final double dz = k - z;
                    final double h = y - this.meteoriteSize + 1 + this.type.adjustCrater();

                    final double distanceFrom = dx * dx + dz * dz;

                    if (j > h + distanceFrom * 0.02) {
                        BlockState currentBlock = world.getBlockState(blockPos);
                        if (currentBlock.getBlock() == Blocks.AIR) {
                            this.putter.put(world, blockPos, Blocks.WATER.getDefaultState());
                        }

                    }
                }
            }
        }
    }

    private Fallout getFallout(IWorld w, BlockPos pos, FalloutMode mode) {
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
