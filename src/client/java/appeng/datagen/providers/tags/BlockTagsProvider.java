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
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import appeng.api.ids.AETags;
import appeng.core.AppEng;
import appeng.core.ConventionTags;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

// IntrinsicHolderTagsProvider was removed. TagsProvider#tag now hands out a
// TagAppender that only accepts ResourceKeys, so we re-add the intrinsic Block overloads via a
// wrapping appender. This mirrors NeoForgeBlockTagsProvider.Appender in NeoForge 26.3 and keeps
// every tag entry (and its order) byte-identical to what IntrinsicHolderTagsProvider produced.
public class BlockTagsProvider extends TagsProvider<Block> implements IAE2DataProvider {
    public BlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, Registries.BLOCK, registries, AppEng.MOD_ID);
    }

    // replacement for IntrinsicHolderTagsProvider.IntrinsicTagAppender
    protected record Appender(TagAppender<Block> delegate) implements TagAppender<Block> {
        @Override
        public Appender add(ResourceKey<Block> element) {
            delegate.add(element);
            return this;
        }

        @SafeVarargs
        public final Appender add(ResourceKey<Block>... elements) {
            delegate.add(elements);
            return this;
        }

        public Appender add(Block... blocks) {
            for (Block block : blocks) {
                delegate.add(block.builtInRegistryHolder().key());
            }
            return this;
        }

        @Override
        public Appender addOptional(ResourceKey<Block> element) {
            delegate.addOptional(element);
            return this;
        }

        @Override
        public Appender addTag(TagKey<Block> tag) {
            delegate.addTag(tag);
            return this;
        }

        @Override
        public Appender addOptionalTag(TagKey<Block> tag) {
            delegate.addOptionalTag(tag);
            return this;
        }

        @Override
        public Appender add(TagEntry entry) {
            delegate.add(entry);
            return this;
        }

        @Override
        public Appender replace(boolean value) {
            delegate.replace(value);
            return this;
        }

        @Override
        public Appender remove(ResourceKey<Block> element) {
            delegate.remove(element);
            return this;
        }

        @Override
        public Appender remove(TagKey<Block> tag) {
            delegate.remove(tag);
            return this;
        }
    }

    @Override
    protected Appender tag(TagKey<Block> tag) {
        return new Appender(super.tag(tag));
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // Black- and whitelist tags
        tag(AETags.SPATIAL_BLACKLIST)
                .add(Blocks.BEDROCK)
                .addOptionalTag(ConventionTags.IMMOVABLE_BLOCKS);
        tag(AETags.ANNIHILATION_PLANE_BLOCK_BLACKLIST);
        tag(AETags.FACADE_BLOCK_WHITELIST)
                .add(AEBlocks.QUARTZ_GLASS.block(), AEBlocks.QUARTZ_VIBRANT_GLASS.block(),
                        Blocks.CHISELED_BOOKSHELF, Blocks.JUKEBOX, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.DROPPER,
                        Blocks.DISPENSER, Blocks.CRAFTER, Blocks.BARREL, Blocks.BEE_NEST, Blocks.BEEHIVE,
                        Blocks.SCULK_CATALYST, Blocks.SOUL_SAND, Blocks.HONEY_BLOCK,
                        AEBlocks.CONTROLLER.block(), AEBlocks.CRAFTING_STORAGE_1K.block(),
                        AEBlocks.CRAFTING_STORAGE_4K.block(), AEBlocks.CRAFTING_STORAGE_16K.block(),
                        AEBlocks.CRAFTING_STORAGE_64K.block(), AEBlocks.CRAFTING_STORAGE_256K.block(),
                        AEBlocks.CRAFTING_MONITOR.block(), AEBlocks.CRAFTING_UNIT.block(),
                        AEBlocks.CRAFTING_ACCELERATOR.block())
                .addOptionalTag(ConventionTags.GLASS_BLOCK);
        tag(AETags.GROWTH_ACCELERATABLE)
                // TODO: Should all be in some conventional tag
                .add(Blocks.BAMBOO_SAPLING, Blocks.BAMBOO, Blocks.SUGAR_CANE, Blocks.VINE,
                        Blocks.TWISTING_VINES, Blocks.WEEPING_VINES, Blocks.CAVE_VINES, Blocks.SWEET_BERRY_BUSH,
                        Blocks.NETHER_WART, Blocks.KELP, Blocks.COCOA)
                .addOptionalTag(ConventionTags.CROPS)
                .addOptionalTag(ConventionTags.SAPLINGS)
                .addTag(ConventionTags.BUDDING_BLOCKS_BLOCKS);

        tag(ConventionTags.BUDDING_BLOCKS_BLOCKS)
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

        tag(Tags.Blocks.CHESTS).add(AEBlocks.SKY_STONE_CHEST.block(), AEBlocks.SMOOTH_SKY_STONE_CHEST.block());
        tag(ConventionTags.GLASS_BLOCK).add(AEBlocks.QUARTZ_GLASS.block(), AEBlocks.QUARTZ_VIBRANT_GLASS.block());

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
}
