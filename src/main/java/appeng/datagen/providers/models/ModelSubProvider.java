package appeng.datagen.providers.models;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.client.render.model.BuiltInModelLoaderBuilder;
import appeng.core.AppEng;
import appeng.core.definitions.BlockDefinition;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.Condition;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.blockstates.VariantProperty;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class ModelSubProvider {
    protected static final VariantProperty<VariantProperties.Rotation> Z_ROT = new VariantProperty<>("ae2:z",
            r -> new JsonPrimitive(r.ordinal() * 90));

    public static final TextureMapping TRANSPARENT_PARTICLE = TextureMapping.particle(AppEng.makeId("block/transparent"));

    public static final ModelTemplate EMPTY_MODEL = new ModelTemplate(Optional.empty(), Optional.empty(), TextureSlot.PARTICLE);

    protected final BlockModelGenerators blockModels;
    protected final ItemModelGenerators itemModels;
    protected final BiConsumer<ResourceLocation, ModelInstance> modelOutput;

    public ModelSubProvider(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.blockModels = blockModels;
        this.itemModels = itemModels;
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
                TexturedModel.CUBE.updateTexture(mapping -> mapping.put(TextureSlot.ALL, AppEng.makeId(textureName)))
        );
        // item falls back automatically to the block model
    }

    protected final MultiVariantGenerator multiVariantGenerator(BlockDefinition<?> blockDef, Variant... variants) {
        if (variants.length == 0) {
            variants = new Variant[]{Variant.variant()};
        }
        var builder = MultiVariantGenerator.multiVariant(blockDef.block(), variants);
        blockModels.blockStateOutput.accept(builder);
        return builder;
    }

    protected static PropertyDispatch createFacingDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.property(BlockStateProperties.FACING)
                .select(Direction.DOWN, applyRotation(Variant.variant(), baseRotX + 90, baseRotY, 0))
                .select(Direction.UP, applyRotation(Variant.variant(), baseRotX + 270, baseRotY, 0))
                .select(Direction.NORTH, applyRotation(Variant.variant(), baseRotX, baseRotY, 0))
                .select(Direction.SOUTH, applyRotation(Variant.variant(), baseRotX, baseRotY + 180, 0))
                .select(Direction.WEST, applyRotation(Variant.variant(), baseRotX, baseRotY + 270, 0))
                .select(Direction.EAST, applyRotation(Variant.variant(), baseRotX, baseRotY + 90, 0));
    }

    protected static PropertyDispatch createFacingSpinDispatch(int baseRotX, int baseRotY) {
        return PropertyDispatch.properties(BlockStateProperties.FACING, IOrientationStrategy.SPIN)
                .generate((facing, spin) -> {
                    var orientation = BlockOrientation.get(facing, spin);
                    return applyRotation(
                            Variant.variant(),
                            orientation.getAngleX() + baseRotX,
                            orientation.getAngleY() + baseRotY,
                            orientation.getAngleZ());
                });
    }

    protected static PropertyDispatch createFacingSpinDispatch() {
        return createFacingSpinDispatch(0, 0);
    }

    protected static void withOrientations(MultiPartGenerator multipart, Variant baseVariant) {
        withOrientations(multipart, Condition::condition, baseVariant);
    }

    protected static void withOrientations(MultiPartGenerator multipart,
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

    protected static Variant applyOrientation(Variant variant, BlockOrientation orientation) {
        return applyRotation(variant,
                orientation.getAngleX(),
                orientation.getAngleY(),
                orientation.getAngleZ());
    }

    protected static Variant applyRotation(Variant variant, int angleX, int angleY, int angleZ) {
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

    protected final MultiPartGenerator multiPartGenerator(BlockDefinition<?> blockDef) {
        var multipart = MultiPartGenerator.multiPart(blockDef.block());
        blockModels.blockStateOutput.accept(multipart);
        return multipart;
    }

    private static <T extends Comparable<T>> Condition addConditionTerm(Condition.TerminalCondition condition,
                                                                        BlockState blockState,
                                                                        Property<T> property) {
        return condition.term(property, blockState.getValue(property));
    }

    protected final ResourceLocation createBuiltInModel(ResourceLocation id) {
        return EMPTY_MODEL
                .extend()
                .customLoader(BuiltInModelLoaderBuilder::new, builder -> builder.id(id))
                .build()
                .create(id, TRANSPARENT_PARTICLE, modelOutput);
    }
}
