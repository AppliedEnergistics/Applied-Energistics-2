package appeng.datagen.providers.models;

import appeng.api.orientation.BlockOrientation;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.misc.GrowthAcceleratorBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.block.networking.WirelessAccessPointBlock;
import appeng.block.spatial.SpatialAnchorBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.storage.IOPortBlock;
import appeng.block.storage.MEChestBlock;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.Condition;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static appeng.core.AppEng.makeId;

public class BlockModelProvider extends AE2BlockStateProvider {

    public static final ModelTemplate EMPTY_MODEL = new ModelTemplate(Optional.empty(), Optional.empty());

    public BlockModelProvider(PackOutput packOutput) {
        super(packOutput, AppEng.MOD_ID);
    }

    @Override
    protected void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialBlock(AEBlocks.MATRIX_FRAME.block(), TexturedModel.createDefault(block -> new TextureMapping(), EMPTY_MODEL));

        // These models will be overwritten in code
        builtInModel(AEBlocks.QUARTZ_GLASS, true);
        builtInModel(AEBlocks.CABLE_BUS);
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

        builtInModel("spatial_pylon");
        builtInModel("qnb/qnb_formed");
        builtInModel("crafting/unit_formed");
        builtInModel("crafting/accelerator_formed");
        builtInModel("crafting/1k_storage_formed");
        builtInModel("crafting/4k_storage_formed");
        builtInModel("crafting/16k_storage_formed");
        builtInModel("crafting/64k_storage_formed");
        builtInModel("crafting/256k_storage_formed");

//       // Spatial pylon uses a normal model for the item, special model for block
//       simpleBlock(AEBlocks.SPATIAL_PYLON.block(), models().getBuilder(modelPath(AEBlocks.SPATIAL_PYLON)));
//       itemModels().cubeAll(modelPath(AEBlocks.SPATIAL_PYLON), makeId("block/spatial_pylon/spatial_pylon_item"));

//       simpleBlockAndItem(AEBlocks.FLAWLESS_BUDDING_QUARTZ);
//       simpleBlockAndItem(AEBlocks.FLAWED_BUDDING_QUARTZ);
//       simpleBlockAndItem(AEBlocks.CHIPPED_BUDDING_QUARTZ);
//       simpleBlockAndItem(AEBlocks.DAMAGED_BUDDING_QUARTZ);

//       generateQuartzCluster(AEBlocks.SMALL_QUARTZ_BUD);
//       generateQuartzCluster(AEBlocks.MEDIUM_QUARTZ_BUD);
//       generateQuartzCluster(AEBlocks.LARGE_QUARTZ_BUD);
//       generateQuartzCluster(AEBlocks.QUARTZ_CLUSTER);

//       simpleBlockAndItem(AEBlocks.CONDENSER);
//       simpleBlockAndItem(AEBlocks.ENERGY_ACCEPTOR);
//       simpleBlockAndItem(AEBlocks.INTERFACE);

//       simpleBlockAndItem(AEBlocks.DEBUG_ITEM_GEN, "block/debug/item_gen");
//       simpleBlockAndItem(AEBlocks.DEBUG_PHANTOM_NODE, "block/debug/phantom_node");
//       simpleBlockAndItem(AEBlocks.DEBUG_CUBE_GEN, "block/debug/cube_gen");
//       simpleBlockAndItem(AEBlocks.DEBUG_ENERGY_GEN, "block/debug/energy_gen");

//       craftingModel(AEBlocks.CRAFTING_ACCELERATOR, "accelerator");
//       craftingModel(AEBlocks.CRAFTING_UNIT, "unit");
//       craftingModel(AEBlocks.CRAFTING_STORAGE_1K, "1k_storage");
//       craftingModel(AEBlocks.CRAFTING_STORAGE_4K, "4k_storage");
//       craftingModel(AEBlocks.CRAFTING_STORAGE_16K, "16k_storage");
//       craftingModel(AEBlocks.CRAFTING_STORAGE_64K, "64k_storage");
//       craftingModel(AEBlocks.CRAFTING_STORAGE_256K, "256k_storage");

//       simpleBlockAndItem(AEBlocks.CELL_WORKBENCH, models().cubeBottomTop(
//               modelPath(AEBlocks.CELL_WORKBENCH),
//               makeId("block/cell_workbench_side"),
//               makeId("block/generics/bottom"),
//               makeId("block/cell_workbench_top")));

//       energyCell(AEBlocks.ENERGY_CELL, "block/energy_cell");
//       energyCell(AEBlocks.DENSE_ENERGY_CELL, "block/dense_energy_cell");
//       simpleBlockAndItem(AEBlocks.CREATIVE_ENERGY_CELL, "block/creative_energy_cell");

//       // Both use the same mysterious cube model
//       simpleBlockAndItem(AEBlocks.MYSTERIOUS_CUBE, (makeId("block/mysterious_cube")));
//       simpleBlockAndItem(AEBlocks.NOT_SO_MYSTERIOUS_CUBE, (makeId("block/mysterious_cube")));
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
        var unpoweredModel = TexturedModel.CUBE_TOP_BOTTOM.create(AEBlocks.GROWTH_ACCELERATOR.block(), modelOutput);
        var poweredModel = TexturedModel.CUBE_TOP_BOTTOM
                .createWithSuffix(AEBlocks.GROWTH_ACCELERATOR.block(), "_on", modelOutput);

        multiVariantGenerator(AEBlocks.GROWTH_ACCELERATOR)
                .with(createFacingDispatch(90, 0))
                .with(PropertyDispatch.property(GrowthAcceleratorBlock.POWERED)
                        .select(false, Variant.variant().with(VariantProperties.MODEL, unpoweredModel))
                        .select(true, Variant.variant().with(VariantProperties.MODEL, poweredModel)));

        itemModels.itemModelOutput.accept(AEBlocks.GROWTH_ACCELERATOR.asItem(), ItemModelUtils.plainModel(unpoweredModel));
    }

    private void craftingMonitor() {
        var formedModel = makeId("block/crafting/monitor_formed");
        var unformedModel = makeId("block/crafting/monitor");

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
            var rotation = BlockOrientation.get(facing, 0);

            facingVariants.select(facing, Variant.variant()
                    .with(VariantProperties.MODEL, modelFile)
                    // The original is facing "up" while we generally assume models are facing north
                    // but this looks better as an item model
                    .with(VariantProperties.X_ROT, ofDegrees(rotation.getAngleX() + 90))
                    .with(VariantProperties.Y_ROT, ofDegrees(rotation.getAngleY())));
        }

        blockModels.registerSimpleItemModel(AEBlocks.CRYSTAL_RESONANCE_GENERATOR.block(), modelFile);
    }

    private VariantProperties.Rotation ofDegrees(int degrees) {
        degrees %= 360;
        return switch (degrees) {
            case 0 -> VariantProperties.Rotation.R0;
            case 90 -> VariantProperties.Rotation.R90;
            case 180 -> VariantProperties.Rotation.R180;
            case 270 -> VariantProperties.Rotation.R270;
            default -> throw new IllegalStateException("Unexpected value: " + degrees);
        };
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
                    Variant.variant().with(VariantProperties.MODEL, chassis)
                            .with(VariantProperties.X_ROT, ofDegrees(rotation.getAngleX()))
                            .with(VariantProperties.Y_ROT, ofDegrees(rotation.getAngleY()))
            );

            BiConsumer<ResourceLocation, WirelessAccessPointBlock.State> addModel = (modelFile, state) -> builder.with(
                    Condition.condition().term(BlockStateProperties.FACING, facing)
                            .term(WirelessAccessPointBlock.STATE, state),
                    Variant.variant().with(VariantProperties.MODEL, modelFile)
                            .with(VariantProperties.X_ROT, ofDegrees(rotation.getAngleX()))
                            .with(VariantProperties.Y_ROT, ofDegrees(rotation.getAngleY()))
            );
            addModel.accept(antennaOff, WirelessAccessPointBlock.State.OFF);
            addModel.accept(statusOff, WirelessAccessPointBlock.State.OFF);
            addModel.accept(antennaOff, WirelessAccessPointBlock.State.ON);
            addModel.accept(statusOn, WirelessAccessPointBlock.State.ON);
            addModel.accept(antennaOn, WirelessAccessPointBlock.State.HAS_CHANNEL);
            addModel.accept(statusOn, WirelessAccessPointBlock.State.HAS_CHANNEL);
        }
    }

    private void vibrationChamber() {
        var offModel = TexturedModel.CUBE.create(AEBlocks.VIBRATION_CHAMBER.block(), modelOutput);
        var onModel = TexturedModel.CUBE.createWithSuffix(AEBlocks.VIBRATION_CHAMBER.block(), "_on", modelOutput);
       // TODO  var offModel = models().cube(
       // TODO          modelPath(AEBlocks.VIBRATION_CHAMBER),
       // TODO          makeId("block/generics/bottom"),
       // TODO          makeId("block/vibration_chamber_top"),
       // TODO          makeId("block/vibration_chamber_front"),
       // TODO          makeId("block/vibration_chamber_back"),
       // TODO          makeId("block/vibration_chamber_side"),
       // TODO          makeId("block/vibration_chamber_side"))
       // TODO          .texture("particle", makeId("block/vibration_chamber_front"));
       // TODO  var onModel = models().cube(
       // TODO          modelPath(AEBlocks.VIBRATION_CHAMBER) + "_on",
       // TODO          makeId("block/generics/bottom"),
       // TODO          makeId("block/vibration_chamber_top_on"),
       // TODO          makeId("block/vibration_chamber_front_on"),
       // TODO          makeId("block/vibration_chamber_back_on"),
       // TODO          makeId("block/vibration_chamber_side"),
       // TODO          makeId("block/vibration_chamber_side")).texture("particle", makeId("block/vibration_chamber_front_on"));

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
        blockModels.modelOutput.accept(modelId, JsonObject::new);
        return modelId;
    }

    private void builtInModel(BlockDefinition<?> block) {
        builtInModel(block, false);
    }

    private void builtInModel(BlockDefinition<?> block, boolean skipItem) {

        blockModels.createTrivialBlock(block.block(), TexturedModel.createDefault(ignored -> new TextureMapping(), EMPTY_MODEL));

        if (!skipItem) {
            // The item model should not reference the block model since that will be replaced in-code
            blockModels.itemModelOutput.accept(block.item().asItem(), new EmptyModel.Unbaked());
        }
    }

//   private void energyCell(
//           BlockDefinition<?> block,
//           String baseTexture) {

//       var blockBuilder = getVariantBuilder(block.block());
//       var models = new ArrayList<ModelFile>();
//       for (var i = 0; i < 5; i++) {
//           var model = models().cubeAll(modelPath(block) + "_" + i, makeId(baseTexture + "_" + i));
//           blockBuilder.partialState().with(EnergyCellBlock.ENERGY_STORAGE, i).setModels(new ConfiguredModel(model));
//           models.add(model);
//       }

//       var item = itemModels().withExistingParent(modelPath(block), models.get(0));
//       for (var i = 1; i < models.size(); i++) {
//           // The predicate matches "greater than", meaning for fill-level > 0 the first non-empty texture is used
//           float fillFactor = i / (float) models.size();
//           item.override()
//                   .predicate(InitItemModelsProperties.ENERGY_FILL_LEVEL_ID, fillFactor)
//                   .model(models.get(i));
//       }
//   }

//   private void craftingModel(BlockDefinition<?> block, String name) {
//       var blockModel = models().cubeAll("block/crafting/" + name, makeId("block/crafting/" + name));
//       getVariantBuilder(block.block())
//               .partialState().with(AbstractCraftingUnitBlock.FORMED, false).setModels(
//                       new ConfiguredModel(blockModel))
//               .partialState().with(AbstractCraftingUnitBlock.FORMED, true).setModels(
//                       // Empty model, will be replaced dynamically
//                       new ConfiguredModel(models().getBuilder("block/crafting/" + name + "_formed")));
//       simpleBlockItem(block.block(), blockModel);
//   }

//   private void generateQuartzCluster(BlockDefinition<?> quartz) {
//       var name = quartz.id().getPath();
//       var texture = makeId("block/" + name);
//       var model = models().cross(name, texture).renderType("cutout");
//       directionalBlock(quartz.block(), model);
//       itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", texture);
//   }
}
