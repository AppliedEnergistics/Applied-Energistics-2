package appeng.datagen.providers.models;

import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ItemLike;

import appeng.api.util.AEColor;
import appeng.client.item.ColorApplicatorItemModel;
import appeng.client.item.PortableCellColorTintSource;
import appeng.client.item.StorageCellStateTintSource;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.model.MemoryCardItemModel;
import appeng.client.render.model.MeteoriteCompassModel;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;

public class ItemModelProvider extends ModelSubProvider {
    public ItemModelProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
            PartModelOutput partModels) {
        super(blockModels, itemModels, partModels);
    }

    @Override
    protected void register() {
        registerPaintballs();

        flatSingleLayer(AEItems.MISSING_CONTENT, "minecraft:item/barrier");

        builtInItemModel(AEItems.FACADE, new FacadeItemModel.Unbaked());
        builtInItemModel(AEItems.METEORITE_COMPASS, new MeteoriteCompassModel.Unbaked());

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
        flatSingleLayer(AEItems.FLUID_CELL_HOUSING, "item/fluid_cell_housing");
        flatSingleLayer(AEItems.FLUIX_CRYSTAL, "item/fluix_crystal");
        flatSingleLayer(AEItems.FLUIX_DUST, "item/fluix_dust");
        flatSingleLayer(AEItems.FLUIX_PEARL, "item/fluix_pearl");
        flatSingleLayer(AEItems.FLUIX_UPGRADE_SMITHING_TEMPLATE, "item/fluix_upgrade_smithing_template");
        flatSingleLayer(AEItems.FORMATION_CORE, "item/formation_core");
        flatSingleLayer(AEItems.FUZZY_CARD, "item/card_fuzzy");
        flatSingleLayer(AEItems.INVERTER_CARD, "item/card_inverter");
        flatSingleLayer(AEItems.CELL_COMPONENT_16K, "item/cell_component_16k");
        flatSingleLayer(AEItems.CELL_COMPONENT_1K, "item/cell_component_1k");
        flatSingleLayer(AEItems.CELL_COMPONENT_4K, "item/cell_component_4k");
        flatSingleLayer(AEItems.CELL_COMPONENT_64K, "item/cell_component_64k");
        flatSingleLayer(AEItems.CELL_COMPONENT_256K, "item/cell_component_256k");
        flatSingleLayer(AEItems.CREATIVE_CELL, "item/creative_storage_cell");
        flatSingleLayer(AEItems.ITEM_CELL_HOUSING, "item/item_cell_housing");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR, "item/logic_processor");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR_PRESS, "item/logic_processor_press");
        flatSingleLayer(AEItems.LOGIC_PROCESSOR_PRINT, "item/printed_logic_processor");
        flatSingleLayer(AEItems.MATTER_BALL, "item/matter_ball");
        flatSingleLayer(AEItems.NAME_PRESS, "item/name_press");
        portableCell(AEItems.PORTABLE_ITEM_CELL1K, "item", "1k");
        portableCell(AEItems.PORTABLE_ITEM_CELL4K, "item", "4k");
        portableCell(AEItems.PORTABLE_ITEM_CELL16K, "item", "16k");
        portableCell(AEItems.PORTABLE_ITEM_CELL64K, "item", "64k");
        portableCell(AEItems.PORTABLE_ITEM_CELL256K, "item", "256k");
        portableCell(AEItems.PORTABLE_FLUID_CELL1K, "fluid", "1k");
        portableCell(AEItems.PORTABLE_FLUID_CELL4K, "fluid", "4k");
        portableCell(AEItems.PORTABLE_FLUID_CELL16K, "fluid", "16k");
        portableCell(AEItems.PORTABLE_FLUID_CELL64K, "fluid", "64k");
        portableCell(AEItems.PORTABLE_FLUID_CELL256K, "fluid", "256k");
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
        flatSingleLayer(AEItems.GUIDE, "item/guide");
        flatSingleLayer(AEItems.VIEW_CELL, "item/view_cell");
        flatSingleLayer(AEItems.WIRELESS_BOOSTER, "item/wireless_booster");
        flatSingleLayer(AEItems.WIRELESS_CRAFTING_TERMINAL, "item/wireless_crafting_terminal");
        flatSingleLayer(AEItems.WIRELESS_RECEIVER, "item/wireless_receiver");
        flatSingleLayer(AEItems.WIRELESS_TERMINAL, "item/wireless_terminal");
        registerEmptyModel(AEItems.WRAPPED_GENERIC_STACK);
        registerEmptyModel(AEBlocks.CABLE_BUS.item());
        registerHandheld();

        memoryCard();
        itemModels.itemModelOutput.accept(
                AEItems.COLOR_APPLICATOR.asItem(),
                new ColorApplicatorItemModel.Unbaked(
                        ModelLocationUtils.getModelLocation(AEItems.COLOR_APPLICATOR.asItem()),
                        ModelLocationUtils.getModelLocation(AEItems.COLOR_APPLICATOR.asItem(), "_colored")));
        itemModels.declareCustomModelItem(AEItems.MATTER_CANNON.asItem());
        itemModels.declareCustomModelItem(AEItems.NETWORK_TOOL.asItem());
    }

    private void memoryCard() {
        // Create the base models
        var baseModel = ModelTemplates.TWO_LAYERED_ITEM.create(
                ModelLocationUtils.getModelLocation(AEItems.MEMORY_CARD.asItem(), "_base"),
                TextureMapping.layered(makeId("item/memory_card_base"), makeId("item/memory_card_led")),
                modelOutput);
        itemModels.itemModelOutput.accept(
                AEItems.MEMORY_CARD.asItem(),
                new MemoryCardItemModel.Unbaked(
                        baseModel,
                        makeId("item/memory_card_hash"))

        );
    }

    private void storageCell(ItemDefinition<?> item, String background) {
        var model = itemModels.generateLayeredItem(
                item.asItem(),
                makeId(background),
                makeId("item/storage_cell_led"));
        itemModels.itemModelOutput.accept(item.asItem(), ItemModelUtils.tintedModel(
                model,
                new Constant(-1),
                new StorageCellStateTintSource()));
    }

    public static final TextureSlot LAYER3 = TextureSlot.create("layer3");
    private static final ModelTemplate FOUR_LAYERED_ITEM = ModelTemplates.createItem("generated", TextureSlot.LAYER0,
            TextureSlot.LAYER1, TextureSlot.LAYER2, LAYER3);

    private void portableCell(ItemDefinition<?> item, String housingType, String tier) {

        var model = FOUR_LAYERED_ITEM.create(
                item.asItem(),
                TextureMapping.layered(
                        makeId("item/portable_cell_%s_housing".formatted(housingType)),
                        makeId("item/portable_cell_led"),
                        makeId("item/portable_cell_screen"))
                        .put(LAYER3, makeId("item/portable_cell_side_%s".formatted(tier))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item.asItem(), ItemModelUtils.tintedModel(
                model,
                new Constant(-1),
                new StorageCellStateTintSource(),
                new PortableCellColorTintSource()));
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
    }

    private void handheld(ItemDefinition<?> item) {
        itemModels.generateFlatItem(item.asItem(), ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    private void registerEmptyModel(ItemDefinition<?> item) {
        itemModels.itemModelOutput.accept(item.asItem(), new EmptyModel.Unbaked());
    }

    /**
     * Note that color is applied to the textures in {@link appeng.init.client.InitItemColors}.
     */
    private void registerPaintballs() {
        var baseModel = ModelTemplates.FLAT_ITEM.create(AppEng.makeId("item/paint_ball"),
                TextureMapping.layer0(AppEng.makeId("item/paint_ball")), this.modelOutput);
        var lumenModel = ModelTemplates.FLAT_ITEM.create(AppEng.makeId("item/paint_ball_shimmer"),
                TextureMapping.layer0(AppEng.makeId("item/paint_ball_shimmer")), this.modelOutput);

        for (var color : AEColor.values()) {
            if (color == AEColor.TRANSPARENT) {
                continue;
            }

            itemModels.itemModelOutput.accept(
                    AEItems.COLORED_PAINT_BALL.item(color),
                    ItemModelUtils.tintedModel(baseModel, new Constant(color.mediumVariant)));
            var lumenColor = getLumenTintColor(color);
            itemModels.itemModelOutput.accept(
                    AEItems.COLORED_LUMEN_PAINT_BALL.item(color),
                    ItemModelUtils.tintedModel(lumenModel, new Constant(lumenColor)));
        }
    }

    private static int getLumenTintColor(AEColor color) {
        // We use a white base item icon for paint balls. This applies the correct color to it.
        final int colorValue = color.mediumVariant;
        final int r = ARGB.red(colorValue);
        final int g = ARGB.green(colorValue);
        final int b = ARGB.blue(colorValue);
        float fail = 0.7f;
        int full = (int) (255 * 0.3);
        return ARGB.color(
                (int) (full + r * fail),
                (int) (full + g * fail),
                (int) (full + b * fail));
    }

    private void flatSingleLayer(ItemLike item, String texture) {
        var model = ModelTemplates.FLAT_ITEM.create(item.asItem(), TextureMapping.layer0(makeId(texture)),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item.asItem(), ItemModelUtils.plainModel(model));
    }

    private void builtInItemModel(ItemLike item, ItemModel.Unbaked unbaked) {
        blockModels.itemModelOutput.accept(item.asItem(), unbaked);
    }

    private static ResourceLocation makeId(String id) {
        return id.contains(":") ? ResourceLocation.parse(id) : AppEng.makeId(id);
    }
}
