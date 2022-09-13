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

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.ids.AETags;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.IAE2DataProvider;

public class ItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider implements IAE2DataProvider {

    public ItemTagsProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagsProvider) {
        super(dataGenerator, blockTagsProvider);
    }

    @Override
    protected void addTags() {
        copyBlockTags();

        // Provide empty blacklist tags
        tag(AETags.ANNIHILATION_PLANE_ITEM_BLACKLIST);

        // Only provide amethyst in the budding tag since that's the one we use; the other tags are for other mods
        tag(ConventionTags.BUDDING_BLOCKS)
                .add(Items.BUDDING_AMETHYST)
                .add(AEBlocks.FLAWLESS_BUDDING_QUARTZ.asItem())
                .add(AEBlocks.FLAWED_BUDDING_QUARTZ.asItem())
                .add(AEBlocks.CHIPPED_BUDDING_QUARTZ.asItem())
                .add(AEBlocks.DAMAGED_BUDDING_QUARTZ.asItem());
        tag(ConventionTags.BUDS)
                .add(AEBlocks.SMALL_QUARTZ_BUD.asItem())
                .add(AEBlocks.MEDIUM_QUARTZ_BUD.asItem())
                .add(AEBlocks.LARGE_QUARTZ_BUD.asItem());
        tag(ConventionTags.CLUSTERS)
                .add(AEBlocks.QUARTZ_CLUSTER.asItem());

        tag(ConventionTags.CERTUS_QUARTZ_DUST)
                .add(AEItems.CERTUS_QUARTZ_DUST.asItem());
        tag(ConventionTags.ENDER_PEARL_DUST)
                .add(AEItems.ENDER_DUST.asItem());

        tag(ConventionTags.ALL_QUARTZ_DUST)
                .addTag(ConventionTags.CERTUS_QUARTZ_DUST);

        tag(ConventionTags.ALL_CERTUS_QUARTZ)
                .addTag(ConventionTags.CERTUS_QUARTZ)
                .add(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem());
        tag(ConventionTags.ALL_FLUIX)
                .add(AEItems.FLUIX_CRYSTAL.asItem());
        tag(ConventionTags.ALL_NETHER_QUARTZ)
                .addTag(ConventionTags.NETHER_QUARTZ);
        tag(ConventionTags.ALL_QUARTZ)
                .addTag(ConventionTags.NETHER_QUARTZ)
                .addTag(ConventionTags.CERTUS_QUARTZ)
                .add(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem());

        for (AEColor color : AEColor.values()) {
            tag(ConventionTags.SMART_DENSE_CABLE).add(AEParts.SMART_DENSE_CABLE.item(color));
            tag(ConventionTags.SMART_CABLE).add(AEParts.SMART_CABLE.item(color));
            tag(ConventionTags.GLASS_CABLE).add(AEParts.GLASS_CABLE.item(color));
            tag(ConventionTags.COVERED_CABLE).add(AEParts.COVERED_CABLE.item(color));
            tag(ConventionTags.COVERED_DENSE_CABLE).add(AEParts.COVERED_DENSE_CABLE.item(color));
            tag(ConventionTags.MEMORY_CARDS).add(AEItems.MEMORY_CARDS.item(color));
        }

        for (AEColor color : AEColor.VALID_COLORS) {
            tag(ConventionTags.PAINT_BALLS).add(AEItems.COLORED_PAINT_BALL.item(color));
        }

        tag(ConventionTags.SILICON)
                .add(AEItems.SILICON.asItem());

        tag(ConventionTags.QUARTZ_AXE)
                .add(AEItems.CERTUS_QUARTZ_AXE.asItem())
                .add(AEItems.NETHER_QUARTZ_AXE.asItem());
        tag(ConventionTags.QUARTZ_HOE)
                .add(AEItems.CERTUS_QUARTZ_HOE.asItem())
                .add(AEItems.NETHER_QUARTZ_HOE.asItem());
        tag(ConventionTags.QUARTZ_PICK)
                .add(AEItems.CERTUS_QUARTZ_PICK.asItem())
                .add(AEItems.NETHER_QUARTZ_PICK.asItem());
        tag(ConventionTags.QUARTZ_SHOVEL)
                .add(AEItems.CERTUS_QUARTZ_SHOVEL.asItem())
                .add(AEItems.NETHER_QUARTZ_SHOVEL.asItem());
        tag(ConventionTags.QUARTZ_SWORD)
                .add(AEItems.CERTUS_QUARTZ_SWORD.asItem())
                .add(AEItems.NETHER_QUARTZ_SWORD.asItem());
        tag(ConventionTags.QUARTZ_WRENCH)
                .add(AEItems.CERTUS_QUARTZ_WRENCH.asItem())
                .add(AEItems.NETHER_QUARTZ_WRENCH.asItem());
        tag(ConventionTags.QUARTZ_KNIFE)
                .add(AEItems.CERTUS_QUARTZ_KNIFE.asItem())
                .add(AEItems.NETHER_QUARTZ_KNIFE.asItem());

        tag(AETags.METAL_INGOTS)
                .addOptionalTag(ConventionTags.IRON_INGOT.location())
                .addOptionalTag(ConventionTags.GOLD_INGOT.location())
                .addOptionalTag(new ResourceLocation("c:copper_ingots"))
                .addOptionalTag(new ResourceLocation("c:tin_ingots"))
                .addOptionalTag(new ResourceLocation("c:brass_ingots"))
                .addOptionalTag(new ResourceLocation("c:nickel_ingots"))
                .addOptionalTag(new ResourceLocation("c:aluminium_ingots"));

        tag(ConventionTags.PATTERN_PROVIDER)
                .add(AEParts.PATTERN_PROVIDER.asItem())
                .add(AEBlocks.PATTERN_PROVIDER.asItem());

        tag(ConventionTags.INTERFACE)
                .add(AEParts.INTERFACE.asItem())
                .add(AEBlocks.INTERFACE.asItem());

        tag(ConventionTags.ILLUMINATED_PANEL)
                .add(AEParts.MONITOR.asItem())
                .add(AEParts.SEMI_DARK_MONITOR.asItem())
                .add(AEParts.DARK_MONITOR.asItem());

        tag(ConventionTags.FLUIX_DUST)
                .add(AEItems.FLUIX_DUST.asItem());
        tag(ConventionTags.CERTUS_QUARTZ_DUST)
                .add(AEItems.CERTUS_QUARTZ_DUST.asItem());

        tag(ConventionTags.FLUIX_CRYSTAL)
                .add(AEItems.FLUIX_CRYSTAL.asItem());
        tag(ConventionTags.CERTUS_QUARTZ)
                .add(AEItems.CERTUS_QUARTZ_CRYSTAL.asItem())
                .add(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem());

        tag(ConventionTags.DUSTS)
                .add(AEItems.CERTUS_QUARTZ_DUST.asItem())
                .add(AEItems.ENDER_DUST.asItem())
                .add(AEItems.FLUIX_DUST.asItem())
                .add(AEItems.SKY_DUST.asItem());

        tag(ConventionTags.GEMS)
                .add(AEItems.CERTUS_QUARTZ_CRYSTAL.asItem())
                .add(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
                .add(AEItems.FLUIX_CRYSTAL.asItem());

        // Fabric replacement for ToolActions for now
        tag(ConventionTags.WRENCH).add(
                AEItems.CERTUS_QUARTZ_WRENCH.asItem(),
                AEItems.NETHER_QUARTZ_WRENCH.asItem(),
                AEItems.NETWORK_TOOL.asItem());

        addConventionTags();

        addP2pAttunementTags();
    }

    private void addConventionTags() {

        tag(ConventionTags.NETHER_QUARTZ)
                .add(Items.QUARTZ);

        tag(ConventionTags.NETHER_QUARTZ_ORE)
                .add(Items.NETHER_QUARTZ_ORE);

        tag(ConventionTags.COPPER_INGOT)
                .add(Items.COPPER_INGOT);

        tag(ConventionTags.GOLD_NUGGET)
                .add(Items.GOLD_NUGGET);

        tag(ConventionTags.GOLD_INGOT)
                .add(Items.GOLD_INGOT);

        tag(ConventionTags.GOLD_ORE)
                .addOptionalTag(ItemTags.GOLD_ORES.location());

        tag(ConventionTags.IRON_NUGGET)
                .add(Items.IRON_NUGGET);

        tag(ConventionTags.IRON_INGOT)
                .add(Items.IRON_INGOT);

        tag(ConventionTags.IRON_ORE)
                .addOptional(ItemTags.IRON_ORES.location());

        tag(ConventionTags.DIAMOND)
                .add(Items.DIAMOND);

        tag(ConventionTags.REDSTONE)
                .add(Items.REDSTONE);

        tag(ConventionTags.GLOWSTONE)
                .add(Items.GLOWSTONE_DUST);

        tag(ConventionTags.ENDER_PEARL)
                .add(Items.ENDER_PEARL);

        tag(ConventionTags.WOOD_STICK)
                .add(Items.STICK);

        tag(ConventionTags.CHEST)
                .add(Items.CHEST, Items.TRAPPED_CHEST);

        // Direct copy of forge:stone
        tag(ConventionTags.STONE)
                .add(
                        Items.ANDESITE,
                        Items.DIORITE,
                        Items.GRANITE,
                        Items.INFESTED_STONE,
                        Items.STONE,
                        Items.POLISHED_ANDESITE,
                        Items.POLISHED_DIORITE,
                        Items.POLISHED_GRANITE);

        tag(ConventionTags.COBBLESTONE)
                .add(
                        Items.COBBLESTONE,
                        Items.INFESTED_COBBLESTONE,
                        Items.MOSSY_COBBLESTONE);

        tag(ConventionTags.GLASS)
                .add(
                        Items.GLASS,
                        Items.WHITE_STAINED_GLASS,
                        Items.ORANGE_STAINED_GLASS,
                        Items.MAGENTA_STAINED_GLASS,
                        Items.LIGHT_BLUE_STAINED_GLASS,
                        Items.YELLOW_STAINED_GLASS,
                        Items.LIME_STAINED_GLASS,
                        Items.PINK_STAINED_GLASS,
                        Items.GRAY_STAINED_GLASS,
                        Items.LIGHT_GRAY_STAINED_GLASS,
                        Items.CYAN_STAINED_GLASS,
                        Items.PURPLE_STAINED_GLASS,
                        Items.BLUE_STAINED_GLASS,
                        Items.BROWN_STAINED_GLASS,
                        Items.GREEN_STAINED_GLASS,
                        Items.RED_STAINED_GLASS,
                        Items.BLACK_STAINED_GLASS);

        tag(ConventionTags.dye(DyeColor.WHITE)).add(Items.WHITE_DYE);
        tag(ConventionTags.dye(DyeColor.ORANGE)).add(Items.ORANGE_DYE);
        tag(ConventionTags.dye(DyeColor.MAGENTA)).add(Items.MAGENTA_DYE);
        tag(ConventionTags.dye(DyeColor.LIGHT_BLUE)).add(Items.LIGHT_BLUE_DYE);
        tag(ConventionTags.dye(DyeColor.YELLOW)).add(Items.YELLOW_DYE);
        tag(ConventionTags.dye(DyeColor.LIME)).add(Items.LIME_DYE);
        tag(ConventionTags.dye(DyeColor.PINK)).add(Items.PINK_DYE);
        tag(ConventionTags.dye(DyeColor.GRAY)).add(Items.GRAY_DYE);
        tag(ConventionTags.dye(DyeColor.LIGHT_GRAY)).add(Items.LIGHT_GRAY_DYE);
        tag(ConventionTags.dye(DyeColor.CYAN)).add(Items.CYAN_DYE);
        tag(ConventionTags.dye(DyeColor.PURPLE)).add(Items.PURPLE_DYE);
        tag(ConventionTags.dye(DyeColor.BLUE)).add(Items.BLUE_DYE);
        tag(ConventionTags.dye(DyeColor.BROWN)).add(Items.BROWN_DYE);
        tag(ConventionTags.dye(DyeColor.GREEN)).add(Items.GREEN_DYE);
        tag(ConventionTags.dye(DyeColor.RED)).add(Items.RED_DYE);
        tag(ConventionTags.dye(DyeColor.BLACK)).add(Items.BLACK_DYE);

        tag(ConventionTags.WHEAT_CROP).add(Items.WHEAT);

    }

    // Copy the entries AE2 added to certain block tags over to item tags of the same name
    // Assumes that items or item tags generally have the same name as the block equivalent.
    private void copyBlockTags() {
        mirrorBlockTag(new ResourceLocation("c:ores"));
        mirrorBlockTag(new ResourceLocation("c:certus_quartz_ores"));

        mirrorBlockTag(new ResourceLocation("c:storage_blocks"));
        mirrorBlockTag(new ResourceLocation("c:certus_quartz_blocks"));
    }

    private void mirrorBlockTag(ResourceLocation tagName) {
        copy(TagKey.create(Registry.BLOCK_REGISTRY, tagName), TagKey.create(Registry.ITEM_REGISTRY, tagName));
    }

    private void addP2pAttunementTags() {
        tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.LIGHT_TUNNEL))
                .add(Items.TORCH, Items.GLOWSTONE);

        tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.ENERGY_TUNNEL))
                .add(AEBlocks.DENSE_ENERGY_CELL.asItem(), AEBlocks.ENERGY_ACCEPTOR.asItem(),
                        AEBlocks.ENERGY_CELL.asItem(), AEBlocks.CREATIVE_ENERGY_CELL.asItem());

        tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.REDSTONE_TUNNEL))
                .add(Items.REDSTONE, Items.REPEATER, Items.REDSTONE_LAMP, Items.COMPARATOR, Items.DAYLIGHT_DETECTOR,
                        Items.REDSTONE_TORCH, Items.REDSTONE_BLOCK, Items.LEVER);

        tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.ITEM_TUNNEL))
                .add(AEParts.STORAGE_BUS.asItem(), AEParts.EXPORT_BUS.asItem(), AEParts.IMPORT_BUS.asItem(),
                        Items.HOPPER, Items.CHEST, Items.TRAPPED_CHEST)
                .addTag(ConventionTags.INTERFACE);

        tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.FLUID_TUNNEL))
                .add(Items.BUCKET, Items.MILK_BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET);

        tag(P2PTunnelAttunement.getAttunementTag(P2PTunnelAttunement.ME_TUNNEL))
                .addTag(ConventionTags.COVERED_CABLE)
                .addTag(ConventionTags.COVERED_DENSE_CABLE)
                .addTag(ConventionTags.GLASS_CABLE)
                .addTag(ConventionTags.SMART_CABLE)
                .addTag(ConventionTags.SMART_DENSE_CABLE);
    }
}
