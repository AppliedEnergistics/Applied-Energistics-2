/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public interface IWorldGen {

    /**
     * This method does not do anything since Worldgen is centered around biomes, and not dimensions.
     * <p>
     *
     * @deprecated Scheduled for removal in 9.0.0.
     */
    @Deprecated
    boolean isWorldGenEnabled(WorldGenType type, ServerLevel w);

    /**
     * Forces a given AE2 world-generation type to be disabled for a given biome.
     */
    void disableWorldGenForBiome(WorldGenType type, ResourceLocation biomeId);

    /**
     * Checks if the given world-generation type is disabled for the given biome id.
     * <p>
     * This also takes AE2's configuration file into account.
     */
    boolean isWorldGenDisabledForBiome(WorldGenType type, net.minecraft.resources.ResourceLocation biomeId);

    enum WorldGenType {
        CERTUS_QUARTZ,
        CHARGED_CERTUS_QUARTZ,
        METEORITES
    }
}
