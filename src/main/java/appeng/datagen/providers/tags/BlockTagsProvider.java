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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.api.ids.AETags;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockTagsProvider extends IntrinsicHolderTagsProvider<Block> implements IAE2DataProvider {
    public BlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries,
            ExistingFileHelper existingFileHelper) {
        super(packOutput, Registries.BLOCK, registries, block -> block.builtInRegistryHolder().key(), AppEng.MOD_ID,
                existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Black- and whitelist tags
        tag(AETags.SPATIAL_BLACKLIST)
                .add(Blocks.BEDROCK)
                .addOptionalTag(ConventionTags.IMMOVABLE_BLOCKS.location());
        tag(AETags.ANNIHILATION_PLANE_BLOCK_BLACKLIST);
        tag(AETags.FACADE_BLOCK_WHITELIST)
                .add(AEBlocks.QUARTZ_GLASS.block(), AEBlocks.QUARTZ_VIBRANT_GLASS.block())
                .addOptionalTag(ConventionTags.GLASS_BLOCK.location());
        tag(AETags.GROWTH_ACCELERATABLE)
                // TODO: Should all be in some conventional tag
                .add(Blocks.BAMBOO_SAPLING, Blocks.BAMBOO, Blocks.SUGAR_CANE, Blocks.SUGAR_CANE, Blocks.VINE,
                        Blocks.TWISTING_VINES, Blocks.WEEPING_VINES, Blocks.CAVE_VINES, Blocks.SWEET_BERRY_BUSH,
                        Blocks.NETHER_WART, Blocks.KELP, Blocks.COCOA)
                .addOptionalTag(ConventionTags.CROPS.location())
                .addOptionalTag(ConventionTags.SAPLINGS.location())
                .addTag(ConventionTags.BUDDING_BLOCKS_BLOCKS);

        // Only provide amethyst in the budding tag since that's the one we use; the other tags are for other mods
        tag(ConventionTags.BUDDING_BLOCKS_BLOCKS)
                .add(Blocks.BUDDING_AMETHYST)
                .add(AEBlocks.FLAWLESS_BUDDING_QUARTZ.block())
                .add(AEBlocks.FLAWED_BUDDING_QUARTZ.block())
                .add(AEBlocks.CHIPPED_BUDDING_QUARTZ.block())
                .add(AEBlocks.DAMAGED_BUDDING_QUARTZ.block());
        tag(ConventionTags.BUDS_BLOCKS)
                .add(AEBlocks.SMALL_QUARTZ_BUD.block())
                .add(AEBlocks.MEDIUM_QUARTZ_BUD.block())
                .add(AEBlocks.LARGE_QUARTZ_BUD.block());
        tag(ConventionTags.CLUSTERS_BLOCKS)
                .add(AEBlocks.QUARTZ_CLUSTER.block());

        tag(ConventionTags.CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK)
                .add(AEBlocks.QUARTZ_BLOCK.block());
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTag(ConventionTags.CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK);

        // Special behavior is associated with this tag, so our walls need to be added to it
        tag(BlockTags.WALLS).add(
                AEBlocks.SKY_STONE_WALL.block(),
                AEBlocks.SMOOTH_SKY_STONE_WALL.block(),
                AEBlocks.SKY_STONE_BRICK_WALL.block(),
                AEBlocks.SKY_STONE_SMALL_BRICK_WALL.block(),
                AEBlocks.FLUIX_WALL.block(),
                AEBlocks.QUARTZ_WALL.block(),
                AEBlocks.CUT_QUARTZ_WALL.block(),
                AEBlocks.SMOOTH_QUARTZ_WALL.block(),
                AEBlocks.QUARTZ_BRICK_WALL.block(),
                AEBlocks.CHISELED_QUARTZ_WALL.block(),
                AEBlocks.QUARTZ_PILLAR_WALL.block());

        // Fixtures should cause walls to have posts
        tag(BlockTags.WALL_POST_OVERRIDE).add(AEBlocks.QUARTZ_FIXTURE.block(), AEBlocks.LIGHT_DETECTOR.block());

        addEffectiveTools();
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
        Map<BlockDefinition<?>, List<TagKey<Block>>> specialTags = new HashMap<>();
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

    private TagsProvider.TagAppender<Block> tag(String name) {
        return tag(TagKey.create(Registries.BLOCK, new ResourceLocation(name)));
    }
}
