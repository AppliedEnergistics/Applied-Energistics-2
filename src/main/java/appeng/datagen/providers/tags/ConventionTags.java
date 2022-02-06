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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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

    public static TagKey<Item> SILICON = tag("forge:silicon");

    // Includes purified versions of certus/nether and the natural ones
    public static TagKey<Item> ALL_QUARTZ = tag("ae2:all_quartz");
    // Includes both certus/nether quartz dust
    public static TagKey<Item> ALL_QUARTZ_DUST = tag("ae2:all_quartz_dust");

    // Includes charged, synthetic/purified and natural certus quartz
    public static TagKey<Item> ALL_CERTUS_QUARTZ = tag("ae2:all_certus_quartz");
    public static TagKey<Item> CERTUS_QUARTZ = tag("forge:gems/certus_quartz");
    public static TagKey<Item> CERTUS_QUARTZ_ORE = tag("forge:ores/certus_quartz");
    public static TagKey<Block> CERTUS_QUARTZ_ORE_BLOCK = blockTag("forge:ores/certus_quartz");
    public static TagKey<Block> CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK = blockTag("forge:storage_blocks/certus_quartz");
    public static TagKey<Item> CERTUS_QUARTZ_DUST = tag("forge:dusts/certus_quartz");

    // Includes synthetic/purified
    public static TagKey<Item> ALL_NETHER_QUARTZ = tag("ae2:all_nether_quartz");
    public static TagKey<Item> NETHER_QUARTZ = Tags.Items.GEMS_QUARTZ;
    public static TagKey<Item> NETHER_QUARTZ_ORE = Tags.Items.ORES_QUARTZ;

    // Includes synthetic/purified
    public static TagKey<Item> ALL_FLUIX = tag("ae2:all_fluix");
    public static TagKey<Item> FLUIX_DUST = tag("forge:dusts/fluix");
    public static TagKey<Item> FLUIX_CRYSTAL = tag("forge:gems/fluix");

    public static TagKey<Item> COPPER_INGOT = tag("forge:ingots/copper");

    public static TagKey<Item> GOLD_NUGGET = Tags.Items.NUGGETS_GOLD;
    public static TagKey<Item> GOLD_INGOT = Tags.Items.INGOTS_GOLD;
    public static TagKey<Item> GOLD_ORE = Tags.Items.ORES_GOLD;

    public static TagKey<Item> IRON_NUGGET = Tags.Items.NUGGETS_IRON;
    public static TagKey<Item> IRON_INGOT = Tags.Items.INGOTS_IRON;
    public static TagKey<Item> IRON_ORE = Tags.Items.ORES_IRON;

    public static TagKey<Item> DIAMOND = Tags.Items.GEMS_DIAMOND;
    public static TagKey<Item> REDSTONE = Tags.Items.DUSTS_REDSTONE;
    public static TagKey<Item> GLOWSTONE = Tags.Items.DUSTS_GLOWSTONE;

    public static TagKey<Item> ENDER_PEARL = Tags.Items.ENDER_PEARLS;
    public static TagKey<Item> ENDER_PEARL_DUST = tag("forge:dusts/ender");
    public static TagKey<Item> WHEAT_CROP = Tags.Items.CROPS_WHEAT;

    public static TagKey<Item> WOOD_STICK = Tags.Items.RODS_WOODEN;
    public static TagKey<Item> CHEST = Tags.Items.CHESTS_WOODEN;

    public static TagKey<Item> STONE = Tags.Items.STONE;
    public static TagKey<Item> COBBLESTONE = Tags.Items.COBBLESTONE;
    public static TagKey<Item> GLASS = Tags.Items.GLASS;

    public static TagKey<Item> GLASS_CABLE = tag("ae2:glass_cable");
    public static TagKey<Item> SMART_CABLE = tag("ae2:smart_cable");
    public static TagKey<Item> COVERED_CABLE = tag("ae2:covered_cable");
    public static TagKey<Item> COVERED_DENSE_CABLE = tag("ae2:covered_dense_cable");
    public static TagKey<Item> SMART_DENSE_CABLE = tag("ae2:smart_dense_cable");
    public static TagKey<Item> ILLUMINATED_PANEL = tag("ae2:illuminated_panel");
    public static TagKey<Item> INTERFACE = tag("ae2:interface");
    public static TagKey<Item> PATTERN_PROVIDER = tag("ae2:pattern_provider");
    public static TagKey<Item> QUARTZ_WRENCH = tag("ae2:quartz_wrench");
    public static TagKey<Item> QUARTZ_KNIFE = tag("ae2:knife");
    public static TagKey<Item> PAINT_BALLS = tag("ae2:paint_balls");

    public static final Map<DyeColor, TagKey<Item>> DYES = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(
                    Function.identity(),
                    dye -> tag("forge:dyes/" + dye.getSerializedName())));

    public static final TagKey<Block> STAINED_GLASS_BLOCK = Tags.Blocks.STAINED_GLASS;

    public static final TagKey<Block> TERRACOTTA_BLOCK = blockTag("forge:terracotta");

    public static final TagKey<Item> STORAGE_BLOCKS = Tags.Items.STORAGE_BLOCKS;

    public static TagKey<Item> dye(DyeColor color) {
        return DYES.get(color);
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(name));
    }

}
