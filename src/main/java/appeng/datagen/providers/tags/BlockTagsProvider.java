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

package appeng.datagen.providers.tags;

import java.nio.file.Path;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import appeng.api.ids.AETags;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockTagsProvider extends net.minecraft.data.tags.BlockTagsProvider implements IAE2DataProvider {
    public BlockTagsProvider(GatherDataEvent dataEvent) {
        super(dataEvent.getGenerator(), AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        // Black- and whitelist tags
        tag(AETags.SPATIAL_BLACKLIST).add(Blocks.BEDROCK);
        tag(AETags.ANNIHILATION_PLANE_BLOCK_BLACKLIST);
        tag(AETags.FACADE_BLOCK_WHITELIST)
                .add(Blocks.GLASS,
                        AEBlocks.QUARTZ_GLASS.block(),
                        AEBlocks.QUARTZ_VIBRANT_GLASS.block())
                .addTag(Tags.Blocks.STAINED_GLASS);

        tag(ConventionTags.CERTUS_QUARTZ_ORE_BLOCK)
                .add(AEBlocks.QUARTZ_ORE.block(), AEBlocks.QUARTZ_ORE_CHARGED.block());
        tag(Tags.Blocks.ORES)
                .addTag(ConventionTags.CERTUS_QUARTZ_ORE_BLOCK);

        tag(ConventionTags.CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK)
                .add(AEBlocks.QUARTZ_BLOCK.block());
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTag(ConventionTags.CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK);

        tag(ConventionTags.TERRACOTTA_BLOCK).add(
                Blocks.TERRACOTTA,
                Blocks.WHITE_TERRACOTTA,
                Blocks.ORANGE_TERRACOTTA,
                Blocks.MAGENTA_TERRACOTTA,
                Blocks.LIGHT_BLUE_TERRACOTTA,
                Blocks.YELLOW_TERRACOTTA,
                Blocks.LIME_TERRACOTTA,
                Blocks.PINK_TERRACOTTA,
                Blocks.GRAY_TERRACOTTA,
                Blocks.LIGHT_GRAY_TERRACOTTA,
                Blocks.CYAN_TERRACOTTA,
                Blocks.PURPLE_TERRACOTTA,
                Blocks.BLUE_TERRACOTTA,
                Blocks.BROWN_TERRACOTTA,
                Blocks.GREEN_TERRACOTTA,
                Blocks.RED_TERRACOTTA,
                Blocks.BLACK_TERRACOTTA);

        // Special behavior is associated with this tag, so our walls need to be added to it
        tag(BlockTags.WALLS).add(
                AEBlocks.SKY_STONE_WALL.block(),
                AEBlocks.SMOOTH_SKY_STONE_WALL.block(),
                AEBlocks.SKY_STONE_BRICK_WALL.block(),
                AEBlocks.SKY_STONE_SMALL_BRICK_WALL.block(),
                AEBlocks.FLUIX_WALL.block(),
                AEBlocks.QUARTZ_WALL.block(),
                AEBlocks.CHISELED_QUARTZ_WALL.block(),
                AEBlocks.QUARTZ_PILLAR_WALL.block());
    }

    private TagsProvider.TagAppender<Block> tag(String name) {
        return tag(BlockTags.bind(name));
    }

    @Override
    protected Path getPath(ResourceLocation id) {
        return this.generator.getOutputFolder()
                .resolve("data/" + id.getNamespace() + "/tags/blocks/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Block Tags";
    }
}
