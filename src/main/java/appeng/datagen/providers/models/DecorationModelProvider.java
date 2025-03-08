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

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;

public class DecorationModelProvider extends AE2BlockStateProvider {
    public DecorationModelProvider(PackOutput packOutput) {
        super(packOutput, AppEng.MOD_ID);
    }

    private static final BlockFamily CHISELED_QUARTZ = new BlockFamily.Builder(AEBlocks.CHISELED_QUARTZ_BLOCK.block())
            .stairs(AEBlocks.CHISELED_QUARTZ_STAIRS.block())
            .slab(AEBlocks.CHISELED_QUARTZ_SLAB.block())
            .wall(AEBlocks.CHISELED_QUARTZ_WALL.block())
            .getFamily();

    private static final BlockFamily FLUIX = new BlockFamily.Builder(AEBlocks.FLUIX_BLOCK.block())
            .stairs(AEBlocks.FLUIX_STAIRS.block())
            .slab(AEBlocks.FLUIX_SLAB.block())
            .wall(AEBlocks.FLUIX_WALL.block())
            .getFamily();

    private static final BlockFamily QUARTZ = new BlockFamily.Builder(AEBlocks.QUARTZ_BLOCK.block())
            .stairs(AEBlocks.QUARTZ_STAIRS.block())
            .slab(AEBlocks.QUARTZ_SLAB.block())
            .wall(AEBlocks.QUARTZ_WALL.block())
            .getFamily();

    private static final BlockFamily CUT_QUARTZ = new BlockFamily.Builder(AEBlocks.CUT_QUARTZ_BLOCK.block())
            .stairs(AEBlocks.CUT_QUARTZ_STAIRS.block())
            .slab(AEBlocks.CUT_QUARTZ_SLAB.block())
            .wall(AEBlocks.CUT_QUARTZ_WALL.block())
            .getFamily();

    private static final BlockFamily SMOOTH_QUARTZ = new BlockFamily.Builder(AEBlocks.SMOOTH_QUARTZ_BLOCK.block())
            .stairs(AEBlocks.SMOOTH_QUARTZ_STAIRS.block())
            .slab(AEBlocks.SMOOTH_QUARTZ_SLAB.block())
            .wall(AEBlocks.SMOOTH_QUARTZ_WALL.block())
            .getFamily();

    private static final BlockFamily QUARTZ_BRICK = new BlockFamily.Builder(AEBlocks.QUARTZ_BRICKS.block())
            .stairs(AEBlocks.QUARTZ_BRICK_STAIRS.block())
            .slab(AEBlocks.QUARTZ_BRICK_SLAB.block())
            .wall(AEBlocks.QUARTZ_BRICK_WALL.block())
            .getFamily();

    private static final BlockFamily QUARTZ_PILLAR = new BlockFamily.Builder(AEBlocks.QUARTZ_PILLAR.block())
            .stairs(AEBlocks.QUARTZ_PILLAR_STAIRS.block())
            .slab(AEBlocks.QUARTZ_PILLAR_SLAB.block())
            .wall(AEBlocks.QUARTZ_PILLAR_WALL.block())
            .getFamily();

    private static final BlockFamily SKY_STONE_BLOCK = new BlockFamily.Builder(AEBlocks.SKY_STONE_BLOCK.block())
            .stairs(AEBlocks.SKY_STONE_STAIRS.block())
            .slab(AEBlocks.SKY_STONE_SLAB.block())
            .wall(AEBlocks.SKY_STONE_WALL.block())
            .getFamily();

    private static final BlockFamily SKY_STONE_SMALL_BRICK = new BlockFamily.Builder(AEBlocks.SKY_STONE_SMALL_BRICK.block())
            .stairs(AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS.block())
            .slab(AEBlocks.SKY_STONE_SMALL_BRICK_SLAB.block())
            .wall(AEBlocks.SKY_STONE_SMALL_BRICK_WALL.block())
            .getFamily();

    private static final BlockFamily SKY_STONE_BRICK = new BlockFamily.Builder(AEBlocks.SKY_STONE_BRICK.block())
            .stairs(AEBlocks.SKY_STONE_BRICK_STAIRS.block())
            .slab(AEBlocks.SKY_STONE_BRICK_SLAB.block())
            .wall(AEBlocks.SKY_STONE_BRICK_WALL.block())
            .getFamily();

    private static final BlockFamily SMOOTH_SKY_STONE_BLOCK = new BlockFamily.Builder(AEBlocks.SMOOTH_SKY_STONE_BLOCK.block())
            .stairs(AEBlocks.SMOOTH_SKY_STONE_STAIRS.block())
            .slab(AEBlocks.SMOOTH_SKY_STONE_SLAB.block())
            .wall(AEBlocks.SMOOTH_SKY_STONE_WALL.block())
            .getFamily();

    @Override
    protected void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.family(AEBlocks.CHISELED_QUARTZ_BLOCK.block())
                .generateFor(CHISELED_QUARTZ);
        blockModels.family(AEBlocks.FLUIX_BLOCK.block())
                .generateFor(FLUIX);
        blockModels.family(AEBlocks.QUARTZ_BLOCK.block())
                .generateFor(QUARTZ);
        blockModels.family(AEBlocks.CUT_QUARTZ_BLOCK.block())
                .generateFor(CUT_QUARTZ);
        blockModels.family(AEBlocks.SMOOTH_QUARTZ_BLOCK.block())
                .generateFor(SMOOTH_QUARTZ);
        blockModels.family(AEBlocks.QUARTZ_BRICKS.block())
                .generateFor(QUARTZ_BRICK);
        blockModels.family(AEBlocks.QUARTZ_PILLAR.block())
                .generateFor(QUARTZ_PILLAR);
        blockModels.family(AEBlocks.SKY_STONE_BLOCK.block())
                .generateFor(SKY_STONE_BLOCK);
        blockModels.family(AEBlocks.SKY_STONE_SMALL_BRICK.block())
                .generateFor(SKY_STONE_SMALL_BRICK);
        blockModels.family(AEBlocks.SKY_STONE_BRICK.block())
                .generateFor(SKY_STONE_BRICK);
        blockModels.family(AEBlocks.SMOOTH_SKY_STONE_BLOCK.block())
                .generateFor(SMOOTH_SKY_STONE_BLOCK);

    }
}
