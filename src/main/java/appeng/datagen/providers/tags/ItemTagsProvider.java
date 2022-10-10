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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.ids.AETags;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.IAE2DataProvider;

public class ItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider implements IAE2DataProvider {

    public ItemTagsProvider(DataGenerator generator, ExistingFileHelper existingFileHelper,
            BlockTagsProvider blockTagsProvider) {
        super(generator, blockTagsProvider, AppEng.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        copyBlockTags();

        // Forge is missing this tag right now
        tag(ConventionTags.COPPER_INGOT)
                .add(Items.COPPER_INGOT);

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

        tag(ConventionTags.INSCRIBER_PRESSES)
                .add(AEItems.CALCULATION_PROCESSOR_PRESS.asItem())
                .add(AEItems.ENGINEERING_PROCESSOR_PRESS.asItem())
                .add(AEItems.LOGIC_PROCESSOR_PRESS.asItem())
                .add(AEItems.SILICON_PRESS.asItem());

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
                .addOptionalTag(new ResourceLocation("forge:ingots/copper"))
                .addOptionalTag(new ResourceLocation("forge:ingots/tin"))
                .addOptionalTag(new ResourceLocation("forge:ingots/iron"))
                .addOptionalTag(new ResourceLocation("forge:ingots/gold"))
                .addOptionalTag(new ResourceLocation("forge:ingots/brass"))
                .addOptionalTag(new ResourceLocation("forge:ingots/nickel"))
                .addOptionalTag(new ResourceLocation("forge:ingots/aluminium"));

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

        tag(ConventionTags.CAN_REMOVE_COLOR).add(Items.WATER_BUCKET, Items.SNOWBALL);

        // Manually add tags for mods that are unlikely to do it themselves since we don't want to force users to craft
        tag(ConventionTags.WRENCH).addOptional(new ResourceLocation("immersiveengineering:hammer"));

        addP2pAttunementTags();
    }

    // Copy the entries AE2 added to certain block tags over to item tags of the same name
    // Assumes that items or item tags generally have the same name as the block equivalent.
    private void copyBlockTags() {
        mirrorBlockTag(Tags.Blocks.ORES.location());
        mirrorBlockTag(new ResourceLocation("forge:ores/certus_quartz"));

        mirrorBlockTag(Tags.Blocks.STORAGE_BLOCKS.location());
        mirrorBlockTag(new ResourceLocation("forge:storage_blocks/certus_quartz"));
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
