/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

    public static Tag.Named<Item> SILICON = tag("c:silicon");

    // Includes purified versions of certus/nether and the natural ones
    public static Tag.Named<Item> ALL_QUARTZ = tag("appliedenergistics2:all_quartz");
    // Includes both certus/nether quartz dust
    public static Tag.Named<Item> ALL_QUARTZ_DUST = tag("appliedenergistics2:all_quartz_dust");

    // Includes charged, synthetic/purified and natural certus quartz
    public static Tag.Named<Item> ALL_CERTUS_QUARTZ = tag("appliedenergistics2:all_certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ = tag("c:certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_ORE = tag("c:certus_quartz_ores");
    public static Tag.Named<Block> CERTUS_QUARTZ_ORE_BLOCK = blockTag("c:certus_quartz_ores");
    public static Tag.Named<Block> CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK = blockTag("c:certus_quartz_blocks");
    public static Tag.Named<Item> CERTUS_QUARTZ_DUST = tag("c:certus_quartz_dusts");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_NETHER_QUARTZ = tag("appliedenergistics2:all_nether_quartz");
    public static Tag.Named<Item> NETHER_QUARTZ = tag("c:quartz");
    public static Tag.Named<Item> NETHER_QUARTZ_ORE = tag("c:quartz_ores");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_FLUIX = tag("appliedenergistics2:all_fluix");
    public static Tag.Named<Item> FLUIX_DUST = tag("c:fluix_dusts");
    public static Tag.Named<Item> FLUIX_CRYSTAL = tag("c:fluix");

    public static Tag.Named<Item> GOLD_INGOT = tag("c:gold_ingots");
    public static Tag.Named<Item> GOLD_ORE = tag("c:gold_ores");

    public static Tag.Named<Item> IRON_INGOT = tag("c:iron_ingots");
    public static Tag.Named<Item> IRON_ORE = tag("c:iron_ores");

    public static Tag.Named<Item> DIAMOND = tag("c:diamonds");
    public static Tag.Named<Item> REDSTONE = tag("c:redstone_dusts");
    public static Tag.Named<Item> GLOWSTONE = tag("c:glowstone_dusts");

    public static Tag.Named<Item> ENDER_PEARL = tag("c:ender_pearls");
    public static Tag.Named<Item> ENDER_PEARL_DUST = tag("c:ender_pearl_dusts");
    public static Tag.Named<Item> WHEAT_CROP = tag("c:wheat_crops");

    public static Tag.Named<Item> WOOD_STICK = tag("c:wooden_rods");
    public static Tag.Named<Item> CHEST = tag("c:wooden_chests");

    public static Tag.Named<Item> STONE = tag("c:stone");
    public static Tag.Named<Item> COBBLESTONE = tag("c:cobblestone");
    public static Tag.Named<Item> GLASS = tag("c:glass");

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

    /**
     * Used to identify items that act as wrenches.
     */
    public static final Tag.Named<Item> WRENCH = tag("c:wrench");

    public static final Map<DyeColor, Tag.Named<Item>> DYES = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(
                    Function.identity(),
                    dye -> tag("c:" + dye.getSerializedName() + "_dye")));

    public static final Tag.Named<Block> STAINED_GLASS_BLOCK = blockTag("c:stained_glass");

    public static final Tag.Named<Block> TERRACOTTA_BLOCK = blockTag("c:terracotta");

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
