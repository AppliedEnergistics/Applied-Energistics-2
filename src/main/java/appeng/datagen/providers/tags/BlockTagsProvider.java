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

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import appeng.core.AppEng;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.features.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockTagsProvider extends net.minecraft.data.BlockTagsProvider implements IAE2DataProvider {
    public BlockTagsProvider(GatherDataEvent dataEvent) {
        super(dataEvent.getGenerator(), AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void registerTags() {
        addForge("ores/certus_quartz", ApiBlocks.quartzOre, ApiBlocks.quartzOreCharged);
        addForge("ores", "#forge:ores/certus_quartz");

        addForge("storage_blocks/certus_quartz", ApiBlocks.quartzBlock);
        addForge("storage_blocks", "#forge:storage_blocks/certus_quartz");

        addForge("terracotta", Blocks.TERRACOTTA,
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

        addAe2("blacklisted/annihilation_plane");

        addAe2("spatial/blacklist");
        addAe2("spatial/whitelist");

        addAe2("whitelisted/facades",
                Blocks.GLASS,
                Tags.Blocks.STAINED_GLASS,
                ApiBlocks.quartzGlass,
                ApiBlocks.quartzVibrantGlass);

        // Special behavior is associated with this tag
        add(BlockTags.WALLS.getName(),
                ApiBlocks.skyStoneWall.block(),
                ApiBlocks.smoothSkyStoneWall.block(),
                ApiBlocks.skyStoneBrickWall.block(),
                ApiBlocks.skyStoneSmallBrickWall.block(),
                ApiBlocks.fluixWall.block(),
                ApiBlocks.quartzWall.block(),
                ApiBlocks.chiseledQuartzWall.block(),
                ApiBlocks.quartzPillarWall.block());
    }

    private void addForge(String tagName, Object... blockSources) {
        add(new ResourceLocation("forge", tagName), blockSources);
    }

    private void addAe2(String tagName, Object... blockSources) {
        add(AppEng.makeId(tagName), blockSources);
    }

    @SuppressWarnings("unchecked")
    private void add(ResourceLocation tagName, Object... blockSources) {
        Builder<Block> builder = getOrCreateBuilder(net.minecraft.tags.BlockTags.createOptional(tagName));

        for (Object blockSource : blockSources) {
            if (blockSource instanceof Block) {
                builder.add((Block) blockSource);
            } else if (blockSource instanceof BlockDefinition) {
                builder.add(((BlockDefinition) blockSource).block());
            } else if (blockSource instanceof ITag.INamedTag) {
                builder.addTag(
                        (ITag.INamedTag<Block>) blockSource);
            } else if (blockSource instanceof String) {
                String blockSourceString = (String) blockSource;
                if (blockSourceString.startsWith("#")) {
                    builder.add(new ITag.TagEntry(new ResourceLocation(blockSourceString.substring(1))));
                } else {
                    builder.add(new ITag.ItemEntry(new ResourceLocation(blockSourceString)));
                }
            } else {
                throw new IllegalArgumentException("Unknown block source: " + blockSource);
            }
        }
    }

    @Override
    protected Path makePath(ResourceLocation id) {
        return this.generator.getOutputFolder()
                .resolve("data/" + id.getNamespace() + "/tags/blocks/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Block Tags";
    }
}
