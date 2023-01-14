package appeng.datagen.providers.models;

import static appeng.core.AppEng.makeId;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.JsonPrimitive;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.blockstates.VariantProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.QuartzGrowthAcceleratorBlock;
import appeng.block.misc.SecurityStationBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.WirelessBlock;
import appeng.block.orientation.BlockOrientation;
import appeng.block.orientation.IOrientationStrategy;
import appeng.block.spatial.SpatialAnchorBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.storage.ChestBlock;
import appeng.block.storage.IOPortBlock;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.init.client.InitItemModelsProperties;

public class BlockModelProvider extends AE2BlockStateProvider {
    private static final VariantProperty<VariantProperties.Rotation> Z_ROT = new VariantProperty<>("ae2:z",
            r -> new JsonPrimitive(r.ordinal() * 90));

    public BlockModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, AppEng.MOD_ID, exFileHelper);
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

        var charger = models().getExistingFile(AppEng.makeId("charger"));
        multiVariantGenerator(AEBlocks.CHARGER, Variant.variant().with(VariantProperties.MODEL, charger.getLocation()))
                .with(createFacingSpinDispatch());

        var inscriber = models().getExistingFile(AppEng.makeId("inscriber"));
        multiVariantGenerator(AEBlocks.INSCRIBER,
                Variant.variant().with(VariantProperties.MODEL, inscriber.getLocation()))
                        .with(createFacingSpinDispatch());

        wirelessAccessPoint();
        craftingMonitor();
        quartzGrowthAccelerator();
        securityStation();
        meChest();
        patternProvider();

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
        itemModels().cubeAll(modelPath(AEBlocks.SPATIAL_PYLON), makeId("item/spatial_pylon"));

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
        simpleBlockAndItem(AEBlocks.DEBUG_CHUNK_LOADER, "block/debug/chunk_loader");
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
                makeId("block/cell_workbench_bottom"),
                makeId("block/cell_workbench")));

        energyCell(AEBlocks.ENERGY_CELL, "block/energy_cell");
        energyCell(AEBlocks.DENSE_ENERGY_CELL, "block/dense_energy_cell");
        simpleBlockAndItem(AEBlocks.CREATIVE_ENERGY_CELL, "block/creative_energy_cell");

        vibrationChamber();
        spatialAnchor();
        patternProvider();
        ioPorts();

        // Both use the same mysterious cube model
        simpleBlockAndItem(AEBlocks.MYSTERIOUS_CUBE, models().getExistingFile(makeId("block/mysterious_cube")));
        simpleBlockAndItem(AEBlocks.NOT_SO_MYSTERIOUS_CUBE, models().getExistingFile(makeId("block/mysterious_cube")));
    }

    private void meChest() {
        var multipart = multiPartGenerator(AEBlocks.CHEST);
        withOrientations(
                multipart,
                Variant.variant().with(VariantProperties.MODEL, AppEng.makeId("block/chest/base")));
        withOrientations(
                multipart,
                () -> Condition.condition().term(ChestBlock.LIGHTS_ON, false),
                Variant.variant().with(VariantProperties.MODEL, AppEng.makeId("block/chest/lights_off")));
        withOrientations(
                multipart,
                () -> Condition.condition().term(ChestBlock.LIGHTS_ON, true),
                Variant.variant().with(VariantProperties.MODEL, AppEng.makeId("block/chest/lights_on")));
    }

    private static <T extends Comparable<T>> Condition addConditionTerm(Condition.TerminalCondition condition,
            BlockState blockState,
            Property<T> property) {
        return condition.term(property, blockState.getValue(property));
    }

    private void quartzGrowthAccelerator() {
        var unpoweredModel = getExistingModel("block/quartz_growth_accelerator_off");
        var poweredModel = getExistingModel("block/quartz_growth_accelerator_on");

        multiVariantGenerator(AEBlocks.QUARTZ_GROWTH_ACCELERATOR)
                .with(createFacingDispatch(90, 0))
                .with(PropertyDispatch.property(QuartzGrowthAcceleratorBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, unpoweredModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, poweredModel)));
    }

    private void securityStation() {
        var unpoweredModel = getExistingModel("block/security_station_off");
        var poweredModel = getExistingModel("block/security_station_on");

        multiVariantGenerator(AEBlocks.SECURITY_STATION)
                .with(createFacingSpinDispatch())
                .with(PropertyDispatch.property(SecurityStationBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, unpoweredModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, poweredModel)));
    }

    private static PropertyDispatch createFacingDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.property(BlockStateProperties.FACING)
                .select(Direction.DOWN, applyRotation(Variant.variant(), baseRotX + 90, baseRotY, 0))
                .select(Direction.UP, applyRotation(Variant.variant(), baseRotX + 270, baseRotY, 0))
                .select(Direction.NORTH, applyRotation(Variant.variant(), baseRotX, baseRotY, 0))
                .select(Direction.SOUTH, applyRotation(Variant.variant(), baseRotX, baseRotY + 180, 0))
                .select(Direction.WEST, applyRotation(Variant.variant(), baseRotX, baseRotY + 270, 0))
                .select(Direction.EAST, applyRotation(Variant.variant(), baseRotX, baseRotY + 90, 0));
    }

    private static PropertyDispatch createFacingSpinDispatch() {
        var dispatch = PropertyDispatch.properties(BlockStateProperties.FACING, IOrientationStrategy.SPIN);
        return dispatch.generate((facing, spin) -> {
            return applyOrientation(Variant.variant(), BlockOrientation.get(facing, spin));
        });
    }

    private static void withOrientations(MultiPartGenerator multipart, Variant baseVariant) {
        withOrientations(multipart, Condition::condition, baseVariant);
    }

    private static void withOrientations(MultiPartGenerator multipart,
            Supplier<Condition.TerminalCondition> baseCondition, Variant baseVariant) {
        var defaultState = multipart.getBlock().defaultBlockState();
        var strategy = IOrientationStrategy.get(defaultState);

        strategy.getAllStates(defaultState).forEach(blockState -> {
            var condition = baseCondition.get();
            for (var property : strategy.getProperties()) {
                addConditionTerm(condition, blockState, property);
            }

            var orientation = BlockOrientation.get(strategy, blockState);
            var variant = Variant.merge(baseVariant, baseVariant); // Only way to copy...
            multipart.with(condition, applyOrientation(variant, orientation));
        });
    }

    private static Variant applyOrientation(Variant variant, BlockOrientation orientation) {
        return applyRotation(variant,
                orientation.getAngleX(),
                orientation.getAngleY(),
                orientation.getAngleZ());
    }

    private static Variant applyRotation(Variant variant, int angleX, int angleY, int angleZ) {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);
        angleZ = normalizeAngle(angleZ);

        if (angleX != 0) {
            variant = variant.with(VariantProperties.X_ROT, rotationByAngle(angleX));
        }
        if (angleY != 0) {
            variant = variant.with(VariantProperties.Y_ROT, rotationByAngle(angleY));
        }
        if (angleZ != 0) {
            variant = variant.with(Z_ROT, rotationByAngle(angleZ));
        }
        return variant;
    }

    private static int normalizeAngle(int angle) {
        return angle - (angle / 360) * 360;
    }

    private static VariantProperties.Rotation rotationByAngle(int angle) {
        return switch (angle) {
            case 0 -> VariantProperties.Rotation.R0;
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
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

    private MultiVariantGenerator multiVariantGenerator(BlockDefinition<?> blockDef, Variant... variants) {
        if (variants.length == 0) {
            variants = new Variant[] { Variant.variant() };
        }
        var builder = MultiVariantGenerator.multiVariant(blockDef.block(), variants);
        registeredBlocks.put(blockDef.block(), () -> builder.get().getAsJsonObject());
        return builder;
    }

    private MultiPartGenerator multiPartGenerator(BlockDefinition<?> blockDef) {
        var multipart = MultiPartGenerator.multiPart(blockDef.block());
        registeredBlocks.put(blockDef.block(), () -> multipart.get().getAsJsonObject());
        return multipart;
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

            addModel.apply(antennaOff).condition(WirelessBlock.STATE, WirelessBlock.State.OFF).end();
            addModel.apply(statusOff).condition(WirelessBlock.STATE, WirelessBlock.State.OFF).end();

            addModel.apply(antennaOff).condition(WirelessBlock.STATE, WirelessBlock.State.ON).end();
            addModel.apply(statusOn).condition(WirelessBlock.STATE, WirelessBlock.State.ON).end();

            addModel.apply(antennaOn).condition(WirelessBlock.STATE, WirelessBlock.State.HAS_CHANNEL).end();
            addModel.apply(statusOn).condition(WirelessBlock.STATE, WirelessBlock.State.HAS_CHANNEL).end();
        }
    }

    private void vibrationChamber() {
        var offModel = models().orientable(
                modelPath(AEBlocks.VIBRATION_CHAMBER),
                makeId("block/vibration_chamber"),
                makeId("block/vibration_chamber_front"),
                makeId("block/vibration_chamber"));
        var onModel = models().orientable(
                modelPath(AEBlocks.VIBRATION_CHAMBER) + "_on",
                makeId("block/vibration_chamber"),
                makeId("block/vibration_chamber_front_on"),
                makeId("block/vibration_chamber"));

        getVariantBuilder(AEBlocks.VIBRATION_CHAMBER.block())
                .partialState().with(VibrationChamberBlock.ACTIVE, true).setModels(new ConfiguredModel(onModel))
                .partialState().with(VibrationChamberBlock.ACTIVE, false).setModels(new ConfiguredModel(offModel));
        itemModels().withExistingParent(modelPath(AEBlocks.VIBRATION_CHAMBER), offModel.getLocation());
    }

    private void spatialAnchor() {
        var offModel = models().cubeBottomTop(
                modelPath(AEBlocks.SPATIAL_ANCHOR),
                makeId("block/spatial_anchor_side_off"),
                makeId("block/spatial_anchor_bottom"),
                makeId("block/spatial_anchor_top_off"));
        var onModel = models().cubeBottomTop(
                modelPath(AEBlocks.SPATIAL_ANCHOR) + "_on",
                makeId("block/spatial_anchor_side"),
                makeId("block/spatial_anchor_bottom"),
                makeId("block/spatial_anchor_top"));

        getVariantBuilder(AEBlocks.SPATIAL_ANCHOR.block())
                .partialState().with(SpatialAnchorBlock.POWERED, true).setModels(new ConfiguredModel(onModel))
                .partialState().with(SpatialAnchorBlock.POWERED, false).setModels(new ConfiguredModel(offModel));
        itemModels().withExistingParent(modelPath(AEBlocks.SPATIAL_ANCHOR), offModel.getLocation());
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

    private void ioPorts() {
        var ioOff = models().cubeBottomTop(
                modelPath(AEBlocks.IO_PORT),
                makeId("block/io_port_side_off"),
                makeId("block/io_port_bottom"),
                makeId("block/io_port_top_off"));
        var ioOn = models().cubeBottomTop(
                modelPath(AEBlocks.IO_PORT) + "_on",
                makeId("block/io_port_side"),
                makeId("block/io_port_bottom"),
                makeId("block/io_port_top"));
        var spatialOff = models().cubeBottomTop(
                modelPath(AEBlocks.SPATIAL_IO_PORT),
                makeId("block/spatial_io_port_side_off"),
                makeId("block/spatial_io_port_bottom"),
                makeId("block/spatial_io_port_top_off"));
        var spatialOn = models().cubeBottomTop(
                modelPath(AEBlocks.SPATIAL_IO_PORT) + "_on",
                makeId("block/spatial_io_port_side"),
                makeId("block/spatial_io_port_bottom"),
                makeId("block/spatial_io_port_top"));

        getVariantBuilder(AEBlocks.IO_PORT.block())
                .partialState().with(IOPortBlock.POWERED, true).setModels(new ConfiguredModel(ioOn))
                .partialState().with(IOPortBlock.POWERED, false).setModels(new ConfiguredModel(ioOff));
        getVariantBuilder(AEBlocks.SPATIAL_IO_PORT.block())
                .partialState().with(SpatialIOPortBlock.POWERED, true).setModels(new ConfiguredModel(spatialOn))
                .partialState().with(SpatialIOPortBlock.POWERED, false).setModels(new ConfiguredModel(spatialOff));
        itemModels().withExistingParent(modelPath(AEBlocks.IO_PORT), ioOff.getLocation());
        itemModels().withExistingParent(modelPath(AEBlocks.SPATIAL_IO_PORT), spatialOff.getLocation());
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
        var model = models().getBuilder("block/" + name);
        var loaderId = AppEng.makeId("block/" + name);
        model.customLoader((bmb, efh) -> new CustomLoaderBuilder<>(loaderId, bmb, efh) {
        });
        return model;
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

        var item = itemModels().withExistingParent(modelPath(block), models.get(0).getLocation());
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

    private void generateQuartzCluster(BlockDefinition<?> quartz) {
        var name = quartz.id().getPath();
        var texture = makeId("block/" + name);
        var model = models().cross(name, texture);
        directionalBlock(quartz.block(), model);
        itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", texture);
    }

    private ResourceLocation getExistingModel(String name) {
        return models().getExistingFile(AppEng.makeId(name)).getLocation();
    }
}
