package appeng.datagen.providers.models;

import static appeng.core.AppEng.makeId;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.api.ids.AEItemIds;
import appeng.api.util.AEColor;
import appeng.client.render.model.BiometricCardModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.init.client.InitItemModelsProperties;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider
        implements IAE2DataProvider {
    public ItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AppEng.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerPaintballs();

        flatSingleLayer(BiometricCardModel.MODEL_BASE, "item/biometric_card");
        builtInItemModel("biometric_card");

        for (AEColor color : AEColor.values()) {
            String builtInItemModelName = "memory_card"
                    + (color != AEColor.TRANSPARENT ? ("_" + color.registryPrefix) : "");
            flatSingleLayer(MemoryCardModel.MODELS_BASE.get(color), "item/" + builtInItemModelName);
            builtInItemModel(builtInItemModelName);
        }

        builtInItemModel("facade");
        builtInItemModel("meteorite_compass");

        flatSingleLayer(AEItems.ADVANCED_CARD, "item/advanced_card");
        flatSingleLayer(AEItems.VOID_CARD, "item/card_void");
        flatSingleLayer(AEItems.ANNIHILATION_CORE, "item/annihilation_core");
        flatSingleLayer(AEItems.BASIC_CARD, "item/basic_card");
        flatSingleLayer(AEItems.BLANK_PATTERN, "item/blank_pattern");
        flatSingleLayer(AEItems.CALCULATION_PROCESSOR, "item/calculation_processor");
        flatSingleLayer(AEItems.CALCULATION_PROCESSOR_PRESS, "item/calculation_processor_press");
        flatSingleLayer(AEItems.CALCULATION_PROCESSOR_PRINT, "item/printed_calculation_processor");
        flatSingleLayer(AEItems.CAPACITY_CARD, "item/card_capacity");
        storageCell(AEItems.ITEM_CELL_1K, "item/item_storage_cell_1k");
        storageCell(AEItems.ITEM_CELL_4K, "item/item_storage_cell_4k");
        storageCell(AEItems.ITEM_CELL_16K, "item/item_storage_cell_16k");
        storageCell(AEItems.ITEM_CELL_64K, "item/item_storage_cell_64k");
        storageCell(AEItems.ITEM_CELL_256K, "item/item_storage_cell_256k");
        flatSingleLayer(AEItems.CERTUS_QUARTZ_CRYSTAL, "item/certus_quartz_crystal");
        flatSingleLayer(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, "item/certus_quartz_crystal_charged");
        flatSingleLayer(AEItems.CERTUS_QUARTZ_DUST, "item/certus_quartz_dust");
        flatSingleLayer(AEItems.CERTUS_QUARTZ_KNIFE, "item/certus_quartz_cutting_knife");
        flatSingleLayer(AEItems.CERTUS_QUARTZ_WRENCH, "item/certus_quartz_wrench");
        flatSingleLayer(AEItems.CRAFTING_CARD, "item/card_crafting");
        flatSingleLayer(AEItems.CRAFTING_PATTERN, "item/crafting_pattern");
        flatSingleLayer(AEItems.DEBUG_CARD, "item/debug_card");
        flatSingleLayer(AEItems.DEBUG_ERASER, "item/debug/eraser");
        flatSingleLayer(AEItems.DEBUG_METEORITE_PLACER, "item/debug/meteorite_placer");
        flatSingleLayer(AEItems.DEBUG_REPLICATOR_CARD, "item/debug/replicator_card");
        flatSingleLayer(AEItems.ENDER_DUST, "item/ender_dust");
        flatSingleLayer(AEItems.ENERGY_CARD, "item/card_energy");
        flatSingleLayer(AEItems.ENGINEERING_PROCESSOR, "item/engineering_processor");
        flatSingleLayer(AEItems.ENGINEERING_PROCESSOR_PRESS, "item/engineering_processor_press");
        flatSingleLayer(AEItems.ENGINEERING_PROCESSOR_PRINT, "item/printed_engineering_processor");
        flatSingleLayer(AEItems.EQUAL_DISTRIBUTION_CARD, "item/card_equal_distribution");
        storageCell(AEItems.FLUID_CELL_1K, "item/fluid_storage_cell_1k");
        storageCell(AEItems.FLUID_CELL_4K, "item/fluid_storage_cell_4k");
        storageCell(AEItems.FLUID_CELL_16K, "item/fluid_storage_cell_16k");
        storageCell(AEItems.FLUID_CELL_64K, "item/fluid_storage_cell_64k");
        storageCell(AEItems.FLUID_CELL_256K, "item/fluid_storage_cell_256k");
        flatSingleLayer(AEItems.FLUID_CELL_CREATIVE, "item/creative_fluid_cell");
        flatSingleLayer(AEItems.FLUID_CELL_HOUSING, "item/fluid_cell_housing");
        flatSingleLayer(AEItems.FLUIX_CRYSTAL, "item/fluix_crystal");
        flatSingleLayer(AEItems.FLUIX_DUST, "item/fluix_dust");
        flatSingleLayer(AEItems.FLUIX_PEARL, "item/fluix_pearl");
        flatSingleLayer(AEItems.FORMATION_CORE, "item/formation_core");
        flatSingleLayer(AEItems.FUZZY_CARD, "item/card_fuzzy");
        flatSingleLayer(AEItems.INVERTER_CARD, "item/card_inverter");
        flatSingleLayer(AEItems.CELL_COMPONENT_16K, "item/cell_component_16k");
        flatSingleLayer(AEItems.CELL_COMPONENT_1K, "item/cell_component_1k");
        flatSingleLayer(AEItems.CELL_COMPONENT_4K, "item/cell_component_4k");
        flatSingleLayer(AEItems.CELL_COMPONENT_64K, "item/cell_component_64k");
        flatSingleLayer(AEItems.CELL_COMPONENT_256K, "item/cell_component_256k");
        flatSingleLayer(AEItems.ITEM_CELL_CREATIVE, "item/creative_item_cell");
        flatSingleLayer(AEItems.ITEM_CELL_HOUSING, "item/item_cell_housing");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR, "item/logic_processor");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR_PRESS, "item/logic_processor_press");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR_PRINT, "item/printed_logic_processor");
        flatSingleLayer(AEItems.MATTER_BALL, "item/matter_ball");
        flatSingleLayer(AEItems.NAME_PRESS, "item/name_press");
        flatSingleLayer(AEItems.NETHER_QUARTZ_KNIFE, "item/nether_quartz_cutting_knife");
        flatSingleLayer(AEItems.NETHER_QUARTZ_WRENCH, "item/nether_quartz_wrench");
        flatSingleLayer(AEItems.NETWORK_TOOL, "item/network_tool");
        portableCell(AEItems.PORTABLE_ITEM_CELL1K, "item/portable_item_cell_1k");
        portableCell(AEItems.PORTABLE_ITEM_CELL4K, "item/portable_item_cell_4k");
        portableCell(AEItems.PORTABLE_ITEM_CELL16K, "item/portable_item_cell_16k");
        portableCell(AEItems.PORTABLE_ITEM_CELL64K, "item/portable_item_cell_64k");
        portableCell(AEItems.PORTABLE_ITEM_CELL256K, "item/portable_item_cell_256k");
        portableCell(AEItems.PORTABLE_FLUID_CELL1K, "item/portable_fluid_cell_1k");
        portableCell(AEItems.PORTABLE_FLUID_CELL4K, "item/portable_fluid_cell_4k");
        portableCell(AEItems.PORTABLE_FLUID_CELL16K, "item/portable_fluid_cell_16k");
        portableCell(AEItems.PORTABLE_FLUID_CELL64K, "item/portable_fluid_cell_64k");
        portableCell(AEItems.PORTABLE_FLUID_CELL256K, "item/portable_fluid_cell_256k");
        flatSingleLayer(AEItems.PROCESSING_PATTERN, "item/processing_pattern");
        flatSingleLayer(AEItems.QUANTUM_ENTANGLED_SINGULARITY, "item/quantum_entangled_singularity");
        flatSingleLayer(AEItems.REDSTONE_CARD, "item/card_redstone");
        flatSingleLayer(AEItems.SILICON, "item/silicon");
        flatSingleLayer(AEItems.SILICON_PRESS, "item/silicon_press");
        flatSingleLayer(AEItems.SILICON_PRINT, "item/printed_silicon");
        flatSingleLayer(AEItems.SINGULARITY, "item/singularity");
        flatSingleLayer(AEItems.SKY_DUST, "item/sky_dust");
        flatSingleLayer(AEItems.SPATIAL_2_CELL_COMPONENT, "item/spatial_cell_component_2");
        flatSingleLayer(AEItems.SPATIAL_16_CELL_COMPONENT, "item/spatial_cell_component_16");
        flatSingleLayer(AEItems.SPATIAL_128_CELL_COMPONENT, "item/spatial_cell_component_128");
        flatSingleLayer(AEItems.SPATIAL_CELL2, "item/spatial_storage_cell_2");
        flatSingleLayer(AEItems.SPATIAL_CELL16, "item/spatial_storage_cell_16");
        flatSingleLayer(AEItems.SPATIAL_CELL128, "item/spatial_storage_cell_128");
        flatSingleLayer(AEItems.SPEED_CARD, "item/card_speed");
        flatSingleLayer(AEItems.SMITHING_TABLE_PATTERN, "item/smithing_table_pattern");
        flatSingleLayer(AEItems.STONECUTTING_PATTERN, "item/stonecutting_pattern");
        flatSingleLayer(AEItemIds.GUIDE, "item/guide");
        flatSingleLayer(AEItems.VIEW_CELL, "item/view_cell");
        flatSingleLayer(AEItems.WIRELESS_BOOSTER, "item/wireless_booster");
        flatSingleLayer(AEItems.WIRELESS_CRAFTING_TERMINAL, "item/wireless_crafting_terminal");
        flatSingleLayer(AEItems.WIRELESS_RECEIVER, "item/wireless_receiver");
        flatSingleLayer(AEItems.WIRELESS_TERMINAL, "item/wireless_terminal");
        registerEmptyModel(AEItems.WRAPPED_GENERIC_STACK);
        registerEmptyModel(AEBlocks.CABLE_BUS);
        registerHandheld();
    }

    private void storageCell(ItemDefinition<?> item, String background) {
        String id = item.id().getPath();
        singleTexture(
                id,
                mcLoc("item/generated"),
                "layer0",
                makeId(background))
                        .texture("layer1", "item/storage_cell_led");
    }

    private void portableCell(ItemDefinition<?> item, String background) {
        String id = item.id().getPath();
        singleTexture(
                id,
                mcLoc("item/generated"),
                "layer0",
                makeId(background))
                        .texture("layer1", "item/portable_cell_led");
    }

    private void registerHandheld() {
        handheld(AEItems.CERTUS_QUARTZ_AXE);
        handheld(AEItems.CERTUS_QUARTZ_HOE);
        handheld(AEItems.CERTUS_QUARTZ_SHOVEL);
        handheld(AEItems.CERTUS_QUARTZ_PICK);
        handheld(AEItems.CERTUS_QUARTZ_SWORD);
        handheld(AEItems.CERTUS_QUARTZ_WRENCH);
        handheld(AEItems.CERTUS_QUARTZ_KNIFE);
        handheld(AEItems.NETHER_QUARTZ_AXE);
        handheld(AEItems.NETHER_QUARTZ_HOE);
        handheld(AEItems.NETHER_QUARTZ_SHOVEL);
        handheld(AEItems.NETHER_QUARTZ_PICK);
        handheld(AEItems.NETHER_QUARTZ_SWORD);
        handheld(AEItems.NETHER_QUARTZ_WRENCH);
        handheld(AEItems.NETHER_QUARTZ_KNIFE);
        handheld(AEItems.FLUIX_AXE);
        handheld(AEItems.FLUIX_HOE);
        handheld(AEItems.FLUIX_SHOVEL);
        handheld(AEItems.FLUIX_PICK);
        handheld(AEItems.FLUIX_SWORD);
        handheld(AEItems.ENTROPY_MANIPULATOR);
        handheld(AEItems.CHARGED_STAFF);

        // The color applicator uses a separate model when colored
        var coloredColorApplicator = withExistingParent(AEItems.COLOR_APPLICATOR.id().getPath() + "_colored",
                "item/generated")
                        .texture("layer0", makeId("item/color_applicator"))
                        .texture("layer1", makeId("item/color_applicator_tip_dark"))
                        .texture("layer2", makeId("item/color_applicator_tip_medium"))
                        .texture("layer3", makeId("item/color_applicator_tip_bright"));
        withExistingParent(AEItems.COLOR_APPLICATOR.id().getPath(), "item/generated")
                .texture("layer0", makeId("item/color_applicator"))
                // Use different model when colored
                .override()
                .predicate(InitItemModelsProperties.COLORED_PREDICATE_ID, 1)
                .model(coloredColorApplicator)
                .end();
    }

    private void handheld(ItemDefinition<?> item) {
        singleTexture(
                item.id().getPath(),
                new ResourceLocation("item/handheld"),
                "layer0",
                makeId("item/" + item.id().getPath()));
    }

    private void registerEmptyModel(ItemDefinition<?> item) {
        this.getBuilder(item.id().getPath());
    }

    /**
     * Note that color is applied to the textures in {@link appeng.init.client.InitItemColors}.
     */
    private void registerPaintballs() {
        for (AEColor value : AEColor.values()) {
            var id = AEItems.COLORED_PAINT_BALL.id(value);
            if (id != null) {
                flatSingleLayer(id, "item/paint_ball");
            }
        }

        for (AEColor value : AEColor.values()) {
            var id = AEItems.COLORED_LUMEN_PAINT_BALL.id(value);
            if (id != null) {
                flatSingleLayer(id, "item/paint_ball_shimmer");
            }
        }
    }

    private ItemModelBuilder flatSingleLayer(ItemDefinition<?> item, String texture) {
        String id = item.id().getPath();
        return singleTexture(
                id,
                mcLoc("item/generated"),
                "layer0",
                makeId(texture));
    }

    private ItemModelBuilder flatSingleLayer(ResourceLocation id, String texture) {
        return singleTexture(
                id.getPath(),
                mcLoc("item/generated"),
                "layer0",
                makeId(texture));
    }

    private ItemModelBuilder builtInItemModel(String name) {
        var model = getBuilder("item/" + name);
        var loaderId = AppEng.makeId("item/" + name);
        model.customLoader((bmb, efh) -> new CustomLoaderBuilder<>(loaderId, bmb, efh) {
        });
        return model;
    }
}
