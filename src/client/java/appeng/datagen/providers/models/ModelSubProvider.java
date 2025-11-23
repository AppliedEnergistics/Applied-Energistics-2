package appeng.datagen.providers.models;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.math.Quadrant;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;
import net.neoforged.neoforge.client.model.generators.blockstate.UnbakedMutator;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.client.model.SpinnableVariant;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.SingleSpinnableVariant;
import appeng.core.AppEng;
import appeng.core.definitions.BlockDefinition;

public abstract class ModelSubProvider {
    public static final TextureMapping TRANSPARENT_PARTICLE = TextureMapping
            .particle(AppEng.makeId("block/transparent"));

    public static final ModelTemplate EMPTY_MODEL = new ModelTemplate(Optional.empty(), Optional.empty(),
            TextureSlot.PARTICLE);

    protected final BlockModelGenerators blockModels;
    protected final Consumer<BlockModelDefinitionGenerator> blockStateOutput;
    protected final ItemModelGenerators itemModels;
    protected final BiConsumer<Identifier, ModelInstance> modelOutput;
    protected final PartModelOutput partModels;

    public ModelSubProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
            PartModelOutput partModels) {
        this.blockModels = blockModels;
        this.itemModels = itemModels;
        this.partModels = partModels;
        this.modelOutput = blockModels.modelOutput;
        this.blockStateOutput = blockModels.blockStateOutput;
    }

    protected abstract void register();

    /**
     * Define a block model that is a simple textured cube, and uses the same model for its item. The texture path is
     * derived from the block's id.
     */
    protected void simpleBlockAndItem(BlockDefinition<?> block) {
        blockModels.createTrivialCube(block.block());
        // item falls back automatically to the block model
    }

    protected void simpleBlockAndItem(BlockDefinition<?> block, TexturedModel.Provider model) {
        blockModels.createTrivialBlock(block.block(), model);
        // item falls back automatically to the block model
    }

    /**
     * Define a block model that is a simple textured cube, and uses the same model for its item.
     */
    protected void simpleBlockAndItem(BlockDefinition<?> block, String textureName) {
        blockModels.createTrivialBlock(
                block.block(),
                TexturedModel.CUBE.updateTexture(mapping -> mapping.put(TextureSlot.ALL, AppEng.makeId(textureName))));
        // item falls back automatically to the block model
    }

    protected MultiVariantGenerator createSpinnableBlock(BlockDefinition<?> block, Identifier model) {
        return MultiVariantGenerator.dispatch(block.block(), plainSpinnableVariant(model))
                .withUnbaked(createFacingSpinDispatch());
    }

    protected static PropertyDispatch<VariantMutator> createFacingDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.modify(BlockStateProperties.FACING)
                .select(Direction.DOWN, applyRotation(baseRotX + 90, baseRotY))
                .select(Direction.UP, applyRotation(baseRotX + 270, baseRotY))
                .select(Direction.NORTH, applyRotation(baseRotX, baseRotY))
                .select(Direction.SOUTH, applyRotation(baseRotX, baseRotY + 180))
                .select(Direction.WEST, applyRotation(baseRotX, baseRotY + 270))
                .select(Direction.EAST, applyRotation(baseRotX, baseRotY + 90));
    }

    protected static PropertyDispatch<UnbakedMutator> createFacingSpinDispatch() {
        return PropertyDispatch.modifyUnbaked(BlockStateProperties.FACING, IOrientationStrategy.SPIN)
                .generate((facing, spin) -> {
                    var orientation = BlockOrientation.get(facing, spin);
                    return UnbakedMutator.builder()
                            .add(SingleSpinnableVariant.Unbaked.class, unbaked -> new SingleSpinnableVariant.Unbaked(
                                    applyOrientation(unbaked.variant(), orientation)))
                            .add(DriveModel.Unbaked.class, unbaked -> new DriveModel.Unbaked(
                                    applyOrientation(unbaked.variant(), orientation)))
                            .build();
                });
    }

    protected static void withOrientations(MultiPartGenerator multipart, SpinnableVariant baseVariant) {
        withOrientations(multipart, ConditionBuilder::new, baseVariant);
    }

    protected static void withOrientations(MultiPartGenerator multipart,
            Supplier<ConditionBuilder> baseCondition,
            SpinnableVariant baseVariant) {
        var defaultState = multipart.block().defaultBlockState();
        var strategy = IOrientationStrategy.get(defaultState);

        strategy.getAllStates(defaultState).forEach(blockState -> {
            var condition = baseCondition.get();
            for (var property : strategy.getProperties()) {
                addConditionTerm(condition, blockState, property);
            }
            var orientation = BlockOrientation.get(strategy, blockState);
            multipart.with(condition, customBlockStateModel(
                    new SingleSpinnableVariant.Unbaked(applyOrientation(baseVariant, orientation))));
        });
    }

    protected static SpinnableVariant applyOrientation(SpinnableVariant baseVariant, BlockOrientation orientation) {
        return applyOrientation(baseVariant, orientation.getAngleX(), orientation.getAngleY(), orientation.getAngleZ());
    }

    protected static SpinnableVariant applyOrientation(SpinnableVariant baseVariant, int angleX, int angleY,
            int angleZ) {
        return baseVariant
                .withXRot(rotationByAngle(normalizeAngle(angleX)))
                .withYRot(rotationByAngle(normalizeAngle(angleY)))
                .withZRot(rotationByAngle(normalizeAngle(angleZ)));
    }

    protected static Variant applyOrientation(Variant baseVariant, Direction facing) {
        var orientation = BlockOrientation.get(facing);

        return baseVariant
                .withXRot(rotationByAngle(orientation.getAngleX()))
                .withYRot(rotationByAngle(orientation.getAngleY()));
    }

    protected static VariantMutator applyRotation(int angleX, int angleY) {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);

        VariantMutator mutator = variant -> variant;
        if (angleX != 0) {
            mutator = mutator.then(VariantMutator.X_ROT.withValue(rotationByAngle(angleX)));
        }
        if (angleY != 0) {
            mutator = mutator.then(VariantMutator.Y_ROT.withValue(rotationByAngle(angleY)));
        }
        return mutator;
    }

    private static int normalizeAngle(int angle) {
        return angle - (angle / 360) * 360;
    }

    private static Quadrant rotationByAngle(int angle) {
        return switch (angle) {
            case 0 -> Quadrant.R0;
            case 90 -> Quadrant.R90;
            case 180 -> Quadrant.R180;
            case 270 -> Quadrant.R270;
            default -> throw new IllegalArgumentException("Invalid angle: " + angle);
        };
    }

    protected final MultiPartGenerator multiPartGenerator(BlockDefinition<?> blockDef) {
        var multipart = MultiPartGenerator.multiPart(blockDef.block());
        blockModels.blockStateOutput.accept(multipart);
        return multipart;
    }

    private static <T extends Comparable<T>> ConditionBuilder addConditionTerm(ConditionBuilder conditionBuilder,
            BlockState blockState,
            Property<T> property) {
        return conditionBuilder.term(property, blockState.getValue(property));
    }

    protected static Identifier getBlockTexture(BlockDefinition<?> block) {
        return TextureMapping.getBlockTexture(block.block());
    }

    protected static Identifier getBlockTexture(Block block) {
        return TextureMapping.getBlockTexture(block);
    }

    protected static Identifier getBlockTexture(Block block, String suffix) {
        return TextureMapping.getBlockTexture(block, suffix);
    }

    protected static Identifier getBlockTexture(BlockDefinition<?> block, String suffix) {
        return TextureMapping.getBlockTexture(block.block(), suffix);
    }

    protected static MultiVariant customBlockStateModel(CustomUnbakedBlockStateModel model) {
        return MultiVariant.of(new CustomBlockStateModelBuilder.Simple(model));
    }

    public static MultiVariant variant(SpinnableVariant variant) {
        return customBlockStateModel(new SingleSpinnableVariant.Unbaked(variant));
    }

    public static MultiVariant variant(Variant variant) {
        return new MultiVariant(WeightedList.of(variant));
    }

    public static MultiVariant plainSpinnableVariant(Identifier model) {
        return variant(new SpinnableVariant(model));
    }

    public static MultiVariantGenerator createSimpleBlock(BlockDefinition<?> block, MultiVariant variant) {
        return BlockModelGenerators.createSimpleBlock(block.block(), variant);
    }
}
