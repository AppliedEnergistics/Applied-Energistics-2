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

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;


import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;

public class DecorationModelProvider extends AE2BlockStateProvider {

    public DecorationModelProvider(PackOutput packOutput) {
        super(packOutput, AppEng.MOD_ID);
    }

    @Override
    protected void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
   // TODO    stairsBlock(AEBlocks.CHISELED_QUARTZ_STAIRS, "block/chiseled_quartz_block_top",
   // TODO            "block/chiseled_quartz_block_side", "block/chiseled_quartz_block_top");
   // TODO    slabBlock(AEBlocks.CHISELED_QUARTZ_SLAB, AEBlocks.CHISELED_QUARTZ_BLOCK, "block/chiseled_quartz_block_top",
   // TODO            "block/chiseled_quartz_block_side", "block/chiseled_quartz_block_top");
   // TODO    wall(AEBlocks.CHISELED_QUARTZ_WALL, "block/chiseled_quartz_block_side");

   // TODO    stairsBlock(AEBlocks.FLUIX_STAIRS, AEBlocks.FLUIX_BLOCK);
   // TODO    slabBlock(AEBlocks.FLUIX_SLAB, AEBlocks.FLUIX_BLOCK);
   // TODO    wall(AEBlocks.FLUIX_WALL, "block/fluix_block");

   // TODO    stairsBlock(AEBlocks.QUARTZ_STAIRS, AEBlocks.QUARTZ_BLOCK);
   // TODO    slabBlock(AEBlocks.QUARTZ_SLAB, AEBlocks.QUARTZ_BLOCK);
   // TODO    wall(AEBlocks.QUARTZ_WALL, "block/quartz_block");

   // TODO    stairsBlock(AEBlocks.CUT_QUARTZ_STAIRS, AEBlocks.CUT_QUARTZ_BLOCK);
   // TODO    slabBlock(AEBlocks.CUT_QUARTZ_SLAB, AEBlocks.CUT_QUARTZ_BLOCK);
   // TODO    wall(AEBlocks.CUT_QUARTZ_WALL, "block/cut_quartz_block");

   // TODO    simpleBlockAndItem(AEBlocks.SMOOTH_QUARTZ_BLOCK);
   // TODO    stairsBlock(AEBlocks.SMOOTH_QUARTZ_STAIRS, AEBlocks.SMOOTH_QUARTZ_BLOCK);
   // TODO    slabBlock(AEBlocks.SMOOTH_QUARTZ_SLAB, AEBlocks.SMOOTH_QUARTZ_BLOCK);
   // TODO    wall(AEBlocks.SMOOTH_QUARTZ_WALL, "block/smooth_quartz_block");

   // TODO    simpleBlockAndItem(AEBlocks.QUARTZ_BRICKS);
   // TODO    stairsBlock(AEBlocks.QUARTZ_BRICK_STAIRS, AEBlocks.QUARTZ_BRICKS);
   // TODO    slabBlock(AEBlocks.QUARTZ_BRICK_SLAB, AEBlocks.QUARTZ_BRICKS);
   // TODO    wall(AEBlocks.QUARTZ_BRICK_WALL, "block/quartz_bricks");

   // TODO    stairsBlock(AEBlocks.QUARTZ_PILLAR_STAIRS, "block/quartz_pillar_top", "block/quartz_pillar_side",
   // TODO            "block/quartz_pillar_top");
   // TODO    slabBlock(AEBlocks.QUARTZ_PILLAR_SLAB, AEBlocks.QUARTZ_PILLAR, "block/quartz_pillar_top",
   // TODO            "block/quartz_pillar_side", "block/quartz_pillar_top");
   // TODO    wall(AEBlocks.QUARTZ_PILLAR_WALL, "block/quartz_pillar_side");

   // TODO    simpleBlockAndItem(AEBlocks.SKY_STONE_BLOCK);
   // TODO    stairsBlock(AEBlocks.SKY_STONE_STAIRS, AEBlocks.SKY_STONE_BLOCK);
   // TODO    slabBlock(AEBlocks.SKY_STONE_SLAB, AEBlocks.SKY_STONE_BLOCK);
   // TODO    wall(AEBlocks.SKY_STONE_WALL, "block/sky_stone_block");

   // TODO    simpleBlockAndItem(AEBlocks.SKY_STONE_SMALL_BRICK);
   // TODO    stairsBlock(AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS, AEBlocks.SKY_STONE_SMALL_BRICK);
   // TODO    slabBlock(AEBlocks.SKY_STONE_SMALL_BRICK_SLAB, AEBlocks.SKY_STONE_SMALL_BRICK);
   // TODO    wall(AEBlocks.SKY_STONE_SMALL_BRICK_WALL, "block/sky_stone_small_brick");

   // TODO    simpleBlockAndItem(AEBlocks.SKY_STONE_BRICK);
   // TODO    stairsBlock(AEBlocks.SKY_STONE_BRICK_STAIRS, AEBlocks.SKY_STONE_BRICK);
   // TODO    slabBlock(AEBlocks.SKY_STONE_BRICK_SLAB, AEBlocks.SKY_STONE_BRICK);
   // TODO    wall(AEBlocks.SKY_STONE_BRICK_WALL, "block/sky_stone_brick");

   // TODO    stairsBlock(AEBlocks.SMOOTH_SKY_STONE_STAIRS, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
   // TODO    slabBlock(AEBlocks.SMOOTH_SKY_STONE_SLAB, AEBlocks.SMOOTH_SKY_STONE_BLOCK);
   // TODO    wall(AEBlocks.SMOOTH_SKY_STONE_WALL, "block/smooth_sky_stone_block");
    }
}
