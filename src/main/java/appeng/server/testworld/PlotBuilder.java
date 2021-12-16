package appeng.server.testworld;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;

public interface PlotBuilder {

    void addBuildAction(BuildAction action);

    BoundingBox bb(String def);

    default CableBuilder cable(String bb) {
        return cable(bb, AEParts.GLASS_CABLE, AEColor.TRANSPARENT);
    }

    default CableBuilder cable(String bb, ColoredItemDefinition definition) {
        return cable(bb, definition, AEColor.TRANSPARENT);
    }

    default CableBuilder cable(String bb, ColoredItemDefinition definition, AEColor color) {
        return cable(bb, definition.stack(color));
    }

    default CableBuilder cable(String bb, ItemStack what) {
        addBuildAction(new PlacePart(bb(bb), what, null));
        return new CableBuilder(this, bb);
    }

    default void part(String bb, Direction side, ItemDefinition<? extends PartItem<?>> part) {
        addBuildAction(new PlacePart(bb(bb), part.stack(), side));
    }

    default <T extends IPart> void part(String bb,
            Direction side,
            ItemDefinition<? extends PartItem<T>> part,
            Consumer<T> partCustomizer) {
        addBuildAction(new PlacePart(bb(bb), part.stack(), side));
        addBuildAction(new PartCustomizer<>(bb(bb), side, part, partCustomizer));
    }

    default void creativeEnergyCell(String bb) {
        block(bb, AEBlocks.CREATIVE_ENERGY_CELL);
    }

    default void block(String bb, BlockDefinition<?> block) {
        blockState(bb, block.block().defaultBlockState());
    }

    /**
     * Place a block that has a block entity and customize it after it has been placed.
     */
    default <T extends AEBaseBlockEntity> void blockEntity(String bb,
            BlockDefinition<? extends AEBaseEntityBlock<T>> block,
            Consumer<T> postProcessor) {
        blockState(bb, block.block().defaultBlockState());
        var type = block.block().getBlockEntityType();
        addBuildAction(new BlockEntityCustomizer<>(bb(bb), type, postProcessor));
    }

    default void chest(String bb, ItemStack... stacks) {
        block(bb, Blocks.CHEST);
        customizeBlockEntity(bb, BlockEntityType.CHEST, chest -> {
            for (int i = 0; i < stacks.length; i++) {
                chest.setItem(i, stacks[i]);
            }
        });
    }

    default void filledHopper(String bb, Direction direction, ItemLike item) {
        var stack = new ItemStack(item);
        stack.setCount(stack.getMaxStackSize());
        filledHopper(bb, direction, stack);
    }

    default void filledHopper(String bb, Direction direction, ItemStack stack) {
        blockState(bb, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, direction));
        customizeBlockEntity(bb, BlockEntityType.HOPPER, hopper -> {
            for (int i = 0; i < hopper.getContainerSize(); i++) {
                hopper.setItem(i, stack.copy());
            }
        });
    }

    default void hopper(String bb, Direction direction, ItemStack... stacks) {
        blockState(bb, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, direction));
        customizeBlockEntity(bb, BlockEntityType.HOPPER, hopper -> {
            for (int i = 0; i < stacks.length; i++) {
                hopper.setItem(i, stacks[i]);
            }
        });
    }

    default <T extends BlockEntity> void customizeBlockEntity(String bb, BlockEntityType<T> type,
            Consumer<T> consumer) {
        addBuildAction(new BlockEntityCustomizer<T>(bb(bb), type, consumer));
    }

    /**
     * Place the default state of a block.
     */
    default void block(String bb, Block block) {
        blockState(bb, block.defaultBlockState());
    }

    /**
     * Place the default state of a block.
     */
    default void fluid(String bb, Fluid fluid) {
        blockState(bb, fluid.defaultFluidState().createLegacyBlock());
    }

    /**
     * Place a specific block state.
     */
    default void blockState(String bb, BlockState blockState) {
        addBuildAction(new PlaceBlockState(bb(bb), blockState));
    }

    PlotBuilder transform(Function<BoundingBox, BoundingBox> transform);

    /**
     * Returns a plot builder that also applies the given offset to bounding boxes.
     */
    default PlotBuilder offset(int x, int y, int z) {
        return transform(bb -> bb.moved(x, y, z));
    }

    /**
     * Runs a given callback once the grid has been initialized at all viable nodes in the given bounding box.
     */
    default void afterGridInitAt(String bb, BiConsumer<IGrid, IGridNode> consumer) {
        addBuildAction(new PostGridInitAction(bb(bb), consumer));
    }

    void addTest(String name, Consumer<PlotTestHelper> assertion);
}
