package appeng.datagen.providers.models;

import static appeng.core.AppEng.makeId;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

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
    public ItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, AppEng.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerPaintballs();

        flatSingleLayer(BiometricCardModel.MODEL_BASE, "item/biometric_card");
        flatSingleLayer(MemoryCardModel.MODEL_BASE, "item/memory_card");

        crystalSeed(AEItems.CERTUS_CRYSTAL_SEED,
                "item/crystal_seed_certus",
                "item/crystal_seed_certus2",
                "item/crystal_seed_certus3");
        crystalSeed(AEItems.FLUIX_CRYSTAL_SEED,
                "item/crystal_seed_fluix",
                "item/crystal_seed_fluix2",
                "item/crystal_seed_fluix3");

        flatSingleLayer(AEItems.ADVANCED_CARD, "item/advanced_card");
        flatSingleLayer(AEItems.ANNIHILATION_CORE, "item/annihilation_core");
        flatSingleLayer(AEItems.BASIC_CARD, "item/basic_card");
        flatSingleLayer(AEItems.BLANK_PATTERN, "item/blank_pattern");
        flatSingleLayer(AEItems.CALCULATION_PROCESSOR, "item/calculation_processor");
        flatSingleLayer(AEItems.CALCULATION_PROCESSOR_PRESS, "item/calculation_processor_press");
        flatSingleLayer(AEItems.CALCULATION_PROCESSOR_PRINT, "item/printed_calculation_processor");
        flatSingleLayer(AEItems.CAPACITY_CARD, "item/card_capacity");
        flatSingleLayer(AEItems.ITEM_CELL_1K, "item/1k_item_storage_cell");
        flatSingleLayer(AEItems.ITEM_CELL_4K, "item/4k_item_storage_cell");
        flatSingleLayer(AEItems.ITEM_CELL_16K, "item/16k_item_storage_cell");
        flatSingleLayer(AEItems.ITEM_CELL_64K, "item/64k_item_storage_cell");
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
        flatSingleLayer(AEItems.DEBUG_PART_PLACER, "item/debug/part_placer");
        flatSingleLayer(AEItems.DEBUG_REPLICATOR_CARD, "item/debug/replicator_card");
        flatSingleLayer(AEItems.EMPTY_STORAGE_CELL, "item/empty_storage_cell");
        flatSingleLayer(AEItems.ENDER_DUST, "item/ender_dust");
        flatSingleLayer(AEItems.ENGINEERING_PROCESSOR, "item/engineering_processor");
        flatSingleLayer(AEItems.ENGINEERING_PROCESSOR_PRESS, "item/engineering_processor_press");
        flatSingleLayer(AEItems.ENGINEERING_PROCESSOR_PRINT, "item/printed_engineering_processor");
        flatSingleLayer(AEItems.FLUID_1K_CELL_COMPONENT, "item/1k_fluid_cell_component");
        flatSingleLayer(AEItems.FLUID_4K_CELL_COMPONENT, "item/4k_fluid_cell_component");
        flatSingleLayer(AEItems.FLUID_16K_CELL_COMPONENT, "item/16k_fluid_cell_component");
        flatSingleLayer(AEItems.FLUID_64K_CELL_COMPONENT, "item/64k_fluid_cell_component");
        flatSingleLayer(AEItems.FLUID_CELL1K, "item/1k_fluid_storage_cell");
        flatSingleLayer(AEItems.FLUID_CELL4K, "item/4k_fluid_storage_cell");
        flatSingleLayer(AEItems.FLUID_CELL16K, "item/16k_fluid_storage_cell");
        flatSingleLayer(AEItems.FLUID_CELL64K, "item/64k_fluid_storage_cell");
        flatSingleLayer(AEItems.FLUID_CELL_CREATIVE, "item/creative_fluid_cell");
        flatSingleLayer(AEItems.FLUIX_CRYSTAL, "item/fluix_crystal");
        flatSingleLayer(AEItems.FLUIX_DUST, "item/fluix_dust");
        flatSingleLayer(AEItems.FLUIX_PEARL, "item/fluix_pearl");
        flatSingleLayer(AEItems.FORMATION_CORE, "item/formation_core");
        flatSingleLayer(AEItems.FUZZY_CARD, "item/card_fuzzy");
        flatSingleLayer(AEItems.INVERTER_CARD, "item/card_inverter");
        flatSingleLayer(AEItems.ITEM_16K_CELL_COMPONENT, "item/16k_item_cell_component");
        flatSingleLayer(AEItems.ITEM_1K_CELL_COMPONENT, "item/1k_item_cell_component");
        flatSingleLayer(AEItems.ITEM_4K_CELL_COMPONENT, "item/4k_item_cell_component");
        flatSingleLayer(AEItems.ITEM_64K_CELL_COMPONENT, "item/64k_item_cell_component");
        flatSingleLayer(AEItems.ITEM_CELL_CREATIVE, "item/creative_item_cell");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR, "item/logic_processor");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR_PRESS, "item/logic_processor_press");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR_PRINT, "item/printed_logic_processor");
        flatSingleLayer(AEItems.MATTER_BALL, "item/matter_ball");
        flatSingleLayer(AEItems.NAME_PRESS, "item/name_press");
        flatSingleLayer(AEItems.NETHER_QUARTZ_KNIFE, "item/nether_quartz_cutting_knife");
        flatSingleLayer(AEItems.NETHER_QUARTZ_WRENCH, "item/nether_quartz_wrench");
        flatSingleLayer(AEItems.NETWORK_TOOL, "item/network_tool");
        flatSingleLayer(AEItems.PORTABLE_ITEM_CELL1K, "item/1k_portable_item_cell");
        flatSingleLayer(AEItems.PORTABLE_ITEM_CELL4k, "item/4k_portable_item_cell");
        flatSingleLayer(AEItems.PORTABLE_ITEM_CELL16K, "item/16k_portable_item_cell");
        flatSingleLayer(AEItems.PORTABLE_ITEM_CELL64K, "item/64k_portable_item_cell");
        flatSingleLayer(AEItems.PORTABLE_FLUID_CELL1K, "item/1k_portable_fluid_cell");
        flatSingleLayer(AEItems.PORTABLE_FLUID_CELL4k, "item/4k_portable_fluid_cell");
        flatSingleLayer(AEItems.PORTABLE_FLUID_CELL16K, "item/16k_portable_fluid_cell");
        flatSingleLayer(AEItems.PORTABLE_FLUID_CELL64K, "item/64k_portable_fluid_cell");
        flatSingleLayer(AEItems.PROCESSING_PATTERN, "item/processing_pattern");
        flatSingleLayer(AEItems.QUANTUM_ENTANGLED_SINGULARITY, "item/quantum_entangled_singularity");
        flatSingleLayer(AEItems.REDSTONE_CARD, "item/card_redstone");
        flatSingleLayer(AEItems.SILICON, "item/silicon");
        flatSingleLayer(AEItems.SILICON_PRESS, "item/silicon_press");
        flatSingleLayer(AEItems.SILICON_PRINT, "item/printed_silicon");
        flatSingleLayer(AEItems.SINGULARITY, "item/singularity");
        flatSingleLayer(AEItems.SKY_DUST, "item/sky_dust");
        flatSingleLayer(AEItems.SPATIAL_2_CELL_COMPONENT, "item/2_cubed_spatial_cell_component");
        flatSingleLayer(AEItems.SPATIAL_16_CELL_COMPONENT, "item/16_cubed_spatial_cell_component");
        flatSingleLayer(AEItems.SPATIAL_128_CELL_COMPONENT, "item/128_cubed_spatial_cell_component");
        flatSingleLayer(AEItems.SPATIAL_CELL2, "item/2_cubed_spatial_storage_cell");
        flatSingleLayer(AEItems.SPATIAL_CELL16, "item/16_cubed_spatial_storage_cell");
        flatSingleLayer(AEItems.SPATIAL_CELL128, "item/128_cubed_spatial_storage_cell");
        flatSingleLayer(AEItems.SPEED_CARD, "item/card_speed");
        flatSingleLayer(AEItems.VIEW_CELL, "item/view_cell");
        flatSingleLayer(AEItems.WIRELESS_BOOSTER, "item/wireless_booster");
        flatSingleLayer(AEItems.WIRELESS_CRAFTING_TERMINAL, "item/wireless_crafting_terminal");
        flatSingleLayer(AEItems.WIRELESS_RECEIVER, "item/wireless_receiver");
        flatSingleLayer(AEItems.WIRELESS_TERMINAL, "item/wireless_terminal");
        registerEmptyModel(AEItems.WRAPPED_GENERIC_STACK);
        registerEmptyModel(AEBlocks.CABLE_BUS);
        registerHandheld();
    }

    /**
     * Define a crystal seed item model with three growth stages shown by three textures. The fully grown crystal is not
     * part of this model.
     */
    private void crystalSeed(ItemDefinition<?> seed,
            String texture0,
            String texture1,
            String texture2) {

        var baseId = seed.id().getPath();

        var model1 = flatSingleLayer(makeId(baseId + "_1"), texture1);
        var model2 = flatSingleLayer(makeId(baseId + "_2"), texture2);

        withExistingParent(baseId, "item/generated")
                .texture("layer0", makeId(texture0))
                // 2nd growth stage
                .override()
                .predicate(InitItemModelsProperties.GROWTH_PREDICATE_ID, 0.333f)
                .model(model1)
                .end()
                // 3rd growth stage
                .override()
                .predicate(InitItemModelsProperties.GROWTH_PREDICATE_ID, 0.666f)
                .model(model2)
                .end();
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
}
