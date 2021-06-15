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
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.api.definitions.ApiMaterials;
import appeng.core.api.definitions.ApiParts;
import appeng.datagen.providers.IAE2DataProvider;

public class ItemTagsProvider extends net.minecraft.data.ItemTagsProvider implements IAE2DataProvider {

    public ItemTagsProvider(GatherDataEvent dataEvent, BlockTagsProvider blockTagsProvider) {
        super(dataEvent.getGenerator(), blockTagsProvider, AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void registerTags() {
        registerForgeTags();

        addAe2("blacklisted/annihilation_plane");

        addAe2("dusts/certus_quartz", ApiMaterials.certusQuartzDust());
        addAe2("dusts/ender", ApiMaterials.enderDust());
        addAe2("dusts/fluix", ApiMaterials.fluixDust());
        addAe2("dusts/quartz", "#appliedenergistics2:dusts/certus_quartz", "#forge:dusts/quartz");

        addAe2("crystals/certus",
                "#appliedenergistics2:crystals/certus_quartz",
                ApiMaterials.certusQuartzCrystalCharged(),
                ApiMaterials.purifiedCertusQuartzCrystal());
        addAe2("crystals/certus_quartz",
                ApiMaterials.certusQuartzCrystal());
        addAe2("crystals/fluix",
                ApiMaterials.fluixCrystal(),
                ApiMaterials.purifiedFluixCrystal());
        addAe2("crystals/nether",
                Tags.Items.GEMS_QUARTZ,
                ApiMaterials.purifiedNetherQuartzCrystal());
        addAe2("crystals/quartz",
                Tags.Items.GEMS_QUARTZ,
                "#appliedenergistics2:crystals/certus_quartz",
                ApiMaterials.certusQuartzCrystalCharged());

        addAe2("workbench", Items.CRAFTING_TABLE);
        addAe2("wool", ItemTags.WOOL);

        for (AEColor color : AEColor.values()) {
            addAe2("smart_dense_cable", ApiParts.cableDenseSmart().item(color));
            addAe2("smart_cable", ApiParts.cableSmart().item(color));
            addAe2("glass_cable", ApiParts.cableGlass().item(color));
            addAe2("covered_cable", ApiParts.cableCovered().item(color));
            addAe2("covered_dense_cable", ApiParts.cableDenseCovered().item(color));
        }

        addAe2("silicon", ApiMaterials.silicon());
        addAe2("quartz_wrench", ApiItems.certusQuartzWrench(), ApiItems.netherQuartzWrench());
        addAe2("knife", ApiItems.certusQuartzKnife(), ApiItems.netherQuartzKnife());

        addAe2("nether_quartz_dust", ApiMaterials.netherQuartzDust());

        addAe2("metal_ingots", Tags.Items.INGOTS_GOLD, Tags.Items.INGOTS_IRON);

        addAe2("interface", ApiParts.iface(), ApiBlocks.iface());
        addAe2("fluid_interface", ApiParts.fluidIface(), ApiBlocks.fluidIface());

        addAe2("illuminated_panel", ApiParts.monitor(),
                ApiParts.semiDarkMonitor(),
                ApiParts.darkMonitor());

        addAe2("glass", Items.GLASS, Tags.Items.GLASS);

        addAe2("gears/wooden", ApiMaterials.woodenGear());
    }

    /**
     * Adds our items to common Forge tags.
     */
    private void registerForgeTags() {
        mirrorForgeBlockTag("ores");
        mirrorForgeBlockTag("ores/certus_quartz");

        mirrorForgeBlockTag("storage_blocks");
        mirrorForgeBlockTag("storage_blocks/certus_quartz");

        addForge("dusts/gold", ApiMaterials.goldDust());
        addForge("dusts/iron", ApiMaterials.ironDust());
        addForge("dusts/quartz", ApiMaterials.netherQuartzDust());
        addForge("dusts/fluix", ApiMaterials.fluixDust());
        addForge("dusts/certus_quartz", ApiMaterials.certusQuartzDust());

        addForge("silicon", ApiMaterials.silicon());

        addForge("gems/fluix", ApiMaterials.fluixCrystal());
        addForge("gems/certus_quartz", ApiMaterials.certusQuartzCrystal(),
                ApiMaterials.certusQuartzCrystalCharged());
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
