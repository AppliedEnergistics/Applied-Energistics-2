package appeng.datagen.providers.models;

import static net.minecraft.client.data.models.BlockModelGenerators.variant;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.mojang.math.Quadrant;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.core.AppEng;
import appeng.core.definitions.BlockDefinition;

public abstract class ModelSubProvider {
    // TODO 1.21.5 protected static final VariantProperty<VariantProperties.Rotation> Z_ROT = new
    // VariantProperty<>("ae2:z",
    // TODO 1.21.5 r -> new JsonPrimitive(r.ordinal() * 90));

    public static final TextureMapping TRANSPARENT_PARTICLE = TextureMapping
            .particle(AppEng.makeId("block/transparent"));

    public static final ModelTemplate EMPTY_MODEL = new ModelTemplate(Optional.empty(), Optional.empty(),
            TextureSlot.PARTICLE);

    protected final BlockModelGenerators blockModels;
    protected final ItemModelGenerators itemModels;
    protected final BiConsumer<ResourceLocation, ModelInstance> modelOutput;
    protected final PartModelOutput partModels;

    public ModelSubProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels,
            PartModelOutput partModels) {
        this.blockModels = blockModels;
        this.itemModels = itemModels;
        this.partModels = partModels;
        this.modelOutput = blockModels.modelOutput;
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

    @SafeVarargs
    protected final void multiVariantGenerator(BlockDefinition<?> blockDef,
            PropertyDispatch<MultiVariant> dispatch,
            PropertyDispatch<VariantMutator>... dispatchMutators) {
        var generator = MultiVariantGenerator.dispatch(blockDef.block()).with(dispatch);
        for (var dispatchMutator : dispatchMutators) {
            generator = generator.with(dispatchMutator);
        }
        blockModels.blockStateOutput.accept(generator);
    }

    @SafeVarargs
    protected final void multiVariantGenerator(BlockDefinition<?> blockDef,
            MultiVariant baseVariant,
            PropertyDispatch<VariantMutator>... dispatchMutators) {
        var generator = MultiVariantGenerator.dispatch(blockDef.block(), baseVariant);
        for (var dispatchMutator : dispatchMutators) {
            generator = generator.with(dispatchMutator);
        }
        blockModels.blockStateOutput.accept(generator);
    }

    protected static PropertyDispatch<VariantMutator> createFacingDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.modify(BlockStateProperties.FACING)
                .select(Direction.DOWN, applyRotation(baseRotX + 90, baseRotY, 0))
                .select(Direction.UP, applyRotation(baseRotX + 270, baseRotY, 0))
                .select(Direction.NORTH, applyRotation(baseRotX, baseRotY, 0))
                .select(Direction.SOUTH, applyRotation(baseRotX, baseRotY + 180, 0))
                .select(Direction.WEST, applyRotation(baseRotX, baseRotY + 270, 0))
                .select(Direction.EAST, applyRotation(baseRotX, baseRotY + 90, 0));
    }

    protected static PropertyDispatch<VariantMutator> createFacingSpinDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.modify(BlockStateProperties.FACING, IOrientationStrategy.SPIN)
                .generate((facing, spin) -> {
                    var orientation = BlockOrientation.get(facing, spin);
                    return applyRotation(
                            orientation.getAngleX() + baseRotX,
                            orientation.getAngleY() + baseRotY,
                            orientation.getAngleZ());
                });
    }

    protected static PropertyDispatch<VariantMutator> createFacingSpinDispatch() {
        return createFacingSpinDispatch(0, 0);
    }

    protected static void withOrientations(MultiPartGenerator multipart, Variant baseVariant) {
        withOrientations(multipart, ConditionBuilder::new, baseVariant);
    }

    protected static void withOrientations(MultiPartGenerator multipart,
            Supplier<ConditionBuilder> baseCondition, Variant baseVariant) {
        var defaultState = multipart.block().defaultBlockState();
        var strategy = IOrientationStrategy.get(defaultState);

        strategy.getAllStates(defaultState).forEach(blockState -> {
            var condition = baseCondition.get();
            for (var property : strategy.getProperties()) {
                addConditionTerm(condition, blockState, property);
            }

            var orientation = BlockOrientation.get(strategy, blockState);
            multipart.with(condition, variant(applyOrientation(baseVariant, orientation)));
        });
    }

    // TODO 1.21.5 should probably also return a VariantModifier always
    protected static Variant applyOrientation(Variant variant, BlockOrientation orientation) {
        return variant.with(applyRotation(orientation.getAngleX(), orientation.getAngleY(), orientation.getAngleZ()));
    }

    protected static VariantMutator applyRotation(int angleX, int angleY, int angleZ) {
        angleZ = normalizeAngle(angleZ);
        VariantMutator mutator = applyRotation(angleX, angleY);
        if (angleZ != 0) {
            // TODO 1.21.5 mutator = mutator.then(Z_ROT, rotationByAngle(angleZ));
        }
        return mutator;
    }

    protected static VariantMutator applyRotation(int angleX, int angleY) {
        angleX = normalizeAngle(angleX);
        angleY = normalizeAngle(angleY);

        VariantMutator mutator = variant -> variant;
        if (angleX != 0) {
            mutator = mutator.then(VariantMutator.X_ROT.withValue(rotationByAngle(angleX)));
        }
        if (angleY != 0) {
            mutator = mutator.then(VariantMutator.Y_ROT.withValue(rotationByAngle(angleX)));
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

    protected static ResourceLocation getBlockTexture(BlockDefinition<?> block) {
        return TextureMapping.getBlockTexture(block.block());
    }

    protected static ResourceLocation getBlockTexture(Block block) {
        return TextureMapping.getBlockTexture(block);
    }

    protected static ResourceLocation getBlockTexture(Block block, String suffix) {
        return TextureMapping.getBlockTexture(block, suffix);
    }

    protected static ResourceLocation getBlockTexture(BlockDefinition<?> block, String suffix) {
        return TextureMapping.getBlockTexture(block.block(), suffix);
    }

    protected static MultiVariant customBlockStateModel(CustomUnbakedBlockStateModel model) {
        return new MultiVariant(WeightedList.of(), new CustomBlockStateModelBuilder.Simple(model));
    }
}
