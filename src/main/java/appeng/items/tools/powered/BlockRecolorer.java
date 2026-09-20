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
import java.util.Objects;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ImmutableList;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColorCollection;

import appeng.api.util.AEColor;

/**
 * Allows recoloring a variety of vanilla blocks.
 */
public final class BlockRecolorer {

    private BlockRecolorer() {
    }

    // Minecraft no longer exposes the 16 dyed variants as individual Blocks fields
    // (Blocks.WHITE_WOOL, ...). They are now grouped into ColorCollection<Block> (Blocks.WOOL, ...),
    // so the per-color maps are derived from those collections instead of being spelled out.
    private static BiMap<AEColor, Block> byColor(ColorCollection<Block> variants) {
        var map = EnumHashBiMap.<AEColor, Block>create(AEColor.class);
        for (var color : AEColor.VALID_COLORS) {
            map.put(color, variants.pick(color.dye));
        }
        return map;
    }

    private static final BiMap<AEColor, Block> STAINED_GLASS_BY_COLOR = byColor(Blocks.STAINED_GLASS);

    private static final BiMap<AEColor, Block> STAINED_GLASS_PANE_BY_COLOR = byColor(Blocks.STAINED_GLASS_PANE);

    private static final BiMap<AEColor, Block> WOOL_BY_COLOR = byColor(Blocks.WOOL);

    private static final BiMap<AEColor, Block> BANNER_BY_COLOR = byColor(Blocks.BANNER);

    private static final BiMap<AEColor, Block> WALL_BANNER_BY_COLOR = byColor(Blocks.WALL_BANNER);

    private static final BiMap<AEColor, Block> CARPET_BY_COLOR = byColor(Blocks.CARPET);

    private static final BiMap<AEColor, Block> TERRACOTTA_BY_COLOR = byColor(Blocks.DYED_TERRACOTTA);

    private static final BiMap<AEColor, Block> GLAZED_TERRACOTTA_BY_COLOR = byColor(Blocks.GLAZED_TERRACOTTA);

    private static final BiMap<AEColor, Block> CONCRETE_BY_COLOR = byColor(Blocks.CONCRETE);

    private static final List<RecolorableBlockGroup> BLOCK_GROUPS = ImmutableList.of(
            new RecolorableBlockGroup(Blocks.GLASS, STAINED_GLASS_BY_COLOR),
            new RecolorableBlockGroup(Blocks.GLASS_PANE, STAINED_GLASS_PANE_BY_COLOR),
            new RecolorableBlockGroup(Blocks.WOOL.white(), WOOL_BY_COLOR),
            new RecolorableBlockGroup(Blocks.BANNER.white(), BANNER_BY_COLOR),
            new RecolorableBlockGroup(Blocks.WALL_BANNER.white(), WALL_BANNER_BY_COLOR),
            new RecolorableBlockGroup(Blocks.CARPET.white(), CARPET_BY_COLOR),
            new RecolorableBlockGroup(Blocks.TERRACOTTA, TERRACOTTA_BY_COLOR),
            new RecolorableBlockGroup(null, GLAZED_TERRACOTTA_BY_COLOR),
            new RecolorableBlockGroup(null, CONCRETE_BY_COLOR));

    public static Block recolor(Block block, AEColor newColor) {
        Objects.requireNonNull(block);

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
