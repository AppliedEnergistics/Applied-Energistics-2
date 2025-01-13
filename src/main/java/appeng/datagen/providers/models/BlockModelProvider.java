package appeng.datagen.providers.models;

import static appeng.core.AppEng.makeId;

import java.util.ArrayList;
import java.util.function.Function;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.api.orientation.BlockOrientation;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.WirelessAccessPointBlock;
import appeng.block.spatial.SpatialAnchorBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.storage.IOPortBlock;
import appeng.block.storage.MEChestBlock;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.init.client.InitItemModelsProperties;

public class BlockModelProvider extends AE2BlockStateProvider {

    public BlockModelProvider(PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, AppEng.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        emptyModel(AEBlocks.MATRIX_FRAME);

        // These models will be overwritten in code
        builtInModel(AEBlocks.QUARTZ_GLASS, true);
        builtInModel(AEBlocks.CABLE_BUS);
        builtInModel(AEBlocks.PAINT);

        var driveModel = builtInBlockModel("drive");
        multiVariantGenerator(AEBlocks.DRIVE, Variant.variant().with(VariantProperties.MODEL, driveModel.getLocation()))
                .with(createFacingSpinDispatch());

        driveCells("item", 0);
        driveCells("fluid", 2);

        driveCell("creative_cell", 6);

        var charger = models().getExistingFile(AppEng.makeId("charger"));
        multiVariantGenerator(AEBlocks.CHARGER, Variant.variant().with(VariantProperties.MODEL, charger.getLocation()))
                .with(createFacingSpinDispatch());

        var inscriber = models().getExistingFile(AppEng.makeId("inscriber"));
        multiVariantGenerator(AEBlocks.INSCRIBER,
                Variant.variant().with(VariantProperties.MODEL, inscriber.getLocation()))
                .with(createFacingSpinDispatch());

        crystalResonanceGenerator();
        wirelessAccessPoint();
        craftingMonitor();
        quartzGrowthAccelerator();
        meChest();
        patternProvider();
        vibrationChamber();
        spatialAnchor();
        patternProvider();
        ioPort();
        spatialIoPort();

        builtInBlockModel("spatial_pylon");
        builtInBlockModel("qnb/qnb_formed");
        builtInBlockModel("crafting/unit_formed");
        builtInBlockModel("crafting/accelerator_formed");
        builtInBlockModel("crafting/1k_storage_formed");
        builtInBlockModel("crafting/4k_storage_formed");
        builtInBlockModel("crafting/16k_storage_formed");
        builtInBlockModel("crafting/64k_storage_formed");
        builtInBlockModel("crafting/256k_storage_formed");

        // Spatial pylon uses a normal model for the item, special model for block
        simpleBlock(AEBlocks.SPATIAL_PYLON.block(), models().getBuilder(modelPath(AEBlocks.SPATIAL_PYLON)));
        itemModels().cubeAll(modelPath(AEBlocks.SPATIAL_PYLON), makeId("block/spatial_pylon/spatial_pylon_item"));

        simpleBlockAndItem(AEBlocks.FLAWLESS_BUDDING_QUARTZ);
        simpleBlockAndItem(AEBlocks.FLAWED_BUDDING_QUARTZ);
        simpleBlockAndItem(AEBlocks.CHIPPED_BUDDING_QUARTZ);
        simpleBlockAndItem(AEBlocks.DAMAGED_BUDDING_QUARTZ);

        generateQuartzCluster(AEBlocks.SMALL_QUARTZ_BUD);
        generateQuartzCluster(AEBlocks.MEDIUM_QUARTZ_BUD);
        generateQuartzCluster(AEBlocks.LARGE_QUARTZ_BUD);
        generateQuartzCluster(AEBlocks.QUARTZ_CLUSTER);

        simpleBlockAndItem(AEBlocks.CONDENSER);
        simpleBlockAndItem(AEBlocks.ENERGY_ACCEPTOR);
        simpleBlockAndItem(AEBlocks.INTERFACE);

        simpleBlockAndItem(AEBlocks.DEBUG_ITEM_GEN, "block/debug/item_gen");
        simpleBlockAndItem(AEBlocks.DEBUG_PHANTOM_NODE, "block/debug/phantom_node");
        simpleBlockAndItem(AEBlocks.DEBUG_CUBE_GEN, "block/debug/cube_gen");
        simpleBlockAndItem(AEBlocks.DEBUG_ENERGY_GEN, "block/debug/energy_gen");

        craftingModel(AEBlocks.CRAFTING_ACCELERATOR, "accelerator");
        craftingModel(AEBlocks.CRAFTING_UNIT, "unit");
        craftingModel(AEBlocks.CRAFTING_STORAGE_1K, "1k_storage");
        craftingModel(AEBlocks.CRAFTING_STORAGE_4K, "4k_storage");
        craftingModel(AEBlocks.CRAFTING_STORAGE_16K, "16k_storage");
        craftingModel(AEBlocks.CRAFTING_STORAGE_64K, "64k_storage");
        craftingModel(AEBlocks.CRAFTING_STORAGE_256K, "256k_storage");

        simpleBlockAndItem(AEBlocks.CELL_WORKBENCH, models().cubeBottomTop(
                modelPath(AEBlocks.CELL_WORKBENCH),
                makeId("block/cell_workbench_side"),
                makeId("block/generics/bottom"),
                makeId("block/cell_workbench_top")));

        energyCell(AEBlocks.ENERGY_CELL, "block/energy_cell");
        energyCell(AEBlocks.DENSE_ENERGY_CELL, "block/dense_energy_cell");
        simpleBlockAndItem(AEBlocks.CREATIVE_ENERGY_CELL, "block/creative_energy_cell");

        // Both use the same mysterious cube model
        simpleBlockAndItem(AEBlocks.MYSTERIOUS_CUBE, models().getExistingFile(makeId("block/mysterious_cube")));
        simpleBlockAndItem(AEBlocks.NOT_SO_MYSTERIOUS_CUBE, models().getExistingFile(makeId("block/mysterious_cube")));
    }

    private void meChest() {
        var multipart = multiPartGenerator(AEBlocks.ME_CHEST);
        withOrientations(
                multipart,
                Variant.variant().with(VariantProperties.MODEL, AppEng.makeId("block/chest/base")));
        withOrientations(
                multipart,
                () -> Condition.condition().term(MEChestBlock.LIGHTS_ON, false),
                Variant.variant().with(VariantProperties.MODEL, AppEng.makeId("block/chest/lights_off")));
        withOrientations(
                multipart,
                () -> Condition.condition().term(MEChestBlock.LIGHTS_ON, true),
                Variant.variant().with(VariantProperties.MODEL, AppEng.makeId("block/chest/lights_on")));
    }

    private void quartzGrowthAccelerator() {
        var unpoweredModel = models().cubeBottomTop(
                modelPath(AEBlocks.GROWTH_ACCELERATOR),
                makeId("block/growth_accelerator_side"),
                makeId("block/growth_accelerator_bottom"),
                makeId("block/growth_accelerator_top"));
        var poweredModel = models().cubeBottomTop(
                modelPath(AEBlocks.GROWTH_ACCELERATOR) + "_on",
                makeId("block/growth_accelerator_side_on"),
                makeId("block/growth_accelerator_bottom"),
                makeId("block/growth_accelerator_top_on"));

        multiVariantGenerator(AEBlocks.GROWTH_ACCELERATOR)
                .with(createFacingDispatch(90, 0))
                .with(PropertyDispatch.property(GrowthAcceleratorBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, unpoweredModel.getLocation()))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, poweredModel.getLocation())));

        itemModels().withExistingParent(modelPath(AEBlocks.GROWTH_ACCELERATOR), unpoweredModel.getLocation());
    }

    private void craftingMonitor() {
        var formedModel = AppEng.makeId("block/crafting/monitor_formed");
        var unformedModel = AppEng.makeId("block/crafting/monitor");

        multiVariantGenerator(AEBlocks.CRAFTING_MONITOR)
                .with(PropertyDispatch.properties(AbstractCraftingUnitBlock.FORMED, BlockStateProperties.FACING)
                        .generate((formed, facing) -> {
                            if (formed) {
                                return Variant.variant().with(VariantProperties.MODEL, formedModel);
                            } else {
                                return applyOrientation(
                                        Variant.variant().with(VariantProperties.MODEL, unformedModel),
                                        BlockOrientation.get(facing));
                            }
                        }));
    }

    private void crystalResonanceGenerator() {
        var builder = getVariantBuilder(AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block());

        var modelFile = models().getExistingFile(AppEng.makeId("block/crystal_resonance_generator"));

        for (var facing : Direction.values()) {
            var rotation = BlockOrientation.get(facing, 0);
            builder.partialState()
                    .with(BlockStateProperties.FACING, facing)
                    .setModels(ConfiguredModel.builder()
                            .modelFile(modelFile)
                            // The original is facing "up" while we generally assume models are facing north
                            // but this looks better as an item model
                            .rotationX(rotation.getAngleX() + 90)
                            .rotationY(rotation.getAngleY())
                            .build());
        }

        simpleBlockItem(AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block(), modelFile);
    }

    private void wirelessAccessPoint() {
        var builder = getMultipartBuilder(AEBlocks.WIRELESS_ACCESS_POINT.block());

        var chassis = models().getExistingFile(AppEng.makeId("block/wireless_access_point_chassis"));
        var antennaOff = models().getExistingFile(AppEng.makeId("block/wireless_access_point_off"));
        var antennaOn = models().getExistingFile(AppEng.makeId("block/wireless_access_point_on"));
        var statusOff = models().getExistingFile(AppEng.makeId("block/wireless_access_point_status_off"));
        var statusOn = models().getExistingFile(AppEng.makeId("block/wireless_access_point_status_has_channel"));

        for (var facing : Direction.values()) {
            var rotation = BlockOrientation.get(facing, 0);

            Function<ModelFile, MultiPartBlockStateBuilder.PartBuilder> addModel = modelFile -> builder.part()
                    .modelFile(modelFile)
                    .rotationX(rotation.getAngleX())
                    .rotationY(rotation.getAngleY())
                    .addModel()
                    .condition(BlockStateProperties.FACING, facing);

            addModel.apply(chassis).end();

            addModel.apply(antennaOff).condition(WirelessAccessPointBlock.STATE, WirelessAccessPointBlock.State.OFF)
                    .end();
            addModel.apply(statusOff).condition(WirelessAccessPointBlock.STATE, WirelessAccessPointBlock.State.OFF)
                    .end();

            addModel.apply(antennaOff).condition(WirelessAccessPointBlock.STATE, WirelessAccessPointBlock.State.ON)
                    .end();
            addModel.apply(statusOn).condition(WirelessAccessPointBlock.STATE, WirelessAccessPointBlock.State.ON).end();

            addModel.apply(antennaOn)
                    .condition(WirelessAccessPointBlock.STATE, WirelessAccessPointBlock.State.HAS_CHANNEL).end();
            addModel.apply(statusOn)
                    .condition(WirelessAccessPointBlock.STATE, WirelessAccessPointBlock.State.HAS_CHANNEL).end();
        }
    }

    private void vibrationChamber() {
        var offModel = models().cube(
                modelPath(AEBlocks.VIBRATION_CHAMBER),
                makeId("block/generics/bottom"),
                makeId("block/vibration_chamber_top"),
                makeId("block/vibration_chamber_front"),
                makeId("block/vibration_chamber_back"),
                makeId("block/vibration_chamber_side"),
                makeId("block/vibration_chamber_side")).texture("particle", makeId("block/vibration_chamber_front"));
        var onModel = models().cube(
                modelPath(AEBlocks.VIBRATION_CHAMBER) + "_on",
                makeId("block/generics/bottom"),
                makeId("block/vibration_chamber_top_on"),
                makeId("block/vibration_chamber_front_on"),
                makeId("block/vibration_chamber_back_on"),
                makeId("block/vibration_chamber_side"),
                makeId("block/vibration_chamber_side")).texture("particle", makeId("block/vibration_chamber_front_on"));

        multiVariantGenerator(AEBlocks.VIBRATION_CHAMBER)
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(VibrationChamberBlock.ACTIVE)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel.getLocation()))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel.getLocation())));

        itemModels().withExistingParent(modelPath(AEBlocks.VIBRATION_CHAMBER), offModel.getLocation());
    }

    private void spatialAnchor() {
        var offModel = getExistingModel("block/spatial_anchor");
        var onModel = getExistingModel("block/spatial_anchor_on");

        multiVariantGenerator(AEBlocks.SPATIAL_ANCHOR)
                .with(createFacingDispatch(90, 0))
                .with(PropertyDispatch.property(SpatialAnchorBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel)));

        itemModels().withExistingParent(modelPath(AEBlocks.SPATIAL_ANCHOR), offModel);
    }

    private void patternProvider() {
        var def = AEBlocks.PATTERN_PROVIDER;
        var normalModel = cubeAll(def.block());
        simpleBlockItem(def.block(), normalModel);
        // the block state and the oriented model are in manually written json files

        var orientedModel = models().getExistingFile(AppEng.makeId("block/pattern_provider_oriented"));
        multiVariantGenerator(AEBlocks.PATTERN_PROVIDER, Variant.variant())
                .with(PropertyDispatch.property(PatternProviderBlock.PUSH_DIRECTION).generate(pushDirection -> {
                    var forward = pushDirection.getDirection();
                    if (forward == null) {
                        return Variant.variant().with(VariantProperties.MODEL, normalModel.getLocation());
                    } else {
                        var orientation = BlockOrientation.get(forward);
                        return applyRotation(
                                Variant.variant().with(VariantProperties.MODEL, orientedModel.getLocation()),
                                // + 90 because the default model is oriented UP, while block orientation assumes NORTH
                                orientation.getAngleX() + 90,
                                orientation.getAngleY(),
                                0);
                    }
                }));
    }

    private void ioPort() {
        var offModel = models().getExistingFile(AppEng.makeId("block/io_port"));
        var onModel = models().getExistingFile(AppEng.makeId("block/io_port_on"));

        multiVariantGenerator(AEBlocks.IO_PORT)
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(IOPortBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel.getLocation()))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel.getLocation())));
        itemModels().withExistingParent(modelPath(AEBlocks.IO_PORT), offModel.getLocation());
    }

    private void spatialIoPort() {
        var offModel = models().getExistingFile(AppEng.makeId("block/spatial_io_port"));
        var onModel = models().getExistingFile(AppEng.makeId("block/spatial_io_port_on"));

        multiVariantGenerator(AEBlocks.SPATIAL_IO_PORT)
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(SpatialIOPortBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel.getLocation()))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel.getLocation())));
        itemModels().withExistingParent(modelPath(AEBlocks.SPATIAL_IO_PORT), offModel.getLocation());
    }

    private String modelPath(BlockDefinition<?> block) {
        return block.id().getPath();
    }

    private void emptyModel(BlockDefinition<?> block) {
        var model = models().getBuilder(block.id().getPath());
        simpleBlockAndItem(block, model);
    }

    private void builtInModel(BlockDefinition<?> block) {
        builtInModel(block, false);
    }

    private void builtInModel(BlockDefinition<?> block, boolean skipItem) {
        var model = builtInBlockModel(block.id().getPath());
        getVariantBuilder(block.block()).partialState().setModels(new ConfiguredModel(model));

        if (!skipItem) {
            // The item model should not reference the block model since that will be replaced in-code
            itemModels().getBuilder(block.id().getPath());
        }
    }

    private BlockModelBuilder builtInBlockModel(String name) {
        return models().getBuilder("block/" + name);
    }

    private void energyCell(
            BlockDefinition<?> block,
            String baseTexture) {

        var blockBuilder = getVariantBuilder(block.block());
        var models = new ArrayList<ModelFile>();
        for (var i = 0; i < 5; i++) {
            var model = models().cubeAll(modelPath(block) + "_" + i, makeId(baseTexture + "_" + i));
            blockBuilder.partialState().with(EnergyCellBlock.ENERGY_STORAGE, i).setModels(new ConfiguredModel(model));
            models.add(model);
        }

        var item = itemModels().withExistingParent(modelPath(block), models.getFirst().getLocation());
        for (var i = 1; i < models.size(); i++) {
            // The predicate matches "greater than", meaning for fill-level > 0 the first non-empty texture is used
            float fillFactor = i / (float) models.size();
            item.override()
                    .predicate(InitItemModelsProperties.ENERGY_FILL_LEVEL_ID, fillFactor)
                    .model(models.get(i));
        }
    }

    private void craftingModel(BlockDefinition<?> block, String name) {
        var blockModel = models().cubeAll("block/crafting/" + name, makeId("block/crafting/" + name));
        getVariantBuilder(block.block())
                .partialState().with(AbstractCraftingUnitBlock.FORMED, false).setModels(
                        new ConfiguredModel(blockModel))
                .partialState().with(AbstractCraftingUnitBlock.FORMED, true).setModels(
                        // Empty model, will be replaced dynamically
                        new ConfiguredModel(models().getBuilder("block/crafting/" + name + "_formed")));
        simpleBlockItem(block.block(), blockModel);
    }

    private void driveCells(String type, int typeOffset) {
        driveCell("1k", type, typeOffset, 0);
        driveCell("4k", type, typeOffset, 2);
        driveCell("16k", type, typeOffset, 4);
        driveCell("64k", type, typeOffset, 6);
        driveCell("256k", type, typeOffset, 8);
    }

    private void driveCell(String tier, String type, int typeOffset, int tierOffset) {
        driveCell(tier + "_" + type + "_cell", typeOffset)
                .texture("tier", "block/drive/drive_cell_tiers")
                .element()
                .to(6, 2, 2)
                .face(Direction.NORTH)
                .uvs(0, tierOffset, 6, tierOffset + 2)
                .end()
                .face(Direction.UP)
                .uvs(6, tierOffset, 0, tierOffset + 2)
                .end()
                .face(Direction.DOWN)
                .uvs(6, tierOffset, 0, tierOffset + 2)
                .end()
                .faces((dir, builder) ->
                        builder.texture("#tier").cullface(Direction.NORTH).end())
                .end();
    }

    private BlockModelBuilder driveCell(String cell, int offset) {
        var texturePrefix = "block/drive/";
        return models().getBuilder(texturePrefix + "cells/" + cell)
                .ao(false)
                .texture("cell", texturePrefix + "drive_cells")
                .texture("particle", texturePrefix + "drive_cells")
                .element()
                .to(6, 2, 2)
                .face(Direction.NORTH)
                .uvs(0, offset, 6, offset + 2)
                .end()
                .face(Direction.UP)
                .uvs(6, offset, 0, offset + 2)
                .end()
                .face(Direction.DOWN)
                .uvs(6, offset, 0, offset + 2)
                .end()
                .faces((dir, builder) ->
                        builder.texture("#cell").cullface(Direction.NORTH).end())
                .end();
    }

    private void generateQuartzCluster(BlockDefinition<?> quartz) {
        var name = quartz.id().getPath();
        var texture = makeId("block/" + name);
        var model = models().cross(name, texture).renderType("cutout");
        directionalBlock(quartz.block(), model);
        itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", texture);
    }

    private ResourceLocation getExistingModel(String name) {
        return models().getExistingFile(AppEng.makeId(name)).getLocation();
    }
}
