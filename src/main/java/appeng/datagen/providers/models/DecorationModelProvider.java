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

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public class DecorationModelProvider extends BlockStateProvider implements IAE2DataProvider {

    public DecorationModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, AppEng.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        wall(AEBlocks.SKY_STONE_WALL, modLoc("block/sky_stone_block"));
        wall(AEBlocks.SMOOTH_SKY_STONE_WALL, modLoc("block/smooth_sky_stone_block"));
        wall(AEBlocks.SKY_STONE_BRICK_WALL, modLoc("block/sky_stone_brick"));
        wall(AEBlocks.SKY_STONE_SMALL_BRICK_WALL, modLoc("block/sky_stone_small_brick"));
        wall(AEBlocks.FLUIX_WALL, modLoc("block/fluix_block"));
        wall(AEBlocks.QUARTZ_WALL, modLoc("block/quartz_block"));
        wall(AEBlocks.CHISELED_QUARTZ_WALL, modLoc("block/chiseled_quartz_block_side"));
        wall(AEBlocks.QUARTZ_PILLAR_WALL, modLoc("block/quartz_pillar_side"));
    }

    /**
     * Defines a standard wall blockstate, the necessary block models and item model.
     */
    private void wall(BlockDefinition block, ResourceLocation texture) {
        wallBlock((WallBlock) block.block(), texture);
        itemModels().wallInventory(block.id().getPath(), texture);
    }

}
