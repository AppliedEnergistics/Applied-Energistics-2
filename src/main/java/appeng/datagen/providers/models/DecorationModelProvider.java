/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.datagen.providers.models;

import static appeng.core.AppEng.makeId;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;

public class DecorationModelProvider extends AE2BlockStateProvider {

    public DecorationModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, AppEng.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockAndItem(AEBlocks.CHISELED_QUARTZ_BLOCK, models().cubeColumn(
                AEBlocks.CHISELED_QUARTZ_BLOCK.id().getPath(),
                makeId("block/chiseled_quartz_block_side"),
                makeId("block/chiseled_quartz_block_top")));
        stairsBlock(AEBlocks.CHISELED_QUARTZ_STAIRS, "block/chiseled_quartz_block_top",
                "block/chiseled_quartz_block_side", "block/chiseled_quartz_block_top");
        slabBlock(AEBlocks.CHISELED_QUARTZ_SLAB, AEBlocks.CHISELED_QUARTZ_BLOCK, "block/chiseled_quartz_block_top",
                "block/chiseled_quartz_block_side", "block/chiseled_quartz_block_top");
        wall(AEBlocks.CHISELED_QUARTZ_WALL, "block/chiseled_quartz_block_side");

        simpleBlockAndItem(AEBlocks.FLUIX_BLOCK);
        stairsBlock(AEBlocks.FLUIX_STAIRS, AEBlocks.FLUIX_BLOCK);
        slabBlock(AEBlocks.FLUIX_SLAB, AEBlocks.FLUIX_BLOCK);
        wall(AEBlocks.FLUIX_WALL, "block/fluix_block");

        simpleBlockAndItem(AEBlocks.QUARTZ_BLOCK);
        stairsBlock(AEBlocks.QUARTZ_STAIRS, AEBlocks.QUARTZ_BLOCK);
        slabBlock(AEBlocks.QUARTZ_SLAB, AEBlocks.QUARTZ_BLOCK);
        wall(AEBlocks.QUARTZ_WALL, "block/quartz_block");

        simpleBlockAndItem(AEBlocks.QUARTZ_PILLAR, models().cubeColumn(
                AEBlocks.QUARTZ_PILLAR.id().getPath(),
                makeId("block/quartz_pillar_side"),
                makeId("block/quartz_pillar_top")));
        stairsBlock(AEBlocks.QUARTZ_PILLAR_STAIRS, "block/quartz_pillar_top", "block/quartz_pillar_side",
                "block/quartz_pillar_top");
        slabBlock(AEBlocks.QUARTZ_PILLAR_SLAB, AEBlocks.QUARTZ_PILLAR, "block/quartz_pillar_top",
                "block/quartz_pillar_side", "block/quartz_pillar_top");
        wall(AEBlocks.QUARTZ_PILLAR_WALL, "block/quartz_pillar_side");

        simpleBlockAndItem(AEBlocks.SKY_STONE_BLOCK);
        stairsBlock(AEBlocks.SKY_STONE_STAIRS, AEBlocks.SKY_STONE_BLOCK);
        slabBlock(AEBlocks.SKY_STONE_SLAB, AEBlocks.SKY_STONE_BLOCK);
        wall(AEBlocks.SKY_STONE_WALL, "block/sky_stone_block");

        simpleBlockAndItem(AEBlocks.SKY_STONE_SMALL_BRICK);
        stairsBlock(AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS, AEBlocks.SKY_STONE_SMALL_BRICK);
        slabBlock(AEBlocks.SKY_STONE_SMALL_BRICK_SLAB, AEBlocks.SKY_STONE_SMALL_BRICK);
        wall(AEBlocks.SKY_STONE_SMALL_BRICK_WALL, "block/sky_stone_small_brick");

        simpleBlockAndItem(AEBlocks.SKY_STONE_BRICK);
        stairsBlock(AEBlocks.SKY_STONE_BRICK_STAIRS, AEBlocks.SKY_STONE_BRICK);
        slabBlock(AEBlocks.SKY_STONE_BRICK_SLAB, AEBlocks.SKY_STONE_BRICK);
        wall(AEBlocks.SKY_STONE_BRICK_WALL, "block/sky_stone_brick");

        simpleBlockAndItem(AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        stairsBlock(AEBlocks.SMOOTH_SKY_STONE_STAIRS, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        slabBlock(AEBlocks.SMOOTH_SKY_STONE_SLAB, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
        wall(AEBlocks.SMOOTH_SKY_STONE_WALL, "block/smooth_sky_stone_block");
    }
}
