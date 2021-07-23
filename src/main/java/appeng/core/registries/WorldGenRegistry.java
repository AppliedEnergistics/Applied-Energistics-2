/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.core.registries;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import appeng.api.features.IWorldGen;

public final class WorldGenRegistry implements IWorldGen {

    public static final WorldGenRegistry INSTANCE = new WorldGenRegistry();

    private final Map<WorldGenType, TypeSet> settings = new EnumMap<>(WorldGenType.class);

    public WorldGenRegistry() {
        for (WorldGenType type : WorldGenType.values()) {
            settings.put(type, new TypeSet());
        }
    }

    @Override
    public boolean isWorldGenEnabled(WorldGenType type, ServerLevel w) {
        return true;
    }

    @Override
    public void disableWorldGenForBiome(final WorldGenType type, final ResourceLocation biomeId) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(biomeId);

        settings.get(type).modBiomeBlacklist.add(biomeId);
    }

    @Override
    public boolean isWorldGenDisabledForBiome(WorldGenType type, ResourceLocation biomeId) {
        TypeSet typeSettings = settings.get(type);
        return typeSettings.configBiomeBlacklist.contains(biomeId)
                || typeSettings.modBiomeBlacklist.contains(biomeId);
    }

    public void setConfigBlacklists(
            List<ResourceLocation> quartzBiomeBlacklist,
            List<ResourceLocation> meteoriteBiomeBlacklist) {
        settings.get(WorldGenType.CERTUS_QUARTZ).configBiomeBlacklist.clear();
        settings.get(WorldGenType.CERTUS_QUARTZ).configBiomeBlacklist.addAll(quartzBiomeBlacklist);
        settings.get(WorldGenType.CHARGED_CERTUS_QUARTZ).configBiomeBlacklist.clear();
        settings.get(WorldGenType.CHARGED_CERTUS_QUARTZ).configBiomeBlacklist.addAll(quartzBiomeBlacklist);
        settings.get(WorldGenType.METEORITES).configBiomeBlacklist.clear();
        settings.get(WorldGenType.METEORITES).configBiomeBlacklist.addAll(meteoriteBiomeBlacklist);
    }

    private static class TypeSet {
        /**
         * Biomes blacklisted by other mods.
         */
        final Set<ResourceLocation> modBiomeBlacklist = new HashSet<>();
        /**
         * Biomes blacklisted in the user's config.
         */
        final Set<ResourceLocation> configBiomeBlacklist = new HashSet<>();
    }
}
