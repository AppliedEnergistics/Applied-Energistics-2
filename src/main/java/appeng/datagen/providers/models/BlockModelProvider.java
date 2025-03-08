package appeng.datagen.providers.models;

import appeng.api.orientation.BlockOrientation;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.block.networking.ControllerBlock;
import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.WirelessAccessPointBlock;
import appeng.block.qnb.QuantumLinkChamberBlock;
import appeng.block.qnb.QuantumRingBlock;
import appeng.block.spatial.SpatialAnchorBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.storage.IOPortBlock;
import appeng.block.storage.MEChestBlock;
import appeng.client.render.model.BuiltInModelLoaderBuilder;
import appeng.client.render.tesr.SkyStoneChestRenderer;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.Condition;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.BiConsumer;

import static appeng.core.AppEng.makeId;
import static net.minecraft.client.data.models.BlockModelGenerators.createSimpleBlock;

public class BlockModelProvider extends ModelSubProvider {

    public BlockModelProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        super(blockModels, itemModels);
    }

    @Override
    protected void register() {
        blockModels.createTrivialBlock(
                AEBlocks.MATRIX_FRAME.block(),
                TexturedModel.createDefault(block -> TRANSPARENT_PARTICLE, EMPTY_MODEL));

        // These models will be overwritten in code
        builtInModel(AEBlocks.QUARTZ_GLASS, true);
        blockModels.registerSimpleItemModel(
                AEBlocks.QUARTZ_GLASS.asItem(),
                ModelTemplates.CUBE_ALL.create(
                        AEBlocks.QUARTZ_GLASS.asItem(),
                        TextureMapping.cube(TextureMapping.getBlockTexture(AEBlocks.QUARTZ_GLASS.block(), "_item")),
                        modelOutput
                )
            );
        blockModels.copyModel(AEBlocks.QUARTZ_GLASS.block(), AEBlocks.QUARTZ_VIBRANT_GLASS.block());
        builtInModel(AEBlocks.CABLE_BUS, true);
        builtInModel(AEBlocks.PAINT);

        var driveModel = builtInModel("drive");
        multiVariantGenerator(AEBlocks.DRIVE, Variant.variant().with(VariantProperties.MODEL, driveModel))
                .with(createFacingSpinDispatch());

        var charger = makeId("block/charger");
        multiVariantGenerator(AEBlocks.CHARGER, Variant.variant().with(VariantProperties.MODEL, charger))
                .with(createFacingSpinDispatch());

        var inscriber = makeId("block/inscriber");
        multiVariantGenerator(AEBlocks.INSCRIBER,
                Variant.variant().with(VariantProperties.MODEL, inscriber))
                .with(createFacingSpinDispatch());

        multiVariantGenerator(AEBlocks.SKY_STONE_TANK, Variant.variant().with(VariantProperties.MODEL, makeId("block/sky_stone_tank")));
        multiVariantGenerator(AEBlocks.TINY_TNT, Variant.variant().with(VariantProperties.MODEL, makeId("block/tiny_tnt")));
        multiVariantGenerator(AEBlocks.MOLECULAR_ASSEMBLER, Variant.variant().with(VariantProperties.MODEL, makeId("block/molecular_assembler")));

        // Generate an empty block model for the crank, since the base model and shaft will be used by the dynamic renderer
        multiVariantGenerator(AEBlocks.CRANK, Variant.variant().with(VariantProperties.MODEL, makeId("block/crank")));

        crystalResonanceGenerator();
        wirelessAccessPoint();
        craftingMonitor();
        quartzGrowthAccelerator();
        meChest();
        vibrationChamber();
        spatialAnchor();
        patternProvider();
        ioPort();
        spatialIoPort();
        spatialPylon();

        blockModels.createChest(AEBlocks.SKY_STONE_CHEST.block(), AEBlocks.SKY_STONE_BLOCK.block(), SkyStoneChestRenderer.TEXTURE_STONE.texture(), false);
        blockModels.createChest(AEBlocks.SMOOTH_SKY_STONE_CHEST.block(), AEBlocks.SMOOTH_SKY_STONE_BLOCK.block(), SkyStoneChestRenderer.TEXTURE_BLOCK.texture(), false);

        quantumBridge();
        controller();

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

        simpleBlockAndItem(AEBlocks.CELL_WORKBENCH, TexturedModel.CUBE_TOP_BOTTOM
                .updateTexture(textures -> textures
                        .put(TextureSlot.TOP, makeId("block/cell_workbench_top"))
                        .put(TextureSlot.BOTTOM, MACHINE_BOTTOM)
                        .put(TextureSlot.PARTICLE, makeId("block/cell_workbench_top"))
                ));

        energyCell(AEBlocks.ENERGY_CELL, "block/energy_cell");
        energyCell(AEBlocks.DENSE_ENERGY_CELL, "block/dense_energy_cell");
        simpleBlockAndItem(AEBlocks.CREATIVE_ENERGY_CELL, "block/creative_energy_cell");

        // Both use the same mysterious cube model
        blockModels.blockStateOutput.accept(createSimpleBlock(AEBlocks.MYSTERIOUS_CUBE.block(), makeId("block/mysterious_cube")));
        blockModels.blockStateOutput.accept(createSimpleBlock(AEBlocks.NOT_SO_MYSTERIOUS_CUBE.block(), makeId("block/mysterious_cube")));
    }

    private static final TextureSlot BLOCK = TextureSlot.create("block");
    private static final TextureSlot LIGHTS = TextureSlot.create("lights");
    private static final ModelTemplate CONTROLLER_BLOCK_LIGHTS = ModelTemplates.create("ae2:controller/controller_block_lights", BLOCK, LIGHTS);
    private static final ModelTemplate CONTROLLER_COLUMN_LIGHTS = ModelTemplates.create("ae2:controller/controller_column_lights", BLOCK, LIGHTS);

    private void controller() {
        var block = AEBlocks.CONTROLLER.block();

        var texturesBlock = new TextureMapping()
                .put(TextureSlot.ALL, AppEng.makeId("block/controller"))
                .put(TextureSlot.SIDE, AppEng.makeId("block/controller_column"))
                .put(TextureSlot.END, AppEng.makeId("block/controller"))
                .put(BLOCK, AppEng.makeId("block/controller_powered"))
                .put(LIGHTS, AppEng.makeId("block/controller_lights"));
        var offlineBlock = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_block_offline", texturesBlock, modelOutput);
        var onlineBlock = CONTROLLER_BLOCK_LIGHTS.createWithSuffix(block, "_block_online", texturesBlock, modelOutput);
        var conflictedBlock = CONTROLLER_BLOCK_LIGHTS.createWithSuffix(block, "_block_conflicted", texturesBlock.copy().put(LIGHTS, AppEng.makeId("block/controller_conflict")), modelOutput);

        var texturesColumn = new TextureMapping()
                .put(TextureSlot.ALL, AppEng.makeId("block/controller"))
                .put(TextureSlot.SIDE, AppEng.makeId("block/controller_column"))
                .put(TextureSlot.END, AppEng.makeId("block/controller"))
                .put(BLOCK, AppEng.makeId("block/controller_column_powered"))
                .put(LIGHTS, AppEng.makeId("block/controller_column_lights"));
        var offlineColumn = ModelTemplates.CUBE_COLUMN.createWithSuffix(block, "_column_offline", texturesColumn, modelOutput);
        var onlineColumn = CONTROLLER_COLUMN_LIGHTS.createWithSuffix(block, "_column_online", texturesColumn, modelOutput);
        var conflictedColumn = CONTROLLER_COLUMN_LIGHTS.createWithSuffix(block, "_column_conflicted", texturesColumn.copy().put(LIGHTS, AppEng.makeId("block/controller_column_conflict")), modelOutput);

        var insideA = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_inside_a", TextureMapping.cube(AppEng.makeId("block/controller_inside_a")), modelOutput);
        var insideB = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_inside_b", TextureMapping.cube(AppEng.makeId("block/controller_inside_b")), modelOutput);
        var insideAConflicted = CONTROLLER_BLOCK_LIGHTS.createWithSuffix(block, "inside_a_conflicted", new TextureMapping()
                .put(BLOCK, AppEng.makeId("block/controller_inside_a_powered"))
                .put(LIGHTS, AppEng.makeId("block/controller_conflict")), modelOutput);
        var insideBConflicted = CONTROLLER_BLOCK_LIGHTS.createWithSuffix(block, "inside_b_conflicted", new TextureMapping()
                .put(BLOCK, AppEng.makeId("block/controller_inside_b_powered"))
                .put(LIGHTS, AppEng.makeId("block/controller_conflict")), modelOutput);

        // Alias the enums since the following becomes very noisy otherwise
        // Static import would be possible but conflicts with local variables
        var state = ControllerBlock.CONTROLLER_STATE;
        var s_type = ControllerBlock.CONTROLLER_TYPE;
        var s_offline = ControllerBlock.ControllerBlockState.offline;
        var s_online = ControllerBlock.ControllerBlockState.online;
        var s_conflicted = ControllerBlock.ControllerBlockState.conflicted;
        var t_block = ControllerBlock.ControllerRenderType.block;
        var t_column_x = ControllerBlock.ControllerRenderType.column_x;
        var t_column_y = ControllerBlock.ControllerRenderType.column_y;
        var t_column_z = ControllerBlock.ControllerRenderType.column_z;
        var t_inside_a = ControllerBlock.ControllerRenderType.inside_a;
        var t_inside_b = ControllerBlock.ControllerRenderType.inside_b;

        blockModels.blockStateOutput.accept(
                MultiPartGenerator.multiPart(block)
                        .with(Condition.condition().term(state, s_offline).term(s_type, t_block),
                                Variant.variant().with(VariantProperties.MODEL, offlineBlock))
                        .with(Condition.condition().term(state, s_online).term(s_type, t_block),
                                Variant.variant().with(VariantProperties.MODEL, onlineBlock))
                        .with(Condition.condition().term(state, s_conflicted).term(s_type, t_block),
                                Variant.variant().with(VariantProperties.MODEL, conflictedBlock))
                        .with(Condition.condition().term(state, s_offline).term(s_type, t_column_x),
                                Variant.variant().with(VariantProperties.MODEL, offlineColumn)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        .with(Condition.condition().term(state, s_offline).term(s_type, t_column_y),
                                Variant.variant().with(VariantProperties.MODEL, offlineColumn))
                        .with(Condition.condition().term(state, s_offline).term(s_type, t_column_z),
                                Variant.variant().with(VariantProperties.MODEL, offlineColumn)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .with(Condition.condition().term(state, s_online).term(s_type, t_column_x),
                                Variant.variant().with(VariantProperties.MODEL, onlineColumn)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        .with(Condition.condition().term(state, s_online).term(s_type, t_column_y),
                                Variant.variant().with(VariantProperties.MODEL, onlineColumn))
                        .with(Condition.condition().term(state, s_online).term(s_type, t_column_z),
                                Variant.variant().with(VariantProperties.MODEL, onlineColumn)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .with(Condition.condition().term(state, s_conflicted).term(s_type, t_column_x),
                                Variant.variant().with(VariantProperties.MODEL, conflictedColumn)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                        .with(Condition.condition().term(state, s_conflicted).term(s_type, t_column_y),
                                Variant.variant().with(VariantProperties.MODEL, conflictedColumn))
                        .with(Condition.condition().term(state, s_conflicted).term(s_type, t_column_z),
                                Variant.variant().with(VariantProperties.MODEL, conflictedColumn)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .with(Condition.condition().term(state, s_offline, s_online).term(s_type, t_inside_a),
                                Variant.variant().with(VariantProperties.MODEL, insideA))
                        .with(Condition.condition().term(state, s_offline, s_online).term(s_type, t_inside_b),
                                Variant.variant().with(VariantProperties.MODEL, insideB))
                        .with(Condition.condition().term(state, s_conflicted).term(s_type, t_inside_a),
                                Variant.variant().with(VariantProperties.MODEL, insideAConflicted))
                        .with(Condition.condition().term(state, s_conflicted).term(s_type, t_inside_b),
                                Variant.variant().with(VariantProperties.MODEL, insideBConflicted))
        );

        blockModels.registerSimpleItemModel(block.asItem(), offlineBlock);
    }

    private void quantumBridge() {
        var formedModel = builtInModel("qnb/qnb_formed");

        var unformedRingModel = AppEng.makeId("block/qnb/ring");
        var ringDispatch = PropertyDispatch.property(QuantumRingBlock.FORMED);
        ringDispatch.select(false, Variant.variant().with(VariantProperties.MODEL, unformedRingModel));
        ringDispatch.select(true, Variant.variant().with(VariantProperties.MODEL, formedModel));
        multiVariantGenerator(AEBlocks.QUANTUM_RING).with(ringDispatch);

        var unformedLinkModel = AppEng.makeId("block/qnb/link");
        var linkDispatch = PropertyDispatch.property(QuantumLinkChamberBlock.FORMED);
        linkDispatch.select(false, Variant.variant().with(VariantProperties.MODEL, unformedLinkModel));
        linkDispatch.select(true, Variant.variant().with(VariantProperties.MODEL, formedModel));
        multiVariantGenerator(AEBlocks.QUANTUM_LINK).with(linkDispatch);
    }

    private void spatialPylon() {
        // Spatial pylon uses a normal model for the item, special model for block
        itemModels.itemModelOutput.accept(
                AEBlocks.SPATIAL_PYLON.asItem(),
                ItemModelUtils.plainModel(
                        ModelTemplates.CUBE_ALL.create(AEBlocks.SPATIAL_PYLON.asItem(), TextureMapping.cube(AEBlocks.SPATIAL_PYLON.block()), modelOutput)
                )
        );
        builtInModel(AEBlocks.SPATIAL_PYLON, true);
    }

    private void meChest() {
        var multipart = multiPartGenerator(AEBlocks.ME_CHEST);
        withOrientations(
                multipart,
                Variant.variant().with(VariantProperties.MODEL, makeId("block/chest/base")));
        withOrientations(
                multipart,
                () -> Condition.condition().term(MEChestBlock.LIGHTS_ON, false),
                Variant.variant().with(VariantProperties.MODEL, makeId("block/chest/lights_off")));
        withOrientations(
                multipart,
                () -> Condition.condition().term(MEChestBlock.LIGHTS_ON, true),
                Variant.variant().with(VariantProperties.MODEL, makeId("block/chest/lights_on")));
    }

    private void quartzGrowthAccelerator() {
        var block = AEBlocks.GROWTH_ACCELERATOR.block();
        var unpoweredModel = TexturedModel.CUBE_TOP_BOTTOM.create(block, modelOutput);
        var poweredModel = TexturedModel.CUBE_TOP_BOTTOM
                .updateTexture(textures -> textures
                        .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side_on"))
                        .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top_on"))
                )
                .createWithSuffix(block, "_on", modelOutput);

        multiVariantGenerator(AEBlocks.GROWTH_ACCELERATOR)
                .with(createFacingDispatch(90, 0))
                .with(PropertyDispatch.property(GrowthAcceleratorBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, unpoweredModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, poweredModel)));

        itemModels.itemModelOutput.accept(AEBlocks.GROWTH_ACCELERATOR.asItem(), ItemModelUtils.plainModel(unpoweredModel));
    }

    private void craftingMonitor() {
        var formedModel = createBuiltInModel(makeId("block/crafting/monitor_formed"));
        var unformedModel = ModelTemplates.CUBE.create(
                makeId("block/crafting/monitor"),
                new TextureMapping()
                        .put(TextureSlot.PARTICLE, makeId("block/crafting/monitor"))
                        .put(TextureSlot.DOWN, MACHINE_BOTTOM)
                        .put(TextureSlot.UP, makeId("block/crafting/unit"))
                        .put(TextureSlot.NORTH, makeId("block/crafting/monitor"))
                        .put(TextureSlot.EAST, makeId("block/crafting/unit"))
                        .put(TextureSlot.SOUTH, makeId("block/crafting/unit"))
                        .put(TextureSlot.WEST, makeId("block/crafting/unit")),
                modelOutput
        );

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
        var builder = MultiVariantGenerator.multiVariant(AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block());

        var modelFile = makeId("block/crystal_resonance_generator");

        var facingVariants = PropertyDispatch.property(BlockStateProperties.FACING);
        builder.with(facingVariants);
        for (var facing : Direction.values()) {
            var variant = Variant.variant().with(VariantProperties.MODEL, modelFile);
            var orientation = BlockOrientation.get(facing, 0);
            // The original is facing "up" while we generally assume models are facing north
            // but this looks better as an item model
            facingVariants.select(facing, applyRotation(variant,
                    orientation.getAngleX() + 90,
                    orientation.getAngleY(),
                    0));
        }

        blockModels.blockStateOutput.accept(builder);

        blockModels.registerSimpleItemModel(AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block(), modelFile);
    }

    private void wirelessAccessPoint() {
        var builder = MultiPartGenerator.multiPart(AEBlocks.WIRELESS_ACCESS_POINT.block());

        var chassis = makeId("block/wireless_access_point_chassis");
        var antennaOff = makeId("block/wireless_access_point_off");
        var antennaOn = makeId("block/wireless_access_point_on");
        var statusOff = makeId("block/wireless_access_point_status_off");
        var statusOn = makeId("block/wireless_access_point_status_has_channel");

        for (var facing : Direction.values()) {
            var rotation = BlockOrientation.get(facing, 0);

            builder.with(
                    Condition.condition().term(BlockStateProperties.FACING, facing),
                    applyOrientation(Variant.variant().with(VariantProperties.MODEL, chassis), rotation)
            );

            BiConsumer<ResourceLocation, WirelessAccessPointBlock.State> addModel = (modelFile, state) -> builder.with(
                    Condition.condition().term(BlockStateProperties.FACING, facing)
                            .term(WirelessAccessPointBlock.STATE, state),
                    applyOrientation(Variant.variant().with(VariantProperties.MODEL, modelFile), rotation)
            );
            addModel.accept(antennaOff, WirelessAccessPointBlock.State.OFF);
            addModel.accept(statusOff, WirelessAccessPointBlock.State.OFF);
            addModel.accept(antennaOff, WirelessAccessPointBlock.State.ON);
            addModel.accept(statusOn, WirelessAccessPointBlock.State.ON);
            addModel.accept(antennaOn, WirelessAccessPointBlock.State.HAS_CHANNEL);
            addModel.accept(statusOn, WirelessAccessPointBlock.State.HAS_CHANNEL);
        }

        blockModels.blockStateOutput.accept(builder);
    }

    private static final ResourceLocation MACHINE_BOTTOM = AppEng.makeId("block/generics/bottom");

    private void vibrationChamber() {
        var block = AEBlocks.VIBRATION_CHAMBER.block();

        var textureMapping = TextureMapping.cube(block)
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.DOWN, MACHINE_BOTTOM)
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_back"))
                .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
        var offModel = ModelTemplates.CUBE.create(block, textureMapping, modelOutput);

        var textureMappingOn = textureMapping.copy()
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top_on"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front_on"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_back_on"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_front_on"));
        var onModel = ModelTemplates.CUBE.createWithSuffix(block, "_on", textureMappingOn, modelOutput);

        multiVariantGenerator(AEBlocks.VIBRATION_CHAMBER)
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(VibrationChamberBlock.ACTIVE)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel)));

        // TODO  itemModels().withExistingParent(modelPath(AEBlocks.VIBRATION_CHAMBER), offModel);
    }

    private void spatialAnchor() {
        var offModel = AppEng.makeId("block/spatial_anchor");
        var onModel = AppEng.makeId("block/spatial_anchor_on");

        multiVariantGenerator(AEBlocks.SPATIAL_ANCHOR)
                .with(createFacingDispatch(90, 0))
                .with(PropertyDispatch.property(SpatialAnchorBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel)));

        // TODO  itemModels().withExistingParent(modelPath(AEBlocks.SPATIAL_ANCHOR), offModel);
    }

    private void patternProvider() {
        var def = AEBlocks.PATTERN_PROVIDER;
        var normalModel = TexturedModel.CUBE.create(def.block(), modelOutput);
        // TODO simpleBlockItem(def.block(), normalModel);
        // the block state and the oriented model are in manually written json files

        var orientedModel = makeId("block/pattern_provider_oriented");
        multiVariantGenerator(AEBlocks.PATTERN_PROVIDER, Variant.variant())
                .with(PropertyDispatch.property(PatternProviderBlock.PUSH_DIRECTION).generate(pushDirection -> {
                    var forward = pushDirection.getDirection();
                    if (forward == null) {
                        return Variant.variant().with(VariantProperties.MODEL, normalModel);
                    } else {
                        var orientation = BlockOrientation.get(forward);
                        return applyRotation(
                                Variant.variant().with(VariantProperties.MODEL, orientedModel),
                                // + 90 because the default model is oriented UP, while block orientation assumes NORTH
                                orientation.getAngleX() + 90,
                                orientation.getAngleY(),
                                0);
                    }
                }));
    }

    private void ioPort() {
        var offModel = makeId("block/io_port");
        var onModel = makeId("block/io_port_on");

        multiVariantGenerator(AEBlocks.IO_PORT)
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(IOPortBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel)));
        // TODO itemModels().withExistingParent(modelPath(AEBlocks.IO_PORT), offModel);
    }

    private void spatialIoPort() {
        var offModel = makeId("block/spatial_io_port");
        var onModel = makeId("block/spatial_io_port_on");

        multiVariantGenerator(AEBlocks.SPATIAL_IO_PORT)
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(SpatialIOPortBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, offModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, onModel)));
        // TODO itemModels().withExistingParent(modelPath(AEBlocks.SPATIAL_IO_PORT), offModel);
    }

    private String modelPath(BlockDefinition<?> block) {
        return block.id().getPath();
    }

    private ResourceLocation builtInModel(String blockId) {
        ResourceLocation modelId = makeId(blockId).withPrefix("block/");
        return createBuiltInModel(modelId);
    }

    private void builtInModel(BlockDefinition<?> block) {
        builtInModel(block, false);
    }

    private void builtInModel(BlockDefinition<?> block, boolean skipItem) {
        blockModels.blockStateOutput.accept(
                createSimpleBlock(block.block(), createBuiltInModel(block.block()))
        );

        if (!skipItem) {
            // The item model should not reference the block model since that will be replaced in-code
            blockModels.itemModelOutput.accept(block.item().asItem(), new EmptyModel.Unbaked());
        }
    }

    private ResourceLocation createBuiltInModel(Block block) {
        return createBuiltInModel(block, "");
    }

    private ResourceLocation createBuiltInModel(Block block, String suffix) {
        return createBuiltInModel(ModelLocationUtils.getModelLocation(block, suffix));
    }

    private void energyCell(
            BlockDefinition<?> blockDef,
            String baseTexture) {

        var block = blockDef.block();

        var energyLevelDispatch = PropertyDispatch.property(EnergyCellBlock.ENERGY_STORAGE);

        for (var i = 0; i < 5; i++) {
            var textures = TextureMapping.cube(TextureMapping.getBlockTexture(block, "_" + i));
            var model = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_" + i, textures, modelOutput);
            energyLevelDispatch.select(i, Variant.variant().with(VariantProperties.MODEL, model));
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(energyLevelDispatch));


        //  TODO 1.21.4 var item = itemModels().withExistingParent(modelPath(block), models.get(0));
        //  TODO 1.21.4 for (var i = 1; i < models.size(); i++) {
        //  TODO 1.21.4     // The predicate matches "greater than", meaning for fill-level > 0 the first non-empty texture is used
        //  TODO 1.21.4     float fillFactor = i / (float) models.size();
        //  TODO 1.21.4     item.override()
        //  TODO 1.21.4             .predicate(InitItemModelsProperties.ENERGY_FILL_LEVEL_ID, fillFactor)
        //  TODO 1.21.4             .model(models.get(i));
        //  TODO 1.21.4 }
    }

    private void craftingModel(BlockDefinition<?> block, String name) {
        var unformedModel = ModelTemplates.CUBE_ALL.create(
                makeId("block/crafting/" + name), TextureMapping.cube(makeId("block/crafting/" + name)), modelOutput
        );
        var formedModel = builtInModel("crafting/" + name + "_formed");

        blockModels.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block.block())
                                .with(
                                        PropertyDispatch.property(AbstractCraftingUnitBlock.FORMED)
                                                .select(false, Variant.variant().with(VariantProperties.MODEL, unformedModel))
                                                .select(true, Variant.variant().with(VariantProperties.MODEL, formedModel))
                                )
                );

        // TODO simpleBlockItem(block.block(), blockModel);
    }

    private void generateQuartzCluster(BlockDefinition<?> quartz) {
        Block block = quartz.block();
        blockModels.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(
                                        block,
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, ModelTemplates.CROSS.create(block, TextureMapping.cross(block), modelOutput))
                                )
                                .with(blockModels.createColumnWithFacing())
                );
    }
}
