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

import java.nio.file.Path;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import appeng.api.ids.AETags;
import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.IAE2DataProvider;

public class ItemTagsProvider extends net.minecraft.data.tags.ItemTagsProvider implements IAE2DataProvider {

    public ItemTagsProvider(GatherDataEvent dataEvent, BlockTagsProvider blockTagsProvider) {
        super(dataEvent.getGenerator(), blockTagsProvider, AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        registerForgeTags();

        addAe2("blacklisted/annihilation_plane");

        addAe2(ConventionTags.CERTUS_QUARTZ_DUST, AEItems.CERTUS_QUARTZ_DUST);
        addAe2(ConventionTags.ENDER_PEARL_DUST, AEItems.ENDER_DUST);
        addAe2(ConventionTags.FLUIX_DUST, AEItems.FLUIX_DUST);
        addAe2(ConventionTags.ALL_QUARTZ_DUST, ConventionTags.CERTUS_QUARTZ_DUST, ConventionTags.NETHER_QUARTZ_DUST);

        addAe2(AETags.CERTUS_QUARTZ_CRYSTAL,
                ConventionTags.CERTUS_QUARTZ,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEItems.PURIFIED_CERTUS_QUARTZ_CRYSTAL);
        addAe2(ConventionTags.CERTUS_QUARTZ,
                AEItems.CERTUS_QUARTZ_CRYSTAL);
        addAe2(ConventionTags.ALL_FLUIX,
                AEItems.FLUIX_CRYSTAL,
                AEItems.PURIFIED_FLUIX_CRYSTAL);
        addAe2(ConventionTags.ALL_NETHER_QUARTZ,
                Tags.Items.GEMS_QUARTZ,
                AEItems.PURIFIED_NETHER_QUARTZ_CRYSTAL);
        addAe2(ConventionTags.ALL_QUARTZ,
                Tags.Items.GEMS_QUARTZ,
                ConventionTags.CERTUS_QUARTZ,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);

        for (AEColor color : AEColor.values()) {
            addAe2(ConventionTags.SMART_DENSE_CABLE, AEParts.SMART_DENSE_CABLE.item(color));
            addAe2(ConventionTags.SMART_CABLE, AEParts.SMART_CABLE.item(color));
            addAe2(ConventionTags.GLASS_CABLE, AEParts.GLASS_CABLE.item(color));
            addAe2(ConventionTags.COVERED_CABLE, AEParts.COVERED_CABLE.item(color));
            addAe2(ConventionTags.COVERED_DENSE_CABLE, AEParts.COVERED_DENSE_CABLE.item(color));
        }

        addAe2(ConventionTags.SILICON, AEItems.SILICON);
        addAe2(ConventionTags.QUARTZ_WRENCH, AEItems.CERTUS_QUARTZ_WRENCH, AEItems.NETHER_QUARTZ_WRENCH);
        addAe2(ConventionTags.QUARTZ_KNIFE, AEItems.CERTUS_QUARTZ_KNIFE, AEItems.NETHER_QUARTZ_KNIFE);

        addAe2(ConventionTags.NETHER_QUARTZ_DUST, AEItems.NETHER_QUARTZ_DUST);

        addAe2(ConventionTags.METAL_INGOTS, Tags.Items.INGOTS_GOLD, Tags.Items.INGOTS_IRON);

        addAe2(ConventionTags.ITEM_INTERFACE, AEParts.INTERFACE, AEBlocks.ITEM_INTERFACE);
        addAe2(ConventionTags.FLUID_INTERFACE, AEParts.FLUID_INTERFACE, AEBlocks.FLUID_INTERFACE);

        addAe2(ConventionTags.ILLUMINATED_PANEL, AEParts.MONITOR,
                AEParts.SEMI_DARK_MONITOR,
                AEParts.DARK_MONITOR);

        addAe2(ConventionTags.WOOD_GEAR, AEItems.WOODEN_GEAR);
    }

    /**
     * Adds our items to common Forge tags.
     */
    private void registerForgeTags() {
        mirrorForgeBlockTag("ores");
        mirrorForgeBlockTag("ores/certus_quartz");

        mirrorForgeBlockTag("storage_blocks");
        mirrorForgeBlockTag("storage_blocks/certus_quartz");

        addForge("dusts/gold", AEItems.GOLD_DUST);
        addForge("dusts/iron", AEItems.IRON_DUST);
        addForge("dusts/quartz", AEItems.NETHER_QUARTZ_DUST);
        addForge("dusts/fluix", AEItems.FLUIX_DUST);
        addForge("dusts/certus_quartz", AEItems.CERTUS_QUARTZ_DUST);

        addForge("gems/fluix", AEItems.FLUIX_CRYSTAL);
        addForge("gems/certus_quartz", AEItems.CERTUS_QUARTZ_CRYSTAL,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);
    }

    private void addForge(String tagName, Object... itemSources) {
        add(new ResourceLocation("forge", tagName), itemSources);
    }

    private void addAe2(String tagName, Object... itemSources) {
        add(AppEng.makeId(tagName), itemSources);
    }

    private void addAe2(Tag.Named<Item> tag, Object... itemSources) {
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

    private void mirrorAe2BlockTag(String tagName) {
        mirrorBlockTag(AppEng.makeId(tagName));
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
