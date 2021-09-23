/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.features;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import net.minecraft.resources.ResourceLocation;

/**
 * Allows other mods to interact with AE2's world generation.
 * <p/>
 * This class is thread-safe and may be used in your mod's constructor.
 */
@ThreadSafe
public final class AEWorldGen {

    private static final Map<AEWorldGenType, TypeSet> settings = new EnumMap<>(AEWorldGenType.class);

    static {
        for (AEWorldGenType type : AEWorldGenType.values()) {
            settings.put(type, new TypeSet());
        }
    }

    private AEWorldGen() {
    }

    /**
     * Forces a given AE2 world-generation type to be disabled for a given biome.
     */
    public synchronized static void disableWorldGenForBiome(AEWorldGenType type, ResourceLocation biomeId) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(biomeId);

        settings.get(type).modBiomeBlacklist.add(biomeId);
    }

    /**
     * Checks if the given world-generation type is disabled for the given biome id.
     * <p>
     * This also takes AE2's configuration file into account.
     */
    public synchronized static boolean isWorldGenDisabledForBiome(AEWorldGenType type, ResourceLocation biomeId) {
        TypeSet typeSettings = settings.get(type);
        return typeSettings.configBiomeBlacklist.contains(biomeId)
                || typeSettings.modBiomeBlacklist.contains(biomeId);
    }

    /**
     * This is used by AE2 to set the biome blacklist from AE2's own configuration file.
     */
    synchronized static void setConfigBlacklists(
            List<ResourceLocation> quartzBiomeBlacklist,
            List<ResourceLocation> meteoriteBiomeBlacklist) {
        settings.get(AEWorldGenType.CERTUS_QUARTZ).configBiomeBlacklist.clear();
        settings.get(AEWorldGenType.CERTUS_QUARTZ).configBiomeBlacklist.addAll(quartzBiomeBlacklist);
        settings.get(AEWorldGenType.METEORITES).configBiomeBlacklist.clear();
        settings.get(AEWorldGenType.METEORITES).configBiomeBlacklist.addAll(meteoriteBiomeBlacklist);
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
