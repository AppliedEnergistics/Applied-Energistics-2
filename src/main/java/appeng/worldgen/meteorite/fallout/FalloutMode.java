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

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

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
    SAND(ConventionalBiomeTags.DESERT, ConventionalBiomeTags.BEACH),

    /**
     * For terracotta (mesa)
     */
    TERRACOTTA(ConventionalBiomeTags.MESA),

    /**
     * Icy/snowy terrain
     */
    ICE_SNOW(ConventionalBiomeTags.SNOWY, ConventionalBiomeTags.ICY);

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
