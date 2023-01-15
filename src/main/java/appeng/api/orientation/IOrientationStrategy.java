package appeng.api.orientation;

import java.util.Collection;
import java.util.stream.Stream;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import appeng.block.orientation.SpinMapping;

/**
 * Specifies how a block determines its orientation and stores it in the blockstate. For use with
 * {@link IOrientableBlock}.
 */
public interface IOrientationStrategy {
    // This is the clockwise rotation around the facing, starting at:
    // UP for horizontal axes
    // NORTH for vertical axes
    IntegerProperty SPIN = IntegerProperty.create("spin", 0, 3);

    static IOrientationStrategy get(BlockState state) {
        if (state.getBlock() instanceof IOrientableBlock orientableBlock) {
            return orientableBlock.getOrientationStrategy();
        }
        return OrientationStrategies.none();
    }

    default Direction getFacing(BlockState state) {
        return Direction.NORTH;
    }

    default int getSpin(BlockState state) {
        return 0;
    }

    default BlockState setFacing(BlockState state, Direction facing) {
        return state;
    }

    default BlockState setSpin(BlockState state, int spin) {
        return state;
    }

    default BlockState setUp(BlockState state, Direction up) {
        var facing = getFacing(state);
        var spin = SpinMapping.getSpinFromUp(facing, up);
        return setSpin(state, spin);
    }

    default BlockState setOrientation(BlockState state, Direction facing, int spin) {
        return setSpin(setFacing(state, facing), spin);
    }

    default BlockState setOrientation(BlockState state, Direction facing, Direction up) {
        return setUp(setFacing(state, facing), up);
    }

    default Direction getSide(BlockState state, RelativeSide side) {
        return BlockOrientation.get(this, state).rotate(side.getUnrotatedSide());
    }

    default BlockState getStateForPlacement(BlockState state, BlockPlaceContext context) {
        return state;
    }

    default Stream<BlockState> getAllStates(BlockState baseState) {
        var result = Stream.of(baseState);
        for (var property : getProperties()) {
            result = enumerateValues(result, property);
        }
        return result;
    }

    /**
     * @return The block state properties used for storing orientation by this strategy.
     */
    Collection<Property<?>> getProperties();

    private static <T extends Comparable<T>> Stream<BlockState> enumerateValues(Stream<BlockState> stream,
            Property<T> property) {
        return stream.flatMap(
                baseState -> property.getPossibleValues().stream().map(value -> baseState.setValue(property, value)));
    }
}
