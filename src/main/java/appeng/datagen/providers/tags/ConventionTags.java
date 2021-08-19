package appeng.datagen.providers.tags;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public final class ConventionTags {

    private ConventionTags() {
    }

    public static Tag.Named<Item> CHEST = itemConvention("blocks/chests/wooden");

    // Includes purified versions of certus/nether and the natural ones
    public static Tag.Named<Item> ALL_QUARTZ = tag("appliedenergistics2:all_quartz");
    // Includes both certus/nether quartz dust
    public static Tag.Named<Item> ALL_QUARTZ_DUST = tag("appliedenergistics2:all_quartz_dust");

    // Includes charged, synthetic/purified
    public static Tag.Named<Item> ALL_CERTUS_QUARTZ = tag("appliedenergistics2:all_certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ = itemConvention("gems/certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_ORE = itemConvention("ores/certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_DUST = itemConvention("dusts/certus_quartz");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_NETHER_QUARTZ = tag("appliedenergistics2:all_nether_quartz");
    public static Tag.Named<Item> NETHER_QUARTZ = itemConvention("gems/quartz");
    public static Tag.Named<Item> NETHER_QUARTZ_ORE = itemConvention("ores/quartz");
    public static Tag.Named<Item> NETHER_QUARTZ_DUST = itemConvention("dusts/quartz");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_FLUIX = tag("appliedenergistics2:all_fluix");
    public static Tag.Named<Item> FLUIX_DUST = itemConvention("dusts/fluix");

    public static Tag.Named<Item> GOLD_INGOT = itemConvention("ingots/gold");
    public static Tag.Named<Item> GOLD_ORE = itemConvention("ores/gold");
    public static Tag.Named<Item> GOLD_DUST = itemConvention("dusts/gold");

    public static Tag.Named<Item> IRON_INGOT = itemConvention("ingots/iron");
    public static Tag.Named<Item> IRON_ORE = itemConvention("ores/iron");
    public static Tag.Named<Item> IRON_DUST = itemConvention("dusts/iron");

    public static Tag.Named<Item> DIAMOND = itemConvention("gems/diamond");
    public static Tag.Named<Item> REDSTONE = itemConvention("dusts/redstone");
    public static Tag.Named<Item> GLOWSTONE = itemConvention("dusts/glowstone");

    public static Tag.Named<Item> ENDER_PEARL = itemConvention("ender_pearls");
    public static Tag.Named<Item> ENDER_PEARL_DUST = itemConvention("dusts/ender");
    public static Tag.Named<Item> WHEAT_CROP = itemConvention("crops/wheat");

    public static Tag.Named<Item> WOOD_STICK = itemConvention("rods/wooden");
    public static Tag.Named<Item> WOOD_GEAR = itemConvention("gears/wooden");

    public static Tag.Named<Item> STONE = itemConvention("stone");
    public static Tag.Named<Item> COBBLESTONE = itemConvention("cobblestone");
    public static Tag.Named<Item> GLASS = itemConvention("glass");

    public static Tag.Named<Item> GLASS_CABLE = tag("appliedenergistics2:glass_cable");
    public static Tag.Named<Item> SMART_CABLE = tag("appliedenergistics2:smart_cable");
    public static Tag.Named<Item> COVERED_CABLE = tag("appliedenergistics2:covered_cable");
    public static Tag.Named<Item> COVERED_DENSE_CABLE = tag("appliedenergistics2:covered_dense_cable");
    public static Tag.Named<Item> SMART_DENSE_CABLE = tag("appliedenergistics2:smart_dense_cable");
    public static Tag.Named<Item> ILLUMINATED_PANEL = tag("appliedenergistics2:illuminated_panel");
    public static Tag.Named<Item> ITEM_INTERFACE = tag("appliedenergistics2:item_interface");
    public static Tag.Named<Item> FLUID_INTERFACE = tag("appliedenergistics2:fluid_interface");
    public static Tag.Named<Item> QUARTZ_WRENCH = tag("appliedenergistics2:quartz_wrench");
    public static Tag.Named<Item> QUARTZ_KNIFE = tag("appliedenergistics2:knife");
    public static Tag.Named<Item> METAL_INGOTS = tag("appliedenergistics2:metal_ingots");
    public static Tag.Named<Item> SILICON = itemConvention("silicon");
    public static Tag.Named<Item> PAINT_BALLS = tag("appliedenergistics2:paint_balls");

    public static final Map<DyeColor, Tag.Named<Item>> DYES = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(
                    Function.identity(),
                    dye -> itemConvention("dyes/" + dye.getSerializedName())));

    public static Tag.Named<Item> dye(DyeColor color) {
        return DYES.get(color);
    }

    private static Tag.Named<Item> tag(String name) {
        return ItemTags.bind(name);
    }

    private static Tag.Named<Item> itemConvention(String name) {
        return ItemTags.bind("forge:" + name);
    }

}
