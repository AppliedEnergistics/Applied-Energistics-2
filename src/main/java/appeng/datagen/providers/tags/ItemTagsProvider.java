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

import appeng.api.ids.AETags;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.IAE2DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.nio.file.Path;

public class ItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider implements IAE2DataProvider {

    public ItemTagsProvider(GatherDataEvent dataEvent, BlockTagsProvider blockTagsProvider) {
        super(dataEvent.getGenerator(), blockTagsProvider, AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        copyBlockTags();

        add("blacklisted/annihilation_plane");

        add(ConventionTags.CERTUS_QUARTZ_DUST, AEItems.CERTUS_QUARTZ_DUST);
        add(ConventionTags.ENDER_PEARL_DUST, AEItems.ENDER_DUST);

        add(ConventionTags.ALL_QUARTZ_DUST, ConventionTags.CERTUS_QUARTZ_DUST, ConventionTags.NETHER_QUARTZ_DUST);
        add(ConventionTags.ALL_CERTUS_QUARTZ,
                ConventionTags.CERTUS_QUARTZ,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEItems.PURIFIED_CERTUS_QUARTZ_CRYSTAL);
        add(ConventionTags.ALL_FLUIX,
                AEItems.FLUIX_CRYSTAL,
                AEItems.PURIFIED_FLUIX_CRYSTAL);
        add(ConventionTags.ALL_NETHER_QUARTZ,
                Tags.Items.GEMS_QUARTZ,
                AEItems.PURIFIED_NETHER_QUARTZ_CRYSTAL);
        add(ConventionTags.ALL_QUARTZ,
                Tags.Items.GEMS_QUARTZ,
                ConventionTags.CERTUS_QUARTZ,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);

        for (AEColor color : AEColor.values()) {
            add(ConventionTags.SMART_DENSE_CABLE, AEParts.SMART_DENSE_CABLE.item(color));
            add(ConventionTags.SMART_CABLE, AEParts.SMART_CABLE.item(color));
            add(ConventionTags.GLASS_CABLE, AEParts.GLASS_CABLE.item(color));
            add(ConventionTags.COVERED_CABLE, AEParts.COVERED_CABLE.item(color));
            add(ConventionTags.COVERED_DENSE_CABLE, AEParts.COVERED_DENSE_CABLE.item(color));
        }

        for (AEColor color : AEColor.VALID_COLORS) {
            add(ConventionTags.PAINT_BALLS, AEItems.COLORED_PAINT_BALL.item(color));
        }

        add(ConventionTags.SILICON, AEItems.SILICON);
        add(ConventionTags.QUARTZ_WRENCH, AEItems.CERTUS_QUARTZ_WRENCH, AEItems.NETHER_QUARTZ_WRENCH);
        add(ConventionTags.QUARTZ_KNIFE, AEItems.CERTUS_QUARTZ_KNIFE, AEItems.NETHER_QUARTZ_KNIFE);

        add(ConventionTags.NETHER_QUARTZ_DUST, AEItems.NETHER_QUARTZ_DUST);

        add(AETags.METAL_INGOTS,
                optionalTag("forge:ingots/copper"),
                optionalTag("forge:ingots/tin"),
                optionalTag("forge:ingots/iron"),
                optionalTag("forge:ingots/gold"),
                optionalTag("forge:ingots/brass"),
                optionalTag("forge:ingots/nickel"),
                optionalTag("forge:ingots/aluminium")
        );

        add(ConventionTags.ITEM_INTERFACE, AEParts.INTERFACE, AEBlocks.ITEM_INTERFACE);
        add(ConventionTags.FLUID_INTERFACE, AEParts.FLUID_INTERFACE, AEBlocks.FLUID_INTERFACE);

        add(ConventionTags.ILLUMINATED_PANEL, AEParts.MONITOR,
                AEParts.SEMI_DARK_MONITOR,
                AEParts.DARK_MONITOR);

        add(ConventionTags.WOOD_GEAR, AEItems.WOODEN_GEAR);

        add(ConventionTags.GOLD_DUST, AEItems.GOLD_DUST);
        add(ConventionTags.IRON_DUST, AEItems.IRON_DUST);
        add(ConventionTags.NETHER_QUARTZ_DUST, AEItems.NETHER_QUARTZ_DUST);
        add(ConventionTags.FLUIX_DUST, AEItems.FLUIX_DUST);
        add(ConventionTags.CERTUS_QUARTZ_DUST, AEItems.CERTUS_QUARTZ_DUST);

        add(ConventionTags.FLUIX_CRYSTAL, AEItems.FLUIX_CRYSTAL);
        add(ConventionTags.CERTUS_QUARTZ, AEItems.CERTUS_QUARTZ_CRYSTAL,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);
    }

    private Tag.Named<Item> optionalTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(name));
    }

    private void copyBlockTags() {
        mirrorForgeBlockTag("ores");
        mirrorForgeBlockTag("ores/certus_quartz");

        mirrorForgeBlockTag("storage_blocks");
        mirrorForgeBlockTag("storage_blocks/certus_quartz");
    }

    private void add(String tagName, Object... itemSources) {
        add(AppEng.makeId(tagName), itemSources);
    }

    private void add(Tag.Named<Item> tag, Object... itemSources) {
        add(tag.getName(), itemSources);
    }

    @SuppressWarnings("unchecked")
    private void add(ResourceLocation tagName, Object... itemSources) {
        TagAppender<Item> builder = tag(ItemTags.createOptional(tagName));

        for (Object itemSource : itemSources) {
            if (itemSource instanceof ItemLike) {
                builder.add(((ItemLike) itemSource).asItem());
            } else if (itemSource instanceof Tag.Named) {
                builder.addTag((Tag.Named<Item>) itemSource);
            } else if (itemSource instanceof String itemSourceString) {
                if (itemSourceString.startsWith("#")) {
                    builder.add(new Tag.TagEntry(new ResourceLocation(itemSourceString.substring(1))));
                } else {
                    builder.add(new Tag.ElementEntry(new ResourceLocation(itemSourceString)));
                }
            } else {
                throw new IllegalArgumentException("Unknown item source: " + itemSource);
            }
        }
    }

    private void mirrorForgeBlockTag(String tagName) {
        mirrorBlockTag(new ResourceLocation("forge:" + tagName));
    }

    private void mirrorBlockTag(ResourceLocation tagName) {
        copy(
                BlockTags.createOptional(tagName),
                ItemTags.createOptional(tagName));
    }

    @Override
    protected Path getPath(ResourceLocation id) {
        return this.generator.getOutputFolder()
                .resolve("data/" + id.getNamespace() + "/tags/items/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Item Tags";
    }
}
