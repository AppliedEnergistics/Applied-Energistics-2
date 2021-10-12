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
import net.minecraftforge.common.Tags;

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
    public static Tag.Named<Block> CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK = blockTag("forge:storage_blocks/certus_quartz");
    public static Tag.Named<Item> CERTUS_QUARTZ_DUST = tag("forge:dusts/certus_quartz");

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_NETHER_QUARTZ = tag("appliedenergistics2:all_nether_quartz");
    public static Tag.Named<Item> NETHER_QUARTZ = Tags.Items.GEMS_QUARTZ;
    public static Tag.Named<Item> NETHER_QUARTZ_ORE = Tags.Items.ORES_QUARTZ;

    // Includes synthetic/purified
    public static Tag.Named<Item> ALL_FLUIX = tag("appliedenergistics2:all_fluix");
    public static Tag.Named<Item> FLUIX_DUST = tag("forge:dusts/fluix");
    public static Tag.Named<Item> FLUIX_CRYSTAL = tag("forge:gems/fluix");

    public static Tag.Named<Item> GOLD_NUGGET = Tags.Items.NUGGETS_GOLD;
    public static Tag.Named<Item> GOLD_INGOT = Tags.Items.INGOTS_GOLD;
    public static Tag.Named<Item> GOLD_ORE = Tags.Items.ORES_GOLD;

    public static Tag.Named<Item> IRON_NUGGET = Tags.Items.NUGGETS_IRON;
    public static Tag.Named<Item> IRON_INGOT = Tags.Items.INGOTS_IRON;
    public static Tag.Named<Item> IRON_ORE = Tags.Items.ORES_IRON;

    public static Tag.Named<Item> DIAMOND = Tags.Items.GEMS_DIAMOND;
    public static Tag.Named<Item> REDSTONE = Tags.Items.DUSTS_REDSTONE;
    public static Tag.Named<Item> GLOWSTONE = Tags.Items.DUSTS_GLOWSTONE;

    public static Tag.Named<Item> ENDER_PEARL = Tags.Items.ENDER_PEARLS;
    public static Tag.Named<Item> ENDER_PEARL_DUST = tag("forge:dusts/ender");
    public static Tag.Named<Item> WHEAT_CROP = Tags.Items.CROPS_WHEAT;

    public static Tag.Named<Item> WOOD_STICK = Tags.Items.RODS_WOODEN;
    public static Tag.Named<Item> CHEST = Tags.Items.CHESTS_WOODEN;

    public static Tag.Named<Item> STONE = Tags.Items.STONE;
    public static Tag.Named<Item> COBBLESTONE = Tags.Items.COBBLESTONE;
    public static Tag.Named<Item> GLASS = Tags.Items.GLASS;

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

    public static final Tag.Named<Block> STAINED_GLASS_BLOCK = Tags.Blocks.STAINED_GLASS;

    public static final Tag.Named<Block> TERRACOTTA_BLOCK = blockTag("forge:terracotta");

    public static final Tag.Named<Item> STORAGE_BLOCKS = Tags.Items.STORAGE_BLOCKS;

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
