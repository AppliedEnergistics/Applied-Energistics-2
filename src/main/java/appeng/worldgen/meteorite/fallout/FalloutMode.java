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

package appeng.worldgen.meteorite.fallout;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;

public enum FalloutMode {

    /**
     * No fallout, e.g. when without a crater.
     */
    NONE,

    /**
     * Default
     */
    DEFAULT,

    /**
     * For sandy terrain
     */
    SAND(Tags.Biomes.IS_SANDY, BiomeTags.IS_BEACH),

    /**
     * For terracotta (mesa)
     */
    TERRACOTTA(BiomeTags.IS_BADLANDS),

    /**
     * Icy/snowy terrain
     */
    ICE_SNOW(Tags.Biomes.IS_COLD);

    private final List<TagKey<Biome>> biomeTags;

    @SafeVarargs
    FalloutMode(TagKey<Biome>... biomeTags) {
        this.biomeTags = ImmutableList.copyOf(biomeTags);
    }

    public boolean matches(Holder<Biome> biome) {
        for (var biomeTag : biomeTags) {
            if (biome.is(biomeTag)) {
                return true;
            }
        }
        return false;
    }

    public static FalloutMode fromBiome(Holder<Biome> biome) {
        for (var mode : FalloutMode.values()) {
            if (mode.matches(biome)) {
                return mode;
            }
        }

        return DEFAULT;
    }
}
