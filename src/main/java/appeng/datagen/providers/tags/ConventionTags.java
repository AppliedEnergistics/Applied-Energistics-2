package appeng.datagen.providers.tags;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Contains various tags:
 * <ul>
 * <li>Convention tags defined by the modding API for mod-compatibility purposes.</li>
 * <li>Tags defined by AE2 itself for recipe use.</li>
 * <li>Tags provided by AE2 for mod compatibility in the convention namespace.</li>
 * </ul>
 */
public final class ConventionTags {

    private ConventionTags() {
    }

    public static Tag.Named<Item> SILICON = tag("forge:silicon");

    // Includes purified versions of certus/nether and the natural ones
    public static Tag.Named<Item> ALL_QUARTZ = tag("appliedenergistics2:all_quartz");
    // Includes both certus/nether quartz dust
    public static Tag.Named<Item> ALL_QUARTZ_DUST = tag("appliedenergistics2:all_quartz_dust");

    // Includes charged, synthetic/purified and natural certus quartz
    public static Tag.Named<Item> ALL_CERTUS_QUARTZ = tag("appliedenergistics2:all_certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ = tag("forge:gems/certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_ORE = tag("forge:ores/certus_quartz");
    public static Tag.Named<Block> CERTUS_QUARTZ_ORE_BLOCK = blockTag("forge:ores/certus_quartz");
    public static Tag.Named<Block> CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK = blockTag("forge:ores/certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_DUST = tag("forge:dusts/certus_quartz");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_NETHER_QUARTZ = tag("appliedenergistics2:all_nether_quartz");
    public static Tag.Named<Item> NETHER_QUARTZ = tag("forge:gems/quartz");
    public static Tag.Named<Item> NETHER_QUARTZ_ORE = tag("forge:ores/quartz");
    public static Tag.Named<Item> NETHER_QUARTZ_DUST = tag("forge:dusts/quartz");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_FLUIX = tag("appliedenergistics2:all_fluix");
    public static Tag.Named<Item> FLUIX_DUST = tag("forge:dusts/fluix");
    public static Tag.Named<Item> FLUIX_CRYSTAL = tag("forge:gems/fluix");

    public static Tag.Named<Item> GOLD_INGOT = tag("forge:ingots/gold");
    public static Tag.Named<Item> GOLD_ORE = tag("forge:ores/gold");
    public static Tag.Named<Item> GOLD_DUST = tag("forge:dusts/gold");

    public static Tag.Named<Item> IRON_INGOT = tag("forge:ingots/iron");
    public static Tag.Named<Item> IRON_ORE = tag("forge:ores/iron");
    public static Tag.Named<Item> IRON_DUST = tag("forge:dusts/iron");

    public static Tag.Named<Item> DIAMOND = tag("forge:gems/diamond");
    public static Tag.Named<Item> REDSTONE = tag("forge:dusts/redstone");
    public static Tag.Named<Item> GLOWSTONE = tag("forge:dusts/glowstone");

    public static Tag.Named<Item> ENDER_PEARL = tag("forge:ender_pearls");
    public static Tag.Named<Item> ENDER_PEARL_DUST = tag("forge:dusts/ender");
    public static Tag.Named<Item> WHEAT_CROP = tag("forge:crops/wheat");

    public static Tag.Named<Item> WOOD_STICK = tag("forge:rods/wooden");
    public static Tag.Named<Item> WOOD_GEAR = tag("forge:gears/wooden");
    public static Tag.Named<Item> CHEST = tag("forge:blocks/chests/wooden");

    public static Tag.Named<Item> STONE = tag("forge:stone");
    public static Tag.Named<Item> COBBLESTONE = tag("forge:cobblestone");
    public static Tag.Named<Item> GLASS = tag("forge:glass");

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
    public static Tag.Named<Item> PAINT_BALLS = tag("appliedenergistics2:paint_balls");

    public static final Map<DyeColor, Tag.Named<Item>> DYES = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(
                    Function.identity(),
                    dye -> tag("forge:dyes/" + dye.getSerializedName())));

    public static final Tag.Named<Block> TERRACOTTA_BLOCK = blockTag("forge:terracotta");

    public static Tag.Named<Item> dye(DyeColor color) {
        return DYES.get(color);
    }

    private static Tag.Named<Item> tag(String name) {
        return ItemTags.bind(name);
    }

    private static Tag.Named<Block> blockTag(String name) {
        return BlockTags.bind(name);
    }

}
