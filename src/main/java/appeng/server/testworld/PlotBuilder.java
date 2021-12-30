package appeng.server.testworld;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColor;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ColoredItemDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;

public interface PlotBuilder {

    void addBuildAction(BuildAction action);

    BoundingBox bb(String def);

    static String posToBb(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    default CableBuilder cable(BlockPos pos) {
        return cable(posToBb(pos));
    }

    default CableBuilder cable(String bb) {
        return cable(bb, AEParts.GLASS_CABLE, AEColor.TRANSPARENT);
    }

    default CableBuilder cable(String bb, ColoredItemDefinition<? extends IPartItem<?>> definition) {
        return cable(bb, definition, AEColor.TRANSPARENT);
    }

    default CableBuilder cable(String bb, ColoredItemDefinition<? extends IPartItem<?>> definition, AEColor color) {
        return cable(bb, definition.item(color));
    }

    default CableBuilder cable(String bb, IPartItem<?> what) {
        addBuildAction(new PlacePart(bb(bb), what, null));
        return new CableBuilder(this, bb);
    }

    default CableBuilder denseCable(BlockPos pos) {
        return cable(posToBb(pos), AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT));
    }

    default void part(String bb, Direction side, ItemDefinition<? extends PartItem<?>> part) {
        addBuildAction(new PlacePart(bb(bb), part.asItem(), side));
    }

    default <T extends IPart> void part(String bb,
            Direction side,
            ItemDefinition<? extends PartItem<T>> part,
            Consumer<T> partCustomizer) {
        addBuildAction(new PlacePart(bb(bb), part.asItem(), side));
        addBuildAction(new PartCustomizer<>(bb(bb), side, part, partCustomizer));
    }

    default void creativeEnergyCell(BlockPos pos) {
        creativeEnergyCell(posToBb(pos));
    }

    default void creativeEnergyCell(String bb) {
        block(bb, AEBlocks.CREATIVE_ENERGY_CELL);
    }

    /**
     * place a lever on the side of the given block. returns the levers position.
     */
    default BlockPos leverOn(BlockPos pos, Direction side) {
        var leverPos = pos.relative(side);
        AttachFace face = AttachFace.WALL;
        if (side == Direction.UP) {
            face = AttachFace.CEILING;
            side = Direction.EAST;
        } else if (side == Direction.DOWN) {
            face = AttachFace.FLOOR;
            side = Direction.EAST;
        }
        var state = Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, face)
                .setValue(LeverBlock.FACING, side);
        blockState(leverPos, state);
        return leverPos;
    }

    /**
     * place a button on the side of the given block. returns the button position.
     */
    default BlockPos buttonOn(BlockPos pos, Direction side) {
        var leverPos = pos.relative(side);
        AttachFace face = AttachFace.WALL;
        if (side == Direction.UP) {
            face = AttachFace.CEILING;
            side = Direction.EAST;
        } else if (side == Direction.DOWN) {
            face = AttachFace.FLOOR;
            side = Direction.EAST;
        }
        var state = Blocks.POLISHED_BLACKSTONE_BUTTON.defaultBlockState()
                .setValue(ButtonBlock.FACE, face)
                .setValue(ButtonBlock.FACING, side);
        blockState(leverPos, state);
        return leverPos;
    }

    default void block(BlockPos pos, BlockDefinition<?> block) {
        block(posToBb(pos), block);
    }

    default void block(String bb, BlockDefinition<?> block) {
        blockState(bb, block.block().defaultBlockState());
    }

    default <T extends AEBaseBlockEntity> void blockEntity(BlockPos pos,
            BlockDefinition<? extends AEBaseEntityBlock<T>> block,
            Consumer<T> postProcessor) {
        blockEntity(posToBb(pos), block, postProcessor);
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

    default void chest(BlockPos pos, ItemStack... stacks) {
        chest(posToBb(pos), stacks);
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

    default <T extends BlockEntity> void customizeBlockEntity(BlockPos pos, BlockEntityType<T> type,
            Consumer<T> consumer) {
        customizeBlockEntity(posToBb(pos), type, consumer);
    }

    /**
     * Place the default state of a block.
     */
    default void block(BlockPos pos, Block block) {
        block(posToBb(pos), block);
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
    default void blockState(BlockPos pos, BlockState blockState) {
        blockState(posToBb(pos), blockState);
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

    default PlotBuilder offset(BlockPos pos) {
        return offset(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Runs a given callback once the grid has been initialized at all viable nodes in the given bounding box.
     */
    default void afterGridInitAt(String bb, BiConsumer<IGrid, IGridNode> consumer) {
        addBuildAction(new PostGridInitAction(bb(bb), consumer, true));
    }

    /**
     * Runs a given callback once the grid is available at all viable nodes in the given bounding box.
     */
    default void afterGridExistsAt(String bb, BiConsumer<IGrid, IGridNode> consumer) {
        addBuildAction(new PostGridInitAction(bb(bb), consumer, false));
    }

    /**
     * Creates a drive with an empty item and fluid cell.
     */
    default void storageDrive(BlockPos pos) {
        blockEntity(posToBb(pos), AEBlocks.DRIVE, drive -> {
            var cells = drive.getInternalInventory();
            cells.addItems(AEItems.ITEM_CELL_64K.stack());
            cells.addItems(AEItems.FLUID_CELL_64K.stack());
        });
    }

    /**
     * Creates a drive with an empty item and fluid cell.
     */
    default DriveBuilder drive(BlockPos pos) {
        var cells = new ArrayList<ItemStack>(10);

        blockEntity(posToBb(pos), AEBlocks.DRIVE, drive -> {
            var cellInv = drive.getInternalInventory();
            for (ItemStack cell : cells) {
                cellInv.addItems(cell);
            }
        });

        return new DriveBuilder(cells);
    }

    Test test(Consumer<PlotTestHelper> assertion);

    default void fencedEntity(BlockPos pos, EntityType<?> entity) {
        fencedEntity(pos, entity, e -> {
        });
    }

    default void fencedEntity(BlockPos pos, EntityType<?> entity, Consumer<Entity> postProcessor) {
        var subPlot = offset(pos.getX(), pos.getY(), pos.getZ());
        subPlot.block("[-1,1] -1 [-1,1]", Blocks.STONE);
        subPlot.block("[-1,1] 0 [-1,1]", Blocks.STONE_BRICK_WALL);
        subPlot.block("0 0 0", Blocks.AIR);

        addBuildAction(new SpawnEntityAction(bb(posToBb(pos)), entity, postProcessor));
    }
}
