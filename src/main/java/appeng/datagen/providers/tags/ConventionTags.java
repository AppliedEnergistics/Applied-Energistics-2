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

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.resources.ResourceLocation;
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
    public static Tag.Named<Item> ALL_QUARTZ = tag("ae2:all_quartz");
    // Includes both certus/nether quartz dust
    public static Tag.Named<Item> ALL_QUARTZ_DUST = tag("ae2:all_quartz_dust");

    // Includes charged, synthetic/purified and natural certus quartz
    public static Tag.Named<Item> ALL_CERTUS_QUARTZ = tag("ae2:all_certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ = tag("c:certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_ORE = tag("c:certus_quartz_ores");
    public static Tag.Named<Block> CERTUS_QUARTZ_ORE_BLOCK = blockTag("c:certus_quartz_ores");
    public static Tag.Named<Block> CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK = blockTag("c:certus_quartz_blocks");
    public static Tag.Named<Item> CERTUS_QUARTZ_DUST = tag("c:certus_quartz_dusts");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_NETHER_QUARTZ = tag("ae2:all_nether_quartz");
    public static Tag.Named<Item> NETHER_QUARTZ = tag("c:quartz");
    public static Tag.Named<Item> NETHER_QUARTZ_ORE = tag("c:quartz_ores");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_FLUIX = tag("ae2:all_fluix");
    public static Tag.Named<Item> FLUIX_DUST = tag("c:fluix_dusts");
    public static Tag.Named<Item> FLUIX_CRYSTAL = tag("c:fluix");

    public static Tag.Named<Item> COPPER_INGOT = tag("c:copper_ingots");

    public static Tag.Named<Item> GOLD_NUGGET = tag("c:gold_nuggets");
    public static Tag.Named<Item> GOLD_INGOT = tag("c:gold_ingots");
    public static Tag.Named<Item> GOLD_ORE = tag("c:gold_ores");

    public static Tag.Named<Item> IRON_NUGGET = tag("c:iron_nuggets");
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

    public static Tag.Named<Item> GLASS_CABLE = tag("ae2:glass_cable");
    public static Tag.Named<Item> SMART_CABLE = tag("ae2:smart_cable");
    public static Tag.Named<Item> COVERED_CABLE = tag("ae2:covered_cable");
    public static Tag.Named<Item> COVERED_DENSE_CABLE = tag("ae2:covered_dense_cable");
    public static Tag.Named<Item> SMART_DENSE_CABLE = tag("ae2:smart_dense_cable");
    public static Tag.Named<Item> ILLUMINATED_PANEL = tag("ae2:illuminated_panel");
    public static Tag.Named<Item> INTERFACE = tag("ae2:interface");
    public static Tag.Named<Item> PATTERN_PROVIDER = tag("ae2:pattern_provider");
    public static Tag.Named<Item> QUARTZ_WRENCH = tag("ae2:quartz_wrench");
    public static Tag.Named<Item> QUARTZ_KNIFE = tag("ae2:knife");
    public static Tag.Named<Item> PAINT_BALLS = tag("ae2:paint_balls");

    /**
     * Used to identify items that act as wrenches.
     */
    public static final Tag.Named<Item> WRENCH = tag("c:wrenches");

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
        return TagFactory.ITEM.create(new ResourceLocation(name));
    }

    private static Tag.Named<Block> blockTag(String name) {
        return TagFactory.BLOCK.create(new ResourceLocation(name));
    }

}
