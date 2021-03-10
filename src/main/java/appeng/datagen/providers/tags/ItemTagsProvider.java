package appeng.datagen.providers.tags;

import java.nio.file.Path;

import net.minecraft.data.TagsProvider.Builder;
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
import appeng.datagen.providers.IAE2DataProvider;

public class ItemTagsProvider extends net.minecraft.data.ItemTagsProvider implements IAE2DataProvider {

    public ItemTagsProvider(GatherDataEvent dataEvent, BlockTagsProvider blockTagsProvider) {
        super(dataEvent.getGenerator(), blockTagsProvider, AppEng.MOD_ID, dataEvent.getExistingFileHelper());
    }

    @Override
    protected void addTags() {
        registerForgeTags();

        addAe2("blacklisted/annihilation_plane");

        addAe2("dusts/certus_quartz", MATERIALS.certusQuartzDust());
        addAe2("dusts/ender", MATERIALS.enderDust());
        addAe2("dusts/fluix", MATERIALS.fluixDust());
        addAe2("dusts/quartz", "#appliedenergistics2:dusts/certus_quartz", "#forge:dusts/quartz");

        addAe2("crystals/certus",
                "#appliedenergistics2:crystals/certus_quartz",
                MATERIALS.certusQuartzCrystalCharged(),
                MATERIALS.purifiedCertusQuartzCrystal());
        addAe2("crystals/certus_quartz",
                MATERIALS.certusQuartzCrystal());
        addAe2("crystals/fluix",
                MATERIALS.fluixCrystal(),
                MATERIALS.purifiedFluixCrystal());
        addAe2("crystals/nether",
                Tags.Items.GEMS_QUARTZ,
                MATERIALS.purifiedNetherQuartzCrystal());
        addAe2("crystals/quartz",
                Tags.Items.GEMS_QUARTZ,
                "#appliedenergistics2:crystals/certus_quartz",
                MATERIALS.certusQuartzCrystalCharged());

        addAe2("workbench", Items.CRAFTING_TABLE);
        addAe2("wool", ItemTags.WOOL);

        for (AEColor color : AEColor.values()) {
            addAe2("smart_dense_cable", PARTS.cableDenseSmart().item(color));
            addAe2("smart_cable", PARTS.cableSmart().item(color));
            addAe2("glass_cable", PARTS.cableGlass().item(color));
            addAe2("covered_cable", PARTS.cableCovered().item(color));
            addAe2("covered_dense_cable", PARTS.cableDenseCovered().item(color));
        }

        addAe2("silicon", MATERIALS.silicon());
        addAe2("quartz_wrench", ITEMS.certusQuartzWrench(), ITEMS.netherQuartzWrench());
        addAe2("knife", ITEMS.certusQuartzKnife(), ITEMS.netherQuartzKnife());

        addAe2("nether_quartz_dust", MATERIALS.netherQuartzDust());

        addAe2("metal_ingots", Tags.Items.INGOTS_GOLD, Tags.Items.INGOTS_IRON);

        addAe2("interface", PARTS.iface(), BLOCKS.iface());
        addAe2("fluid_interface", PARTS.fluidIface(), BLOCKS.fluidIface());

        addAe2("illuminated_panel", PARTS.monitor(),
                PARTS.semiDarkMonitor(),
                PARTS.darkMonitor());

        addAe2("glass", Items.GLASS, Tags.Items.GLASS);

        addAe2("gears/wooden", MATERIALS.woodenGear());
    }

    /**
     * Adds our items to common Forge tags.
     */
    private void registerForgeTags() {
        mirrorForgeBlockTag("ores");
        mirrorForgeBlockTag("ores/certus_quartz");

        mirrorForgeBlockTag("storage_blocks");
        mirrorForgeBlockTag("storage_blocks/certus_quartz");

        addForge("dusts/gold", MATERIALS.goldDust());
        addForge("dusts/iron", MATERIALS.ironDust());
        addForge("dusts/quartz", MATERIALS.netherQuartzDust());
        addForge("dusts/fluix", MATERIALS.fluixDust());
        addForge("dusts/certus_quartz", MATERIALS.certusQuartzDust());

        addForge("silicon", MATERIALS.silicon());

        addForge("gems/fluix", MATERIALS.fluixCrystal());
        addForge("gems/certus_quartz", MATERIALS.certusQuartzCrystal(),
                MATERIALS.certusQuartzCrystalCharged());
    }

    private void addForge(String tagName, Object... itemSources) {
        add(new ResourceLocation("forge", tagName), itemSources);
    }

    private void addAe2(String tagName, Object... itemSources) {
        add(AppEng.makeId(tagName), itemSources);
    }

    @SuppressWarnings("unchecked")
    private void add(ResourceLocation tagName, Object... itemSources) {
        Builder<Item> builder = tag(net.minecraft.tags.ItemTags.createOptional(tagName));

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
    protected Path getPath(ResourceLocation id) {
        return this.generator.getOutputFolder()
                .resolve("data/" + id.getNamespace() + "/tags/items/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Item Tags";
    }
}
