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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import appeng.api.ids.AETags;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockTagsProvider extends net.minecraft.data.tags.BlockTagsProvider implements IAE2DataProvider {
    public BlockTagsProvider(DataGenerator generator) {
        super(generator);
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
                .addTag(ConventionTags.STAINED_GLASS_BLOCK);

        tag(ConventionTags.CERTUS_QUARTZ_ORE_BLOCK)
                .add(AEBlocks.QUARTZ_ORE.block())
                .add(AEBlocks.DEEPSLATE_QUARTZ_ORE.block());

        tag("c:ores")
                .addTag(ConventionTags.CERTUS_QUARTZ_ORE_BLOCK);

        tag(ConventionTags.CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK)
                .add(AEBlocks.QUARTZ_BLOCK.block());
        tag("c:storage_blocks")
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

        addEffectiveTools();

        addConventionTags();
    }

    /**
     * All sky-stone related blocks should be minable with iron-pickaxes and up.
     */
    private static final BlockDefinition<?>[] SKY_STONE_BLOCKS = {
            AEBlocks.SKY_STONE_BLOCK,
            AEBlocks.SMOOTH_SKY_STONE_BLOCK,
            AEBlocks.SKY_STONE_BRICK,
            AEBlocks.SKY_STONE_SMALL_BRICK,
            AEBlocks.SKY_STONE_CHEST,
            AEBlocks.SMOOTH_SKY_STONE_CHEST,
            AEBlocks.SKY_STONE_STAIRS,
            AEBlocks.SMOOTH_SKY_STONE_STAIRS,
            AEBlocks.SKY_STONE_BRICK_STAIRS,
            AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS,
            AEBlocks.SKY_STONE_WALL,
            AEBlocks.SMOOTH_SKY_STONE_WALL,
            AEBlocks.SKY_STONE_BRICK_WALL,
            AEBlocks.SKY_STONE_SMALL_BRICK_WALL,
            AEBlocks.SKY_STONE_SLAB,
            AEBlocks.SMOOTH_SKY_STONE_SLAB,
            AEBlocks.SKY_STONE_BRICK_SLAB,
            AEBlocks.SKY_STONE_SMALL_BRICK_SLAB
    };

    private void addEffectiveTools() {
        Map<BlockDefinition<?>, List<Tag.Named<Block>>> specialTags = new HashMap<>();
        for (var skyStoneBlock : SKY_STONE_BLOCKS) {
            specialTags.put(skyStoneBlock, List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL));
        }
        var defaultTags = List.of(BlockTags.MINEABLE_WITH_PICKAXE);

        for (var block : AEBlocks.getBlocks()) {
            for (var desiredTag : specialTags.getOrDefault(block, defaultTags)) {
                tag(desiredTag).add(block.block());
            }
        }

    }

    /**
     * Add convention tags that would normally be provided by the Platform but need to be added manually on Fabric.
     */
    private void addConventionTags() {
        tag(ConventionTags.STAINED_GLASS_BLOCK)
                .add(
                        Blocks.WHITE_STAINED_GLASS,
                        Blocks.ORANGE_STAINED_GLASS,
                        Blocks.MAGENTA_STAINED_GLASS,
                        Blocks.LIGHT_BLUE_STAINED_GLASS,
                        Blocks.YELLOW_STAINED_GLASS,
                        Blocks.LIME_STAINED_GLASS,
                        Blocks.PINK_STAINED_GLASS,
                        Blocks.GRAY_STAINED_GLASS,
                        Blocks.LIGHT_GRAY_STAINED_GLASS,
                        Blocks.CYAN_STAINED_GLASS,
                        Blocks.PURPLE_STAINED_GLASS,
                        Blocks.BLUE_STAINED_GLASS,
                        Blocks.BROWN_STAINED_GLASS,
                        Blocks.GREEN_STAINED_GLASS,
                        Blocks.RED_STAINED_GLASS,
                        Blocks.BLACK_STAINED_GLASS);
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
