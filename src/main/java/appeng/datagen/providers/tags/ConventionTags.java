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

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
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

    public static TagKey<Item> DUSTS = tag("c:dusts");
    public static TagKey<Item> GEMS = tag("c:gems");
    public static TagKey<Block> ORES = blockTag("c:ores");

    public static TagKey<Item> SILICON = tag("c:silicon");

    // Includes purified versions of certus/nether and the natural ones
    public static TagKey<Item> ALL_QUARTZ = tag("ae2:all_quartz");
    // Includes both certus/nether quartz dust
    public static TagKey<Item> ALL_QUARTZ_DUST = tag("ae2:all_quartz_dust");

    // Includes charged, synthetic/purified and natural certus quartz
    public static TagKey<Item> ALL_CERTUS_QUARTZ = tag("ae2:all_certus_quartz");
    public static TagKey<Item> CERTUS_QUARTZ = tag("c:certus_quartz");
    public static TagKey<Item> CERTUS_QUARTZ_ORE = tag("c:certus_quartz_ores");
    public static TagKey<Block> CERTUS_QUARTZ_ORE_BLOCK = blockTag("c:certus_quartz_ores");
    public static TagKey<Block> CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK = blockTag("c:certus_quartz_blocks");
    public static TagKey<Item> CERTUS_QUARTZ_DUST = tag("c:certus_quartz_dusts");

    // Includes synthetic/purified
    public static TagKey<Item> ALL_NETHER_QUARTZ = tag("ae2:all_nether_quartz");
    public static TagKey<Item> NETHER_QUARTZ = tag("c:quartz");
    public static TagKey<Item> NETHER_QUARTZ_ORE = tag("c:quartz_ores");

    // Includes synthetic/purified
    public static TagKey<Item> ALL_FLUIX = tag("ae2:all_fluix");
    public static TagKey<Item> FLUIX_DUST = tag("c:fluix_dusts");
    public static TagKey<Item> FLUIX_CRYSTAL = tag("c:fluix");

    public static TagKey<Item> COPPER_INGOT = tag("c:copper_ingots");

    public static TagKey<Item> GOLD_NUGGET = tag("c:gold_nuggets");
    public static TagKey<Item> GOLD_INGOT = tag("c:gold_ingots");
    public static TagKey<Item> GOLD_ORE = tag("c:gold_ores");

    public static TagKey<Item> IRON_NUGGET = tag("c:iron_nuggets");
    public static TagKey<Item> IRON_INGOT = tag("c:iron_ingots");
    public static TagKey<Item> IRON_ORE = tag("c:iron_ores");

    public static TagKey<Item> DIAMOND = tag("c:diamonds");
    public static TagKey<Item> REDSTONE = tag("c:redstone_dusts");
    public static TagKey<Item> GLOWSTONE = tag("c:glowstone_dusts");

    public static TagKey<Item> ENDER_PEARL = tag("c:ender_pearls");
    public static TagKey<Item> ENDER_PEARL_DUST = tag("c:ender_pearl_dusts");
    public static TagKey<Item> WHEAT_CROP = tag("c:wheat_crops");

    public static TagKey<Item> WOOD_STICK = tag("c:wooden_rods");
    public static TagKey<Item> CHEST = tag("c:wooden_chests");

    public static TagKey<Item> STONE = tag("c:stone");
    public static TagKey<Item> COBBLESTONE = tag("c:cobblestone");
    public static TagKey<Item> GLASS = tag("c:glass");

    public static TagKey<Item> GLASS_CABLE = tag("ae2:glass_cable");
    public static TagKey<Item> SMART_CABLE = tag("ae2:smart_cable");
    public static TagKey<Item> COVERED_CABLE = tag("ae2:covered_cable");
    public static TagKey<Item> COVERED_DENSE_CABLE = tag("ae2:covered_dense_cable");
    public static TagKey<Item> SMART_DENSE_CABLE = tag("ae2:smart_dense_cable");
    public static TagKey<Item> ILLUMINATED_PANEL = tag("ae2:illuminated_panel");
    public static TagKey<Item> INTERFACE = tag("ae2:interface");
    public static TagKey<Item> PATTERN_PROVIDER = tag("ae2:pattern_provider");
    public static TagKey<Item> QUARTZ_AXE = tag("ae2:quartz_axe");
    public static TagKey<Item> QUARTZ_HOE = tag("ae2:quartz_hoe");
    public static TagKey<Item> QUARTZ_PICK = tag("ae2:quartz_pickaxe");
    public static TagKey<Item> QUARTZ_SHOVEL = tag("ae2:quartz_shovel");
    public static TagKey<Item> QUARTZ_SWORD = tag("ae2:quartz_sword");
    public static TagKey<Item> QUARTZ_WRENCH = tag("ae2:quartz_wrench");
    public static TagKey<Item> QUARTZ_KNIFE = tag("ae2:knife");
    public static TagKey<Item> PAINT_BALLS = tag("ae2:paint_balls");
    public static TagKey<Item> MEMORY_CARDS = tag("ae2:memory_cards");

    /**
     * For Worldgen Biomes
     */
    public static TagKey<Biome> METEORITE_OCEAN = ConventionalBiomeTags.OCEAN;

    /**
     * Indicates biomes that quartz ore generates in.
     */
    public static TagKey<Biome> HAS_QUARTZ_ORE = biomeTag("ae2:has_quartz_ore");

    /**
     * Used to identify items that act as wrenches.
     */
    public static final TagKey<Item> WRENCH = tag("c:wrenches");

    public static final Map<DyeColor, TagKey<Item>> DYES = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(
                    Function.identity(),
                    dye -> tag("c:" + dye.getSerializedName() + "_dye")));

    public static final TagKey<Block> STAINED_GLASS_BLOCK = blockTag("c:stained_glass");

    public static final TagKey<Block> TERRACOTTA_BLOCK = blockTag("c:terracotta");

    public static TagKey<Item> dye(DyeColor color) {
        return DYES.get(color);
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(name));
    }

    private static TagKey<Biome> biomeTag(String name) {
        return TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(name));
    }

}
