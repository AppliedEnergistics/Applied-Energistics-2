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

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.IAE2DataProvider;

public class ItemTagsProvider extends net.minecraft.data.ItemTagsProvider implements IAE2DataProvider {

    public ItemTagsProvider(GatherDataEvent dataEvent, BlockTagsProvider blockTagsProvider) {
        super(dataEvent.getGenerator(), blockTagsProvider, AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void registerTags() {
        registerForgeTags();

        addAe2("blacklisted/annihilation_plane");

        addAe2("dusts/certus_quartz", AEItems.CERTUS_QUARTZ_DUST);
        addAe2("dusts/ender", AEItems.ENDER_DUST);
        addAe2("dusts/fluix", AEItems.FLUIX_DUST);
        addAe2("dusts/quartz", "#appliedenergistics2:dusts/certus_quartz", "#forge:dusts/quartz");

        addAe2("crystals/certus",
                "#appliedenergistics2:crystals/certus_quartz",
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEItems.PURIFIED_CERTUS_QUARTZ_CRYSTAL);
        addAe2("crystals/certus_quartz",
                AEItems.CERTUS_QUARTZ_CRYSTAL);
        addAe2("crystals/fluix",
                AEItems.FLUIX_CRYSTAL,
                AEItems.PURIFIED_FLUIX_CRYSTAL);
        addAe2("crystals/nether",
                Tags.Items.GEMS_QUARTZ,
                AEItems.PURIFIED_NETHER_QUARTZ_CRYSTAL);
        addAe2("crystals/quartz",
                Tags.Items.GEMS_QUARTZ,
                "#appliedenergistics2:crystals/certus_quartz",
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);

        addAe2("workbench", Items.CRAFTING_TABLE);
        addAe2("wool", ItemTags.WOOL);

        for (AEColor color : AEColor.values()) {
            addAe2("smart_dense_cable", AEParts.SMART_DENSE_CABLE.item(color));
            addAe2("smart_cable", AEParts.SMART_CABLE.item(color));
            addAe2("glass_cable", AEParts.GLASS_CABLE.item(color));
            addAe2("covered_cable", AEParts.COVERED_CABLE.item(color));
            addAe2("covered_dense_cable", AEParts.COVERED_DENSE_CABLE.item(color));
        }

        addAe2("silicon", AEItems.SILICON);
        addAe2("quartz_wrench", AEItems.CERTUS_QUARTZ_WRENCH, AEItems.NETHER_QUARTZ_WRENCH);
        addAe2("knife", AEItems.CERTUS_QUARTZ_KNIFE, AEItems.NETHER_QUARTZ_KNIFE);

        addAe2("nether_quartz_dust", AEItems.NETHER_QUARTZ_DUST);

        addAe2("metal_ingots", Tags.Items.INGOTS_GOLD, Tags.Items.INGOTS_IRON);

        addAe2("item_interface", AEParts.INTERFACE, AEBlocks.INTERFACE);
        addAe2("fluid_interface", AEParts.FLUID_INTERFACE, AEBlocks.FLUID_INTERFACE);

        addAe2("illuminated_panel", AEParts.MONITOR,
                AEParts.SEMI_DARK_MONITOR,
                AEParts.DARK_MONITOR);

        addAe2("glass", Items.GLASS, Tags.Items.GLASS);

        addAe2("gears/wooden", AEItems.WOODEN_GEAR);
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

        addForge("silicon", AEItems.SILICON);

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

    @SuppressWarnings("unchecked")
    private void add(ResourceLocation tagName, Object... itemSources) {
        Builder<Item> builder = getOrCreateBuilder(net.minecraft.tags.ItemTags.createOptional(tagName));

        for (Object itemSource : itemSources) {
            if (itemSource instanceof IItemProvider) {
                builder.add(((IItemProvider) itemSource).asItem());
            } else if (itemSource instanceof ITag.INamedTag) {
                builder.addTag((ITag.INamedTag<Item>) itemSource);
            } else if (itemSource instanceof String) {
                String itemSourceString = (String) itemSource;
                if (itemSourceString.startsWith("#")) {
                    builder.add(new ITag.TagEntry(new ResourceLocation(itemSourceString.substring(1))));
                } else {
                    builder.add(new ITag.ItemEntry(new ResourceLocation(itemSourceString)));
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
                net.minecraft.tags.BlockTags.createOptional(tagName),
                net.minecraft.tags.ItemTags.createOptional(tagName));
    }

    @Override
    protected Path makePath(ResourceLocation id) {
        return this.generator.getOutputFolder()
                .resolve("data/" + id.getNamespace() + "/tags/items/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Item Tags";
    }
}
