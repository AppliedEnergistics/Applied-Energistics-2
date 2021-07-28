package appeng.api.ids;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

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
     * Contains only the naturally obtained certus crystal (and equivalent).
     */
    public static final Tag.Named<Item> NATURAL_CERTUS_QUARTZ_CRYSTAL = ItemTags
            .bind("appliedenergistics2:crystals/certus_quartz");

    /**
     * Contains nether quartz crystals recognized by AE2.
     * <ul>
     * <li>Forge Nether Quartz Tag</li>
     * <li>Pure Nether Quartz Crystals</li>
     * </ul>
     */
    public static final Tag.Named<Item> NETHER_QUARTZ_CRYSTALS = ItemTags.bind("appliedenergistics2:crystals/nether");

    /**
     * Contains both nether and certus quartz dust.
     */
    public static final Tag.Named<Item> QUARTZ_DUST = ItemTags.bind("appliedenergistics2:dusts/quartz");

    /**
     * Contains various sources of glass.
     */
    public static final Tag.Named<Item> GLASS = ItemTags.bind("appliedenergistics2:glass");

}
