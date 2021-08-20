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

package appeng.api.ids;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * Tags that AE uses for functional purposes. For recipe tags that you may use in your recipe data generation, please
 * see the non-api class ConventionTags.
 */
public final class AETags {

    private AETags() {
    }

    /**
     * Contains blocks that are blacklisted from being moved in and out of spatial storage.
     * <p/>
     * To blacklist block entities from being moved, you need to add the hosting block to this tag.
     */
    public static final Tag.Named<Block> SPATIAL_BLACKLIST = blockTag("appliedenergistics2:blacklisted/spatial");

    /**
     * Contains blocks that are blacklisted from being picked up by an item annihilation plane.
     */
    public static final Tag.Named<Block> ANNIHILATION_PLANE_BLOCK_BLACKLIST = blockTag(
            "appliedenergistics2:blacklisted/annihilation_plane");

    /**
     * Contains items that are blacklisted from being picked up by an item annihilation plane.
     */
    public static final Tag.Named<Item> ANNIHILATION_PLANE_ITEM_BLACKLIST = itemTag(
            "appliedenergistics2:blacklisted/annihilation_plane");

    /**
     * Contains items that are blacklisted from being picked up by a fluid annihilation plane.
     */
    public static final Tag.Named<Fluid> ANNIHILATION_PLANE_FLUID_BLACKLIST = fluidTag(
            "appliedenergistics2:blacklisted/annihilation_plane");

    /**
     * Used by the quartz knife to decide which ingots can be crafted into nameplates, as well as the crafting recipe
     * for cable anchors.
     */
    public static Tag.Named<Item> METAL_INGOTS = itemTag("appliedenergistics2:metal_ingots");

    /**
     * Block tag used to explicitly whitelist blocks for use in facades, even if they don't meet the general criteria
     * for being used in facades.
     */
    public static final Tag.Named<Block> FACADE_BLOCK_WHITELIST = blockTag("appliedenergistics2:whitelisted/facades");

    private static Tag.Named<Item> itemTag(String name) {
        return TagFactory.ITEM.create(new ResourceLocation(name));
    }

    private static Tag.Named<Fluid> fluidTag(String name) {
        return TagFactory.FLUID.create(new ResourceLocation(name));
    }

    private static Tag.Named<Block> blockTag(String name) {
        return TagFactory.BLOCK.create(new ResourceLocation(name));
    }

}
