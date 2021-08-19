package appeng.api.ids;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
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
    public static final Tag.Named<Block> SPATIAL_BLACKLIST = BlockTags.bind("appliedenergistics2:blacklisted/spatial");

    /**
     * Contains blocks that are blacklisted from being picked up by an item annihilation plane.
     */
    public static final Tag.Named<Block> ANNIHILATION_PLANE_BLOCK_BLACKLIST = BlockTags
            .bind("appliedenergistics2:blacklisted/annihilation_plane");

    /**
     * Contains items that are blacklisted from being picked up by an item annihilation plane.
     */
    public static final Tag.Named<Item> ANNIHILATION_PLANE_ITEM_BLACKLIST = ItemTags
            .bind("appliedenergistics2:blacklisted/annihilation_plane");

    /**
     * Contains items that are blacklisted from being picked up by a fluid annihilation plane.
     */
    public static final Tag.Named<Fluid> ANNIHILATION_PLANE_FLUID_BLACKLIST = FluidTags
            .bind("appliedenergistics2:blacklisted/annihilation_plane");

    /**
     * Used by the quartz knife to decide which ingots can be crafted into nameplates, as well as the crafting recipe
     * for cable anchors.
     */
    public static Tag.Named<Item> METAL_INGOTS = ItemTags.bind("appliedenergistics2:metal_ingots");

    /**
     * Block tag used to explicitly whitelist blocks for use in facades, even if they don't meet the general criteria
     * for being used in facades.
     */
    public static final Tag.Named<Block> FACADE_BLOCK_WHITELIST = BlockTags
            .bind("appliedenergistics2:whitelisted/facades");

}
