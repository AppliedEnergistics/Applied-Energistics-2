package appeng.datagen.providers.models;

import static appeng.core.AppEng.makeId;
import static net.minecraft.client.data.models.BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING;
import static net.minecraft.client.data.models.BlockModelGenerators.createSimpleBlock;
import static net.minecraft.client.data.models.BlockModelGenerators.plainModel;
import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;
import static net.minecraft.client.data.models.BlockModelGenerators.variant;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import com.mojang.math.Quadrant;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;

import appeng.api.orientation.BlockOrientation;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.CraftingUnitType;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.block.networking.ControllerBlock;
import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.WirelessAccessPointBlock;
import appeng.block.qnb.QnbFormedModel;
import appeng.block.qnb.QuantumLinkChamberBlock;
import appeng.block.qnb.QuantumRingBlock;
import appeng.block.spatial.SpatialAnchorBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.storage.IOPortBlock;
import appeng.block.storage.MEChestBlock;
import appeng.client.item.EnergyFillLevelProperty;
import appeng.client.model.PaintSplotchesModel;
import appeng.client.model.SpatialPylonModel;
import appeng.client.model.SpinnableVariant;
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.QuartzGlassModel;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;

public class BlockModelProvider extends ModelSubProvider {

    public BlockModelProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
            PartModelOutput partModels) {
        super(blockModels, itemModels, partModels);
    }

    @Override
    protected void register() {
        blockModels.createTrivialBlock(
                AEBlocks.MATRIX_FRAME.block(),
                TexturedModel.createDefault(block -> TRANSPARENT_PARTICLE, EMPTY_MODEL));

        // These models will be overwritten in code
        builtInModel(AEBlocks.QUARTZ_GLASS, new QuartzGlassModel.Unbaked(), true);
        blockModels.registerSimpleItemModel(
                AEBlocks.QUARTZ_GLASS.asItem(),
                ModelTemplates.CUBE_ALL.create(
                        AEBlocks.QUARTZ_GLASS.asItem(),
                        TextureMapping.cube(AppEng.makeId("block/glass/quartz_glass_item")),
                        modelOutput));
        blockModels.copyModel(AEBlocks.QUARTZ_GLASS.block(), AEBlocks.QUARTZ_VIBRANT_GLASS.block());

        multiVariantGenerator(AEBlocks.CABLE_BUS,
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new CableBusModel.Unbaked())));

        builtInModel(AEBlocks.PAINT, new PaintSplotchesModel.Unbaked());

        var driveUnbaked = new DriveModel.Unbaked(new SpinnableVariant(makeId("drive_base")));
        var driveModel = customBlockStateModel(driveUnbaked);
        multiVariantGenerator(AEBlocks.DRIVE, driveModel, createFacingSpinDispatch());

        var charger = makeId("block/charger");
        multiVariantGenerator(AEBlocks.CHARGER, plainVariant(charger), createFacingSpinDispatch());

        var inscriber = makeId("block/inscriber");
        multiVariantGenerator(AEBlocks.INSCRIBER, plainVariant(inscriber), createFacingSpinDispatch());

        multiVariantGenerator(AEBlocks.SKY_STONE_TANK,
                plainVariant(makeId("block/sky_stone_tank")));
        multiVariantGenerator(AEBlocks.TINY_TNT,
                plainVariant(makeId("block/tiny_tnt")));
        multiVariantGenerator(AEBlocks.MOLECULAR_ASSEMBLER,
                plainVariant(makeId("block/molecular_assembler")));

        // Generate an empty block model for the crank, since the base model and shaft will be used by the dynamic
        // renderer
        multiVariantGenerator(AEBlocks.CRANK, plainVariant(makeId("block/crank")));

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

        blockModels.createParticleOnlyBlock(AEBlocks.SKY_STONE_CHEST.block(), AEBlocks.SKY_STONE_BLOCK.block());
        itemModels.declareCustomModelItem(AEBlocks.SKY_STONE_CHEST.asItem());
        blockModels.createParticleOnlyBlock(AEBlocks.SMOOTH_SKY_STONE_CHEST.block(),
                AEBlocks.SMOOTH_SKY_STONE_BLOCK.block());
        itemModels.declareCustomModelItem(AEBlocks.SMOOTH_SKY_STONE_CHEST.asItem());

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

        craftingModel(AEBlocks.CRAFTING_ACCELERATOR, "accelerator", CraftingUnitType.ACCELERATOR);
        craftingModel(AEBlocks.CRAFTING_UNIT, "unit", CraftingUnitType.UNIT);
        craftingModel(AEBlocks.CRAFTING_STORAGE_1K, "1k_storage", CraftingUnitType.STORAGE_1K);
        craftingModel(AEBlocks.CRAFTING_STORAGE_4K, "4k_storage", CraftingUnitType.STORAGE_4K);
        craftingModel(AEBlocks.CRAFTING_STORAGE_16K, "16k_storage", CraftingUnitType.STORAGE_16K);
        craftingModel(AEBlocks.CRAFTING_STORAGE_64K, "64k_storage", CraftingUnitType.STORAGE_64K);
        craftingModel(AEBlocks.CRAFTING_STORAGE_256K, "256k_storage", CraftingUnitType.STORAGE_256K);

        simpleBlockAndItem(AEBlocks.CELL_WORKBENCH, TexturedModel.CUBE_TOP_BOTTOM
                .updateTexture(textures -> textures
                        .put(TextureSlot.TOP, makeId("block/cell_workbench_top"))
                        .put(TextureSlot.BOTTOM, MACHINE_BOTTOM)
                        .put(TextureSlot.PARTICLE, makeId("block/cell_workbench_top"))));

        energyCell(AEBlocks.ENERGY_CELL, "block/energy_cell");
        energyCell(AEBlocks.DENSE_ENERGY_CELL, "block/dense_energy_cell");
        simpleBlockAndItem(AEBlocks.CREATIVE_ENERGY_CELL, "block/creative_energy_cell");

        // Both use the same mysterious cube model
        blockModels.blockStateOutput.accept(
                createSimpleBlock(AEBlocks.MYSTERIOUS_CUBE.block(), plainVariant(makeId("block/mysterious_cube"))));
        blockModels.blockStateOutput.accept(createSimpleBlock(AEBlocks.NOT_SO_MYSTERIOUS_CUBE.block(),
                plainVariant(makeId("block/mysterious_cube"))));
        blockModels.registerSimpleItemModel(AEBlocks.NOT_SO_MYSTERIOUS_CUBE.asItem(), makeId("block/mysterious_cube"));
    }

    private static final TextureSlot BLOCK = TextureSlot.create("block");
    private static final TextureSlot LIGHTS = TextureSlot.create("lights");
    private static final ModelTemplate CONTROLLER_BLOCK_LIGHTS = ModelTemplates.create("ae2:controller_block_lights",
            BLOCK, LIGHTS);
    private static final ModelTemplate CONTROLLER_COLUMN_LIGHTS = ModelTemplates.create("ae2:controller_column_lights",
            BLOCK, LIGHTS);

    private void controller() {
        var block = AEBlocks.CONTROLLER.block();

        var texturesBlock = new TextureMapping()
                .put(TextureSlot.ALL, AppEng.makeId("block/controller"))
                .put(TextureSlot.SIDE, AppEng.makeId("block/controller_column"))
                .put(TextureSlot.END, AppEng.makeId("block/controller"))
                .put(BLOCK, AppEng.makeId("block/controller_powered"))
                .put(LIGHTS, AppEng.makeId("block/controller_lights"));
        var offlineBlock = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_block_offline", texturesBlock,
                modelOutput);
        var onlineBlock = CONTROLLER_BLOCK_LIGHTS.createWithSuffix(block, "_block_online", texturesBlock, modelOutput);
        var conflictedBlock = CONTROLLER_BLOCK_LIGHTS.createWithSuffix(block, "_block_conflicted",
                texturesBlock.copy().put(LIGHTS, AppEng.makeId("block/controller_conflict")), modelOutput);

        var texturesColumn = new TextureMapping()
                .put(TextureSlot.ALL, AppEng.makeId("block/controller"))
                .put(TextureSlot.SIDE, AppEng.makeId("block/controller_column"))
                .put(TextureSlot.END, AppEng.makeId("block/controller"))
                .put(BLOCK, AppEng.makeId("block/controller_column_powered"))
                .put(LIGHTS, AppEng.makeId("block/controller_column_lights"));
        var offlineColumn = ModelTemplates.CUBE_COLUMN.createWithSuffix(block, "_column_offline", texturesColumn,
                modelOutput);
        var onlineColumn = CONTROLLER_COLUMN_LIGHTS.createWithSuffix(block, "_column_online", texturesColumn,
                modelOutput);
        var conflictedColumn = CONTROLLER_COLUMN_LIGHTS.createWithSuffix(block, "_column_conflicted",
                texturesColumn.copy().put(LIGHTS, AppEng.makeId("block/controller_column_conflict")), modelOutput);

        var insideA = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_inside_a",
                TextureMapping.cube(AppEng.makeId("block/controller_inside_a")), modelOutput);
        var insideB = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_inside_b",
                TextureMapping.cube(AppEng.makeId("block/controller_inside_b")), modelOutput);
        var insideAConflicted = CONTROLLER_BLOCK_LIGHTS.createWithSuffix(block, "inside_a_conflicted",
                new TextureMapping()
                        .put(BLOCK, AppEng.makeId("block/controller_inside_a_powered"))
                        .put(LIGHTS, AppEng.makeId("block/controller_conflict")),
                modelOutput);
        var insideBConflicted = CONTROLLER_BLOCK_LIGHTS.createWithSuffix(block, "inside_b_conflicted",
                new TextureMapping()
                        .put(BLOCK, AppEng.makeId("block/controller_inside_b_powered"))
                        .put(LIGHTS, AppEng.makeId("block/controller_conflict")),
                modelOutput);

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
                        .with(new ConditionBuilder().term(state, s_offline).term(s_type, t_block),
                                plainVariant(offlineBlock))
                        .with(new ConditionBuilder().term(state, s_online).term(s_type, t_block),
                                plainVariant(onlineBlock))
                        .with(new ConditionBuilder().term(state, s_conflicted).term(s_type, t_block),
                                plainVariant(conflictedBlock))
                        .with(new ConditionBuilder().term(state, s_offline).term(s_type, t_column_x),
                                plainVariant(offlineColumn)
                                        .with(VariantMutator.X_ROT.withValue(Quadrant.R90))
                                        .with(VariantMutator.Y_ROT.withValue(Quadrant.R90)))
                        .with(new ConditionBuilder().term(state, s_offline).term(s_type, t_column_y),
                                plainVariant(offlineColumn))
                        .with(new ConditionBuilder().term(state, s_offline).term(s_type, t_column_z),
                                plainVariant(offlineColumn)
                                        .with(VariantMutator.X_ROT.withValue(Quadrant.R90)))
                        .with(new ConditionBuilder().term(state, s_online).term(s_type, t_column_x),
                                plainVariant(onlineColumn)
                                        .with(VariantMutator.X_ROT.withValue(Quadrant.R90))
                                        .with(VariantMutator.Y_ROT.withValue(Quadrant.R90)))
                        .with(new ConditionBuilder().term(state, s_online).term(s_type, t_column_y),
                                plainVariant(onlineColumn))
                        .with(new ConditionBuilder().term(state, s_online).term(s_type, t_column_z),
                                plainVariant(onlineColumn)
                                        .with(VariantMutator.X_ROT.withValue(Quadrant.R90)))
                        .with(new ConditionBuilder().term(state, s_conflicted).term(s_type, t_column_x),
                                plainVariant(conflictedColumn)
                                        .with(VariantMutator.X_ROT.withValue(Quadrant.R90))
                                        .with(VariantMutator.Y_ROT.withValue(Quadrant.R90)))
                        .with(new ConditionBuilder().term(state, s_conflicted).term(s_type, t_column_y),
                                plainVariant(conflictedColumn))
                        .with(new ConditionBuilder().term(state, s_conflicted).term(s_type, t_column_z),
                                plainVariant(conflictedColumn)
                                        .with(VariantMutator.X_ROT.withValue(Quadrant.R90)))
                        .with(new ConditionBuilder().term(state, s_offline, s_online).term(s_type, t_inside_a),
                                plainVariant(insideA))
                        .with(new ConditionBuilder().term(state, s_offline, s_online).term(s_type, t_inside_b),
                                plainVariant(insideB))
                        .with(new ConditionBuilder().term(state, s_conflicted).term(s_type, t_inside_a),
                                plainVariant(insideAConflicted))
                        .with(new ConditionBuilder().term(state, s_conflicted).term(s_type, t_inside_b),
                                plainVariant(insideBConflicted)));

        blockModels.registerSimpleItemModel(block.asItem(), offlineBlock);
    }

    private void quantumBridge() {
        var formedVariant = customBlockStateModel(new QnbFormedModel.Unbaked());
        var unformedRingModel = ModelLocationUtils.getModelLocation(AEBlocks.QUANTUM_RING.block());
        var ringDispatch = PropertyDispatch.initial(QuantumRingBlock.FORMED);
        ringDispatch.select(false, plainVariant(unformedRingModel));
        ringDispatch.select(true, formedVariant);
        multiVariantGenerator(AEBlocks.QUANTUM_RING, ringDispatch);
        blockModels.registerSimpleItemModel(AEBlocks.QUANTUM_RING.asItem(), unformedRingModel);

        var unformedLinkModel = ModelLocationUtils.getModelLocation(AEBlocks.QUANTUM_LINK.block());
        var linkDispatch = PropertyDispatch.initial(QuantumLinkChamberBlock.FORMED);
        linkDispatch.select(false, plainVariant(unformedLinkModel));
        linkDispatch.select(true, formedVariant);
        multiVariantGenerator(AEBlocks.QUANTUM_LINK, linkDispatch);
        blockModels.registerSimpleItemModel(AEBlocks.QUANTUM_LINK.asItem(), unformedLinkModel);
    }

    private void spatialPylon() {
        // Spatial pylon uses a normal model for the item, special model for block
        itemModels.itemModelOutput.accept(
                AEBlocks.SPATIAL_PYLON.asItem(),
                ItemModelUtils.plainModel(
                        ModelTemplates.CUBE_ALL.create(AEBlocks.SPATIAL_PYLON.asItem(),
                                TextureMapping.cube(AppEng.makeId("item/spatial_pylon")), modelOutput)));
        builtInModel(AEBlocks.SPATIAL_PYLON, new SpatialPylonModel.Unbaked(), true);
    }

    private void meChest() {
        var multipart = multiPartGenerator(AEBlocks.ME_CHEST);
        var baseModel = ModelLocationUtils.getModelLocation(AEBlocks.ME_CHEST.block(), "_base");
        var lightsOffModel = ModelLocationUtils.getModelLocation(AEBlocks.ME_CHEST.block(), "_lights_off");
        var lightsOnModel = ModelLocationUtils.getModelLocation(AEBlocks.ME_CHEST.block(), "_lights_on");
        withOrientations(
                multipart,
                plainModel(baseModel));
        withOrientations(
                multipart,
                () -> new ConditionBuilder().term(MEChestBlock.LIGHTS_ON, false),
                plainModel(lightsOffModel));
        withOrientations(
                multipart,
                () -> new ConditionBuilder().term(MEChestBlock.LIGHTS_ON, true),
                plainModel(lightsOnModel));
        itemModels.declareCustomModelItem(AEBlocks.ME_CHEST.asItem());
    }

    private void quartzGrowthAccelerator() {
        var block = AEBlocks.GROWTH_ACCELERATOR.block();
        var unpoweredModel = TexturedModel.CUBE_TOP_BOTTOM.create(block, modelOutput);
        var poweredModel = TexturedModel.CUBE_TOP_BOTTOM
                .updateTexture(textures -> textures
                        .put(TextureSlot.SIDE, getBlockTexture(block, "_side_on"))
                        .put(TextureSlot.TOP, getBlockTexture(block, "_top_on")))
                .createWithSuffix(block, "_on", modelOutput);

        multiVariantGenerator(
                AEBlocks.GROWTH_ACCELERATOR,
                PropertyDispatch.initial(GrowthAcceleratorBlock.POWERED)
                        .select(false, plainVariant(unpoweredModel))
                        .select(true, plainVariant(poweredModel)),
                createFacingDispatch(90, 0));

        itemModels.itemModelOutput.accept(AEBlocks.GROWTH_ACCELERATOR.asItem(),
                ItemModelUtils.plainModel(unpoweredModel));
    }

    private void craftingMonitor() {
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
                modelOutput);

        multiVariantGenerator(AEBlocks.CRAFTING_MONITOR,
                PropertyDispatch.initial(AbstractCraftingUnitBlock.FORMED, BlockStateProperties.FACING)
                        .generate((formed, facing) -> {
                            if (formed) {
                                return customBlockStateModel(new CraftingCubeModel.Unbaked(CraftingUnitType.MONITOR));
                            } else {
                                return variant(applyOrientation(
                                        plainModel(unformedModel),
                                        BlockOrientation.get(facing)));
                            }
                        }));
        blockModels.registerSimpleItemModel(AEBlocks.CRAFTING_MONITOR.asItem(), unformedModel);
    }

    private void crystalResonanceGenerator() {
        var modelFile = makeId("block/crystal_resonance_generator");
        var builder = MultiVariantGenerator.dispatch(AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block(),
                plainVariant(modelFile));

        var facingVariants = PropertyDispatch.modify(BlockStateProperties.FACING);
        for (var facing : Direction.values()) {
            var orientation = BlockOrientation.get(facing, 0);
            // The original is facing "up" while we generally assume models are facing north
            // but this looks better as an item model
            facingVariants.select(facing, applyRotation(
                    orientation.getAngleX() + 90,
                    orientation.getAngleY(),
                    0));
        }
        builder.with(facingVariants);

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
                    new ConditionBuilder().term(BlockStateProperties.FACING, facing),
                    variant(applyOrientation(plainModel(chassis), rotation)));

            BiConsumer<ResourceLocation, WirelessAccessPointBlock.State> addModel = (modelFile, state) -> builder.with(
                    new ConditionBuilder().term(BlockStateProperties.FACING, facing)
                            .term(WirelessAccessPointBlock.STATE, state),
                    variant(applyOrientation(plainModel(modelFile), rotation)));
            addModel.accept(antennaOff, WirelessAccessPointBlock.State.OFF);
            addModel.accept(statusOff, WirelessAccessPointBlock.State.OFF);
            addModel.accept(antennaOff, WirelessAccessPointBlock.State.ON);
            addModel.accept(statusOn, WirelessAccessPointBlock.State.ON);
            addModel.accept(antennaOn, WirelessAccessPointBlock.State.HAS_CHANNEL);
            addModel.accept(statusOn, WirelessAccessPointBlock.State.HAS_CHANNEL);
        }

        blockModels.blockStateOutput.accept(builder);

        itemModels.declareCustomModelItem(AEBlocks.WIRELESS_ACCESS_POINT.asItem());
    }

    private static final ResourceLocation MACHINE_BOTTOM = AppEng.makeId("block/generics/bottom");

    private void vibrationChamber() {
        var block = AEBlocks.VIBRATION_CHAMBER.block();

        var textureMapping = TextureMapping.cube(block)
                .put(TextureSlot.PARTICLE, getBlockTexture(block, "_front"))
                .put(TextureSlot.DOWN, MACHINE_BOTTOM)
                .put(TextureSlot.UP, getBlockTexture(block, "_top"))
                .put(TextureSlot.NORTH, getBlockTexture(block, "_front"))
                .put(TextureSlot.EAST, getBlockTexture(block, "_side"))
                .put(TextureSlot.SOUTH, getBlockTexture(block, "_back"))
                .put(TextureSlot.WEST, getBlockTexture(block, "_side"));
        var offModel = ModelTemplates.CUBE.create(block, textureMapping, modelOutput);

        var textureMappingOn = textureMapping.copy()
                .put(TextureSlot.UP, getBlockTexture(block, "_top_on"))
                .put(TextureSlot.NORTH, getBlockTexture(block, "_front_on"))
                .put(TextureSlot.SOUTH, getBlockTexture(block, "_back_on"))
                .put(TextureSlot.PARTICLE, getBlockTexture(block, "_front_on"));
        var onModel = ModelTemplates.CUBE.createWithSuffix(block, "_on", textureMappingOn, modelOutput);

        multiVariantGenerator(
                AEBlocks.VIBRATION_CHAMBER,
                PropertyDispatch.initial(VibrationChamberBlock.ACTIVE)
                        .select(false, plainVariant(offModel))
                        .select(true, plainVariant(onModel)),
                createFacingSpinDispatch());

        // TODO itemModels().withExistingParent(modelPath(AEBlocks.VIBRATION_CHAMBER), offModel);
    }

    private void spatialAnchor() {
        var offModel = AppEng.makeId("block/spatial_anchor");
        var onModel = AppEng.makeId("block/spatial_anchor_on");

        multiVariantGenerator(
                AEBlocks.SPATIAL_ANCHOR,
                PropertyDispatch.initial(SpatialAnchorBlock.POWERED)
                        .select(false, plainVariant(offModel))
                        .select(true, plainVariant(onModel)),
                createFacingDispatch(90, 0));

        // TODO itemModels().withExistingParent(modelPath(AEBlocks.SPATIAL_ANCHOR), offModel);
    }

    private void patternProvider() {
        var def = AEBlocks.PATTERN_PROVIDER;
        var normalModel = TexturedModel.CUBE.create(def.block(), modelOutput);
        // TODO simpleBlockItem(def.block(), normalModel);
        // the block state and the oriented model are in manually written json files

        var orientedModel = makeId("block/pattern_provider_oriented");

        multiVariantGenerator(AEBlocks.PATTERN_PROVIDER,
                PropertyDispatch.initial(PatternProviderBlock.PUSH_DIRECTION).generate(pushDirection -> {
                    var forward = pushDirection.getDirection();
                    if (forward == null) {
                        return plainVariant(normalModel);
                    } else {
                        var orientation = BlockOrientation.get(forward);
                        return plainVariant(orientedModel).with(applyRotation(
                                // + 90 because the default model is oriented UP, while block orientation assumes NORTH
                                orientation.getAngleX() + 90,
                                orientation.getAngleY()));
                    }
                }));
    }

    private void ioPort() {
        var offModel = makeId("block/io_port");
        var onModel = makeId("block/io_port_on");

        multiVariantGenerator(AEBlocks.IO_PORT, PropertyDispatch.initial(IOPortBlock.POWERED)
                .select(false, plainVariant(offModel))
                .select(true, plainVariant(onModel)), createFacingSpinDispatch());
        // TODO itemModels().withExistingParent(modelPath(AEBlocks.IO_PORT), offModel);
    }

    private void spatialIoPort() {
        var offModel = makeId("block/spatial_io_port");
        var onModel = makeId("block/spatial_io_port_on");

        multiVariantGenerator(
                AEBlocks.SPATIAL_IO_PORT,
                PropertyDispatch.initial(SpatialIOPortBlock.POWERED)
                        .select(false, plainVariant(offModel))
                        .select(true, plainVariant(onModel)),
                createFacingSpinDispatch());
        // TODO itemModels().withExistingParent(modelPath(AEBlocks.SPATIAL_IO_PORT), offModel);
    }

    private String modelPath(BlockDefinition<?> block) {
        return block.id().getPath();
    }

    private void builtInModel(BlockDefinition<?> block, CustomUnbakedBlockStateModel model) {
        builtInModel(block, model, false);
    }

    private void builtInModel(BlockDefinition<?> block, CustomUnbakedBlockStateModel model, boolean skipItem) {
        blockModels.blockStateOutput.accept(
                createSimpleBlock(block.block(), customBlockStateModel(model)));

        if (!skipItem) {
            // The item model should not reference the block model since that will be replaced in-code
            blockModels.itemModelOutput.accept(block.item().asItem(), new EmptyModel.Unbaked());
        }
    }

    private void energyCell(
            BlockDefinition<?> blockDef,
            String baseTexture) {

        var block = blockDef.block();

        var energyLevelDispatch = PropertyDispatch.initial(EnergyCellBlock.ENERGY_STORAGE);

        var models = new ArrayList<ResourceLocation>();
        for (var i = 0; i < 5; i++) {
            var textures = TextureMapping.cube(getBlockTexture(block, "_" + i));
            var model = ModelTemplates.CUBE_ALL.createWithSuffix(block, "_" + i, textures, modelOutput);
            models.add(model);
            energyLevelDispatch.select(i, plainVariant(model));
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(energyLevelDispatch));

        var itemLevelEntries = new ArrayList<RangeSelectItemModel.Entry>();
        for (var i = 1; i < models.size(); i++) {
            // The predicate matches "greater than", meaning for fill-level > 0 the first non-empty texture is used
            float fillFactor = i / (float) models.size();
            itemLevelEntries.add(new RangeSelectItemModel.Entry(fillFactor, ItemModelUtils.plainModel(models.get(i))));
        }

        itemModels.itemModelOutput.accept(
                blockDef.asItem(),
                ItemModelUtils.rangeSelect(
                        new EnergyFillLevelProperty(), ItemModelUtils.plainModel(models.getFirst()), itemLevelEntries));

    }

    private void craftingModel(BlockDefinition<?> block, String name, CraftingUnitType type) {
        var unformedModel = ModelTemplates.CUBE_ALL.create(
                makeId("block/crafting/" + name), TextureMapping.cube(makeId("block/crafting/" + name)), modelOutput);
        var formedModel = customBlockStateModel(new CraftingCubeModel.Unbaked(type));

        blockModels.blockStateOutput
                .accept(
                        MultiVariantGenerator.dispatch(block.block())
                                .with(
                                        PropertyDispatch.initial(AbstractCraftingUnitBlock.FORMED)
                                                .select(false,
                                                        plainVariant(unformedModel))
                                                .select(true,
                                                        formedModel)));

        blockModels.registerSimpleItemModel(block.asItem(), unformedModel);
    }

    private void generateQuartzCluster(BlockDefinition<?> quartz) {
        Block block = quartz.block();
        blockModels.blockStateOutput
                .accept(
                        MultiVariantGenerator.dispatch(
                                block,
                                plainVariant(ModelTemplates.CROSS.create(block, TextureMapping.cross(block),
                                        modelOutput)))
                                .with(ROTATIONS_COLUMN_WITH_FACING));
    }
}
