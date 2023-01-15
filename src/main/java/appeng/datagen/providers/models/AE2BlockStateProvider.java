package appeng.datagen.providers.models;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.core.AppEng;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public abstract class AE2BlockStateProvider extends BlockStateProvider implements IAE2DataProvider {
    private static final VariantProperty<VariantProperties.Rotation> Z_ROT = new VariantProperty<>("ae2:z",
            r -> new JsonPrimitive(r.ordinal() * 90));

    public AE2BlockStateProvider(PackOutput packOutput, String modid, ExistingFileHelper exFileHelper) {
        super(packOutput, modid, exFileHelper);
    }

    /**
     * Define a block model that is a simple textured cube, and uses the same model for its item. The texture path is
     * derived from the block's id.
     */
    protected void simpleBlockAndItem(BlockDefinition<?> block) {
        var model = cubeAll(block.block());
        simpleBlock(block.block(), model);
        simpleBlockItem(block.block(), model);
    }

    protected void simpleBlockAndItem(BlockDefinition<?> block, ModelFile model) {
        simpleBlock(block.block(), model);
        simpleBlockItem(block.block(), model);
    }

    /**
     * Define a block model that is a simple textured cube, and uses the same model for its item.
     */
    protected void simpleBlockAndItem(BlockDefinition<?> block, String textureName) {
        var model = models().cubeAll(block.id().getPath(), AppEng.makeId(textureName));
        simpleBlock(block.block(), model);
        simpleBlockItem(block.block(), model);
    }

    /**
     * Defines a standard wall blockstate, the necessary block models and item model.
     */
    protected void wall(BlockDefinition<WallBlock> block, String texture) {
        wallBlock(block.block(), AppEng.makeId(texture));
        itemModels().wallInventory(block.id().getPath(), AppEng.makeId(texture));
    }

    protected void slabBlock(BlockDefinition<SlabBlock> slab, BlockDefinition<?> base) {
        var texture = blockTexture(base.block()).getPath();
        slabBlock(slab, base, texture, texture, texture);
    }

    protected void slabBlock(BlockDefinition<SlabBlock> slab, BlockDefinition<?> base, String bottomTexture,
            String sideTexture, String topTexture) {
        var side = AppEng.makeId(sideTexture);
        var bottom = AppEng.makeId(bottomTexture);
        var top = AppEng.makeId(topTexture);

        var bottomModel = models().slab(slab.id().getPath(), side, bottom, top);
        simpleBlockItem(slab.block(), bottomModel);
        slabBlock(
                slab.block(),
                bottomModel,
                models().slabTop(slab.id().getPath() + "_top", side, bottom, top),
                models().getExistingFile(base.id()));
    }

    protected void stairsBlock(BlockDefinition<StairBlock> stairs, BlockDefinition<?> base) {
        var texture = "block/" + base.id().getPath();

        stairsBlock(stairs, texture, texture, texture);
    }

    protected void stairsBlock(BlockDefinition<StairBlock> stairs, String bottomTexture, String sideTexture,
            String topTexture) {
        var baseName = stairs.id().getPath();

        var side = AppEng.makeId(sideTexture);
        var bottom = AppEng.makeId(bottomTexture);
        var top = AppEng.makeId(topTexture);

        ModelFile stairsModel = models().stairs(baseName, side, bottom, top);
        ModelFile stairsInner = models().stairsInner(baseName + "_inner", side, bottom, top);
        ModelFile stairsOuter = models().stairsOuter(baseName + "_outer", side, bottom, top);
        stairsBlock(stairs.block(), stairsModel, stairsInner, stairsOuter);
        simpleBlockItem(stairs.block(), stairsModel);
    }

    protected VariantsBuilder rotatedVariants(BlockDefinition<?> blockDef) {
        Block block = blockDef.block();
        var builder = new VariantsBuilder(block);
        registeredBlocks.put(block, builder);
        return builder;
    }

    protected final MultiVariantGenerator multiVariantGenerator(BlockDefinition<?> blockDef, Variant... variants) {
        if (variants.length == 0) {
            variants = new Variant[] { Variant.variant() };
        }
        var builder = MultiVariantGenerator.multiVariant(blockDef.block(), variants);
        registeredBlocks.put(blockDef.block(), () -> builder.get().getAsJsonObject());
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
        registeredBlocks.put(blockDef.block(), () -> multipart.get().getAsJsonObject());
        return multipart;
    }

    private static <T extends Comparable<T>> Condition addConditionTerm(Condition.TerminalCondition condition,
            BlockState blockState,
            Property<T> property) {
        return condition.term(property, blockState.getValue(property));
    }

    @Override
    public String getName() {
        return super.getName() + " " + getClass().getName();
    }
}
