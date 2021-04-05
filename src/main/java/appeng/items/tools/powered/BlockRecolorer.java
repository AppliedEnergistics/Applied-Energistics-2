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

package appeng.items.tools.powered;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import appeng.api.util.AEColor;

/**
 * Allows recoloring a variety of vanilla blocks.
 */
public final class BlockRecolorer {

    private BlockRecolorer() {
    }

    private static final BiMap<AEColor, Block> STAINED_GLASS_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_STAINED_GLASS)
            .put(AEColor.ORANGE, Blocks.ORANGE_STAINED_GLASS).put(AEColor.MAGENTA, Blocks.MAGENTA_STAINED_GLASS)
            .put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_STAINED_GLASS).put(AEColor.YELLOW, Blocks.YELLOW_STAINED_GLASS)
            .put(AEColor.LIME, Blocks.LIME_STAINED_GLASS).put(AEColor.PINK, Blocks.PINK_STAINED_GLASS)
            .put(AEColor.GRAY, Blocks.GRAY_STAINED_GLASS).put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_STAINED_GLASS)
            .put(AEColor.CYAN, Blocks.CYAN_STAINED_GLASS).put(AEColor.PURPLE, Blocks.PURPLE_STAINED_GLASS)
            .put(AEColor.BLUE, Blocks.BLUE_STAINED_GLASS).put(AEColor.BROWN, Blocks.BROWN_STAINED_GLASS)
            .put(AEColor.GREEN, Blocks.GREEN_STAINED_GLASS).put(AEColor.RED, Blocks.RED_STAINED_GLASS)
            .put(AEColor.BLACK, Blocks.BLACK_STAINED_GLASS).build());

    private static final BiMap<AEColor, Block> STAINED_GLASS_PANE_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_STAINED_GLASS_PANE)
            .put(AEColor.ORANGE, Blocks.ORANGE_STAINED_GLASS_PANE)
            .put(AEColor.MAGENTA, Blocks.MAGENTA_STAINED_GLASS_PANE)
            .put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)
            .put(AEColor.YELLOW, Blocks.YELLOW_STAINED_GLASS_PANE).put(AEColor.LIME, Blocks.LIME_STAINED_GLASS_PANE)
            .put(AEColor.PINK, Blocks.PINK_STAINED_GLASS_PANE).put(AEColor.GRAY, Blocks.GRAY_STAINED_GLASS_PANE)
            .put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE)
            .put(AEColor.CYAN, Blocks.CYAN_STAINED_GLASS_PANE).put(AEColor.PURPLE, Blocks.PURPLE_STAINED_GLASS_PANE)
            .put(AEColor.BLUE, Blocks.BLUE_STAINED_GLASS_PANE).put(AEColor.BROWN, Blocks.BROWN_STAINED_GLASS_PANE)
            .put(AEColor.GREEN, Blocks.GREEN_STAINED_GLASS_PANE).put(AEColor.RED, Blocks.RED_STAINED_GLASS_PANE)
            .put(AEColor.BLACK, Blocks.BLACK_STAINED_GLASS_PANE).build());

    private static final BiMap<AEColor, Block> WOOL_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_WOOL).put(AEColor.ORANGE, Blocks.ORANGE_WOOL)
            .put(AEColor.MAGENTA, Blocks.MAGENTA_WOOL).put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL)
            .put(AEColor.YELLOW, Blocks.YELLOW_WOOL).put(AEColor.LIME, Blocks.LIME_WOOL)
            .put(AEColor.PINK, Blocks.PINK_WOOL).put(AEColor.GRAY, Blocks.GRAY_WOOL)
            .put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL).put(AEColor.CYAN, Blocks.CYAN_WOOL)
            .put(AEColor.PURPLE, Blocks.PURPLE_WOOL).put(AEColor.BLUE, Blocks.BLUE_WOOL)
            .put(AEColor.BROWN, Blocks.BROWN_WOOL).put(AEColor.GREEN, Blocks.GREEN_WOOL)
            .put(AEColor.RED, Blocks.RED_WOOL).put(AEColor.BLACK, Blocks.BLACK_WOOL).build());

    private static final BiMap<AEColor, Block> BANNER_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_BANNER).put(AEColor.ORANGE, Blocks.ORANGE_BANNER)
            .put(AEColor.MAGENTA, Blocks.MAGENTA_BANNER).put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_BANNER)
            .put(AEColor.YELLOW, Blocks.YELLOW_BANNER).put(AEColor.LIME, Blocks.LIME_BANNER)
            .put(AEColor.PINK, Blocks.PINK_BANNER).put(AEColor.GRAY, Blocks.GRAY_BANNER)
            .put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_BANNER).put(AEColor.CYAN, Blocks.CYAN_BANNER)
            .put(AEColor.PURPLE, Blocks.PURPLE_BANNER).put(AEColor.BLUE, Blocks.BLUE_BANNER)
            .put(AEColor.BROWN, Blocks.BROWN_BANNER).put(AEColor.GREEN, Blocks.GREEN_BANNER)
            .put(AEColor.RED, Blocks.RED_BANNER).put(AEColor.BLACK, Blocks.BLACK_BANNER).build());

    private static final BiMap<AEColor, Block> WALL_BANNER_BY_COLOR = EnumHashBiMap
            .create(ImmutableMap.<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_WALL_BANNER)
                    .put(AEColor.ORANGE, Blocks.ORANGE_WALL_BANNER).put(AEColor.MAGENTA, Blocks.MAGENTA_WALL_BANNER)
                    .put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WALL_BANNER)
                    .put(AEColor.YELLOW, Blocks.YELLOW_WALL_BANNER).put(AEColor.LIME, Blocks.LIME_WALL_BANNER)
                    .put(AEColor.PINK, Blocks.PINK_WALL_BANNER).put(AEColor.GRAY, Blocks.GRAY_WALL_BANNER)
                    .put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WALL_BANNER).put(AEColor.CYAN, Blocks.CYAN_WALL_BANNER)
                    .put(AEColor.PURPLE, Blocks.PURPLE_WALL_BANNER).put(AEColor.BLUE, Blocks.BLUE_WALL_BANNER)
                    .put(AEColor.BROWN, Blocks.BROWN_WALL_BANNER).put(AEColor.GREEN, Blocks.GREEN_WALL_BANNER)
                    .put(AEColor.RED, Blocks.RED_WALL_BANNER).put(AEColor.BLACK, Blocks.BLACK_WALL_BANNER).build());

    private static final BiMap<AEColor, Block> CARPET_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_CARPET).put(AEColor.ORANGE, Blocks.ORANGE_CARPET)
            .put(AEColor.MAGENTA, Blocks.MAGENTA_CARPET).put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_CARPET)
            .put(AEColor.YELLOW, Blocks.YELLOW_CARPET).put(AEColor.LIME, Blocks.LIME_CARPET)
            .put(AEColor.PINK, Blocks.PINK_CARPET).put(AEColor.GRAY, Blocks.GRAY_CARPET)
            .put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_CARPET).put(AEColor.CYAN, Blocks.CYAN_CARPET)
            .put(AEColor.PURPLE, Blocks.PURPLE_CARPET).put(AEColor.BLUE, Blocks.BLUE_CARPET)
            .put(AEColor.BROWN, Blocks.BROWN_CARPET).put(AEColor.GREEN, Blocks.GREEN_CARPET)
            .put(AEColor.RED, Blocks.RED_CARPET).put(AEColor.BLACK, Blocks.BLACK_CARPET).build());

    private static final BiMap<AEColor, Block> TERRACOTTA_BY_COLOR = EnumHashBiMap
            .create(ImmutableMap.<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_TERRACOTTA)
                    .put(AEColor.ORANGE, Blocks.ORANGE_TERRACOTTA).put(AEColor.MAGENTA, Blocks.MAGENTA_TERRACOTTA)
                    .put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_TERRACOTTA).put(AEColor.YELLOW, Blocks.YELLOW_TERRACOTTA)
                    .put(AEColor.LIME, Blocks.LIME_TERRACOTTA).put(AEColor.PINK, Blocks.PINK_TERRACOTTA)
                    .put(AEColor.GRAY, Blocks.GRAY_TERRACOTTA).put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_TERRACOTTA)
                    .put(AEColor.CYAN, Blocks.CYAN_TERRACOTTA).put(AEColor.PURPLE, Blocks.PURPLE_TERRACOTTA)
                    .put(AEColor.BLUE, Blocks.BLUE_TERRACOTTA).put(AEColor.BROWN, Blocks.BROWN_TERRACOTTA)
                    .put(AEColor.GREEN, Blocks.GREEN_TERRACOTTA).put(AEColor.RED, Blocks.RED_TERRACOTTA)
                    .put(AEColor.BLACK, Blocks.BLACK_TERRACOTTA).build());

    private static final BiMap<AEColor, Block> GLAZED_TERRACOTTA_BY_COLOR = EnumHashBiMap.create(ImmutableMap
            .<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_GLAZED_TERRACOTTA)
            .put(AEColor.ORANGE, Blocks.ORANGE_GLAZED_TERRACOTTA).put(AEColor.MAGENTA, Blocks.MAGENTA_GLAZED_TERRACOTTA)
            .put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA)
            .put(AEColor.YELLOW, Blocks.YELLOW_GLAZED_TERRACOTTA).put(AEColor.LIME, Blocks.LIME_GLAZED_TERRACOTTA)
            .put(AEColor.PINK, Blocks.PINK_GLAZED_TERRACOTTA).put(AEColor.GRAY, Blocks.GRAY_GLAZED_TERRACOTTA)
            .put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA)
            .put(AEColor.CYAN, Blocks.CYAN_GLAZED_TERRACOTTA).put(AEColor.PURPLE, Blocks.PURPLE_GLAZED_TERRACOTTA)
            .put(AEColor.BLUE, Blocks.BLUE_GLAZED_TERRACOTTA).put(AEColor.BROWN, Blocks.BROWN_GLAZED_TERRACOTTA)
            .put(AEColor.GREEN, Blocks.GREEN_GLAZED_TERRACOTTA).put(AEColor.RED, Blocks.RED_GLAZED_TERRACOTTA)
            .put(AEColor.BLACK, Blocks.BLACK_GLAZED_TERRACOTTA).build());

    private static final BiMap<AEColor, Block> CONCRETE_BY_COLOR = EnumHashBiMap
            .create(ImmutableMap.<AEColor, Block>builder().put(AEColor.WHITE, Blocks.WHITE_CONCRETE)
                    .put(AEColor.ORANGE, Blocks.ORANGE_CONCRETE).put(AEColor.MAGENTA, Blocks.MAGENTA_CONCRETE)
                    .put(AEColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_CONCRETE).put(AEColor.YELLOW, Blocks.YELLOW_CONCRETE)
                    .put(AEColor.LIME, Blocks.LIME_CONCRETE).put(AEColor.PINK, Blocks.PINK_CONCRETE)
                    .put(AEColor.GRAY, Blocks.GRAY_CONCRETE).put(AEColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_CONCRETE)
                    .put(AEColor.CYAN, Blocks.CYAN_CONCRETE).put(AEColor.PURPLE, Blocks.PURPLE_CONCRETE)
                    .put(AEColor.BLUE, Blocks.BLUE_CONCRETE).put(AEColor.BROWN, Blocks.BROWN_CONCRETE)
                    .put(AEColor.GREEN, Blocks.GREEN_CONCRETE).put(AEColor.RED, Blocks.RED_CONCRETE)
                    .put(AEColor.BLACK, Blocks.BLACK_CONCRETE).build());

    private static final List<RecolorableBlockGroup> BLOCK_GROUPS = ImmutableList.of(
            new RecolorableBlockGroup(Blocks.GLASS, STAINED_GLASS_BY_COLOR),
            new RecolorableBlockGroup(Blocks.GLASS_PANE, STAINED_GLASS_PANE_BY_COLOR),
            new RecolorableBlockGroup(Blocks.WHITE_WOOL, WOOL_BY_COLOR),
            new RecolorableBlockGroup(Blocks.WHITE_BANNER, BANNER_BY_COLOR),
            new RecolorableBlockGroup(Blocks.WHITE_WALL_BANNER, WALL_BANNER_BY_COLOR),
            new RecolorableBlockGroup(Blocks.WHITE_CARPET, CARPET_BY_COLOR),
            new RecolorableBlockGroup(Blocks.TERRACOTTA, TERRACOTTA_BY_COLOR),
            new RecolorableBlockGroup(null, GLAZED_TERRACOTTA_BY_COLOR),
            new RecolorableBlockGroup(null, CONCRETE_BY_COLOR));

    public static Block recolor(Block block, AEColor newColor) {
        Preconditions.checkNotNull(block);

        for (RecolorableBlockGroup group : BLOCK_GROUPS) {
            if (group.uncoloredVariant == block || group.coloredVariants.containsValue(block)) {
                Block newBlock = group.coloredVariants.get(newColor);
                if (newBlock == null) {
                    if (group.uncoloredVariant != null) {
                        newBlock = group.uncoloredVariant;
                    } else {
                        newBlock = block;
                    }
                }
                return newBlock;
            }
        }

        return block;
    }

    private static class RecolorableBlockGroup {

        final Block uncoloredVariant;

        final BiMap<AEColor, Block> coloredVariants;

        public RecolorableBlockGroup(Block uncoloredVariant, BiMap<AEColor, Block> coloredVariants) {
            this.uncoloredVariant = uncoloredVariant;
            this.coloredVariants = coloredVariants;
        }

    }

}
