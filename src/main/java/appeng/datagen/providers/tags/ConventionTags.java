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

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import appeng.core.AppEng;

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

    public static final TagKey<DataComponentType<?>> EXPORTED_SETTINGS = net.minecraft.tags.TagKey.create(
            Registries.DATA_COMPONENT_TYPE,
            AppEng.makeId("exported_settings"));

    public static final TagKey<Item> DUSTS = Tags.Items.DUSTS;
    public static final TagKey<Item> GEMS = Tags.Items.GEMS;

    public static final TagKey<Item> SILICON = tag("c:silicon");

    // Includes purified versions of certus/nether and the natural ones
    public static final TagKey<Item> ALL_QUARTZ = tag("ae2:all_quartz");
    // Includes both certus/nether quartz dust
    public static final TagKey<Item> ALL_QUARTZ_DUST = tag("ae2:all_quartz_dust");

    // Includes charged, synthetic/purified and natural certus quartz
    public static final TagKey<Item> ALL_CERTUS_QUARTZ = tag("ae2:all_certus_quartz");
    public static final TagKey<Item> CERTUS_QUARTZ = tag("c:gems/certus_quartz");
    public static final TagKey<Block> CERTUS_QUARTZ_STORAGE_BLOCK_BLOCK = blockTag(
            "c:storage_blocks/certus_quartz");
    public static final TagKey<Item> CERTUS_QUARTZ_DUST = tag("c:dusts/certus_quartz");

    // Includes synthetic/purified
    public static final TagKey<Item> ALL_NETHER_QUARTZ = tag("ae2:all_nether_quartz");
    public static final TagKey<Item> NETHER_QUARTZ = Tags.Items.GEMS_QUARTZ;
    public static final TagKey<Item> NETHER_QUARTZ_ORE = Tags.Items.ORES_QUARTZ;

    // Includes synthetic/purified
    public static final TagKey<Item> ALL_FLUIX = tag("ae2:all_fluix");
    public static final TagKey<Item> FLUIX_DUST = tag("c:dusts/fluix");
    public static final TagKey<Item> FLUIX_CRYSTAL = tag("c:gems/fluix");

    public static final TagKey<Item> COPPER_INGOT = tag("c:ingots/copper");

    public static final TagKey<Item> GOLD_NUGGET = Tags.Items.NUGGETS_GOLD;
    public static final TagKey<Item> GOLD_INGOT = Tags.Items.INGOTS_GOLD;
    public static final TagKey<Item> GOLD_ORE = Tags.Items.ORES_GOLD;

    public static final TagKey<Item> IRON_NUGGET = Tags.Items.NUGGETS_IRON;
    public static final TagKey<Item> IRON_INGOT = Tags.Items.INGOTS_IRON;
    public static final TagKey<Item> IRON_ORE = Tags.Items.ORES_IRON;

    public static final TagKey<Item> DIAMOND = Tags.Items.GEMS_DIAMOND;
    public static final TagKey<Item> REDSTONE = Tags.Items.DUSTS_REDSTONE;
    public static final TagKey<Item> GLOWSTONE = Tags.Items.DUSTS_GLOWSTONE;

    public static final TagKey<Item> ENDER_PEARL = Tags.Items.ENDER_PEARLS;
    public static final TagKey<Item> ENDER_PEARL_DUST = tag("c:dusts/ender_pearl");

    public static final TagKey<Item> WOOD_STICK = Tags.Items.RODS_WOODEN;
    public static final TagKey<Item> CHEST = Tags.Items.CHESTS_WOODEN;

    public static final TagKey<Item> STONE = Tags.Items.STONES;
    public static final TagKey<Item> COBBLESTONE = Tags.Items.COBBLESTONES;
    public static final TagKey<Item> GLASS = Tags.Items.GLASS_BLOCKS;
    public static final TagKey<Block> GLASS_BLOCK = Tags.Blocks.GLASS_BLOCKS;

    public static final TagKey<Item> GLASS_CABLE = tag("ae2:glass_cable");
    public static final TagKey<Item> SMART_CABLE = tag("ae2:smart_cable");
    public static final TagKey<Item> COVERED_CABLE = tag("ae2:covered_cable");
    public static final TagKey<Item> COVERED_DENSE_CABLE = tag("ae2:covered_dense_cable");
    public static final TagKey<Item> SMART_DENSE_CABLE = tag("ae2:smart_dense_cable");
    public static final TagKey<Item> ILLUMINATED_PANEL = tag("ae2:illuminated_panel");
    public static final TagKey<Item> INTERFACE = tag("ae2:interface");
    public static final TagKey<Item> PATTERN_PROVIDER = tag("ae2:pattern_provider");
    public static final TagKey<Item> QUARTZ_AXE = tag("ae2:quartz_axe");
    public static final TagKey<Item> QUARTZ_HOE = tag("ae2:quartz_hoe");
    public static final TagKey<Item> QUARTZ_PICK = tag("ae2:quartz_pickaxe");
    public static final TagKey<Item> QUARTZ_SHOVEL = tag("ae2:quartz_shovel");
    public static final TagKey<Item> QUARTZ_SWORD = tag("ae2:quartz_sword");
    public static final TagKey<Item> QUARTZ_WRENCH = tag("ae2:quartz_wrench");
    public static final TagKey<Item> QUARTZ_KNIFE = tag("ae2:knife");
    public static final TagKey<Item> PAINT_BALLS = tag("ae2:paint_balls");
    public static final TagKey<Item> INSCRIBER_PRESSES = tag("ae2:inscriber_presses");
    /**
     * Items that can be used in recipes to remove color from colored items.
     */
    public static final TagKey<Item> CAN_REMOVE_COLOR = tag("ae2:can_remove_color");

    // Budding stuff
    public static final TagKey<Item> BUDDING_BLOCKS = tag("c:budding");
    public static final TagKey<Item> BUDS = tag("c:buds");
    public static final TagKey<Item> CLUSTERS = tag("c:clusters");
    public static final TagKey<Block> BUDDING_BLOCKS_BLOCKS = blockTag("c:budding");
    public static final TagKey<Block> BUDS_BLOCKS = blockTag("c:buds");
    public static final TagKey<Block> CLUSTERS_BLOCKS = blockTag("c:clusters");

    // For Growth Accelerator
    public static final TagKey<Block> CROPS = BlockTags.CROPS;
    public static final TagKey<Block> SAPLINGS = BlockTags.SAPLINGS;

    /**
     * Platform tags for blocks that should not be moved, i.e. some pipes, chunk loaders, etc...
     */
    public static final TagKey<Block> IMMOVABLE_BLOCKS = blockTag("c:relocation_not_supported");

    /**
     * For Worldgen Biomes
     */
    public static final TagKey<Biome> METEORITE_OCEAN = Tags.Biomes.IS_OCEAN;

    /**
     * Used to identify items that act as wrenches.
     */
    public static final TagKey<Item> WRENCH = tag("c:tools/wrenches");

    public static final Map<DyeColor, TagKey<Item>> DYES = Arrays.stream(DyeColor.values())
            .collect(Collectors.toMap(
                    Function.identity(),
                    dye -> tag("c:dyes/" + dye.getSerializedName())));

    public static final TagKey<Block> STAINED_GLASS_BLOCK = Tags.Blocks.GLASS_BLOCKS_TINTED;

    public static final TagKey<Block> TERRACOTTA_BLOCK = blockTag("c:terracotta");

    public static final TagKey<Item> STORAGE_BLOCKS = Tags.Items.STORAGE_BLOCKS;

    public static final TagKey<Item> CURIOS = tag("curios:curio");

    public static TagKey<Item> dye(DyeColor color) {
        return DYES.get(color);
    }

    private static TagKey<Item> tag(String name) {
        return net.minecraft.tags.TagKey.create(Registries.ITEM, ResourceLocation.parse(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return net.minecraft.tags.TagKey.create(Registries.BLOCK, ResourceLocation.parse(name));
    }

}
