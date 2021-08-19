package appeng.api.ids;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class AETags {

    private AETags() {
    }

    /**
     * Contains all of AE2's certus crystals, such as:
     * <ul>
     * <li>Certus Quartz Crystals</li>
     * <li>Charged Certus Quartz Crystals</li>
     * <li>Pure Certus Quartz Crystals</li>
     * </ul>
     */
    public static final Tag.Named<Item> CERTUS_QUARTZ_CRYSTAL = ItemTags.bind("appliedenergistics2:crystals/certus");

    /**
     * Contains blocks that are blacklisted from being moved in and out of spatial storage.
     * <p/>
     * To blacklist block entities from being moved, you need to add the hosting block to this tag.
     */
    public static final Tag.Named<Block> SPATIAL_BLACKLIST = BlockTags.bind("appliedenergistics2:blacklisted/spatial");

    /**
     * Contains blocks that are blacklisted from being picked up by an annihilation plane.
     */
    public static final Tag.Named<Block> ANNIHILATION_PLANE_BLACKLIST = BlockTags
            .bind("appliedenergistics2:blacklisted/annihilation_plane");

}
