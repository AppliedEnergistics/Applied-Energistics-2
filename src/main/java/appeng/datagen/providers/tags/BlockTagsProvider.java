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

import net.minecraft.tags.ITag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

import net.minecraft.data.tags.TagsProvider.TagAppender;

public class BlockTagsProvider extends net.minecraft.data.tags.BlockTagsProvider implements IAE2DataProvider {
    public BlockTagsProvider(GatherDataEvent dataEvent) {
        super(dataEvent.getGenerator(), AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        addForge("ores/certus_quartz", AEBlocks.QUARTZ_ORE, AEBlocks.QUARTZ_ORE_CHARGED);
        addForge("ores", "#forge:ores/certus_quartz");

        addForge("storage_blocks/certus_quartz", AEBlocks.QUARTZ_BLOCK);
        addForge("storage_blocks", "#forge:storage_blocks/certus_quartz");

        addForge("terracotta", net.minecraft.world.level.block.Blocks.TERRACOTTA,
                net.minecraft.world.level.block.Blocks.WHITE_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.ORANGE_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.MAGENTA_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.LIGHT_BLUE_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.YELLOW_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.LIME_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.PINK_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.GRAY_TERRACOTTA,
                Blocks.LIGHT_GRAY_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.CYAN_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.PURPLE_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.BLUE_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.BROWN_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.GREEN_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.RED_TERRACOTTA,
                net.minecraft.world.level.block.Blocks.BLACK_TERRACOTTA);

        addAe2("blacklisted/annihilation_plane");

        addAe2("spatial/blacklist");
        addAe2("spatial/whitelist");

        addAe2("whitelisted/facades",
                net.minecraft.world.level.block.Blocks.GLASS,
                Tags.Blocks.STAINED_GLASS,
                AEBlocks.QUARTZ_GLASS,
                AEBlocks.QUARTZ_VIBRANT_GLASS);

        // Special behavior is associated with this tag
        add(BlockTags.WALLS.getName(),
                AEBlocks.SKY_STONE_WALL.block(),
                AEBlocks.SMOOTH_SKY_STONE_WALL.block(),
                AEBlocks.SKY_STONE_BRICK_WALL.block(),
                AEBlocks.SKY_STONE_SMALL_BRICK_WALL.block(),
                AEBlocks.FLUIX_WALL.block(),
                AEBlocks.QUARTZ_WALL.block(),
                AEBlocks.CHISELED_QUARTZ_WALL.block(),
                AEBlocks.QUARTZ_PILLAR_WALL.block());
    }

    private void addForge(String tagName, Object... blockSources) {
        add(new ResourceLocation("forge", tagName), blockSources);
    }

    private void addAe2(String tagName, Object... blockSources) {
        add(AppEng.makeId(tagName), blockSources);
    }

    @SuppressWarnings("unchecked")
    private void add(ResourceLocation tagName, Object... blockSources) {
        TagAppender<Block> builder = tag(BlockTags.createOptional(tagName));

        for (Object blockSource : blockSources) {
            if (blockSource instanceof Block) {
                builder.add((net.minecraft.world.level.block.Block) blockSource);
            } else if (blockSource instanceof BlockDefinition) {
                builder.add(((BlockDefinition) blockSource).block());
            } else if (blockSource instanceof Named) {
                builder.addTag(
                        (Named<Block>) blockSource);
            } else if (blockSource instanceof String) {
                String blockSourceString = (String) blockSource;
                if (blockSourceString.startsWith("#")) {
                    builder.add(new Tag.TagEntry(new ResourceLocation(blockSourceString.substring(1))));
                } else {
                    builder.add(new ElementEntry(new ResourceLocation(blockSourceString)));
                }
            } else {
                throw new IllegalArgumentException("Unknown block source: " + blockSource);
            }
        }
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
