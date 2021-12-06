package appeng.server.testworld;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;

public interface PlotBuilder {

    void addBuildAction(BuildAction action);

    BoundingBox bb(String def);

    default void cable(String bb, ColoredItemDefinition definition) {
        cable(bb, definition, AEColor.TRANSPARENT);
    }

    default void cable(String bb, ColoredItemDefinition definition, AEColor color) {
        cable(bb, definition.stack(color));
    }

    default void cable(String bb, ItemStack what) {
        addBuildAction(new PlacePart(bb(bb), what, null));
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
}
