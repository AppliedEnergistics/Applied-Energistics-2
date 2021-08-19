package appeng.datagen.providers.tags;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

public final class ConventionTags {

    private ConventionTags() {
    }

    public static Tag.Named<Item> CERTUS_QUARTZ = itemConvention("gems/certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_ORE = itemConvention("ores/certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_DUST = itemConvention("dusts/certus_quartz");

    public static Tag.Named<Item> GOLD_INGOT = itemConvention("ingots/gold");
    public static Tag.Named<Item> GOLD_ORE = itemConvention("ores/gold");
    public static Tag.Named<Item> GOLD_DUST = itemConvention("dusts/gold");

    public static Tag.Named<Item> IRON_INGOT = itemConvention("ingots/iron");
    public static Tag.Named<Item> IRON_ORE = itemConvention("ores/iron");
    public static Tag.Named<Item> IRON_DUST = itemConvention("dusts/iron");

    public static Tag.Named<Item> NETHER_QUARTZ = itemConvention("gems/quartz");
    public static Tag.Named<Item> NETHER_QUARTZ_ORE = itemConvention("ores/quartz");
    public static Tag.Named<Item> NETHER_QUARTZ_DUST = itemConvention("dusts/quartz");

    public static Tag.Named<Item> DIAMOND = itemConvention("gems/diamond");
    public static Tag.Named<Item> REDSTONE = itemConvention("dusts/redstone");
    public static Tag.Named<Item> GLOWSTONE = itemConvention("dusts/glowstone");

    public static Tag.Named<Item> GLASS = itemConvention("blocks/glass");
    public static Tag.Named<Item> ENDER_PEARL = itemConvention("ender_pearls");
    public static Tag.Named<Item> WHEAT_CROP = itemConvention("crops/wheat");

    private static Tag.Named<Item> itemConvention(String name) {
        return ItemTags.bind("forge:" + name);
    }

}
