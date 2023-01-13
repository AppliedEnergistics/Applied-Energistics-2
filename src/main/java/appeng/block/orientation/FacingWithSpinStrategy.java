package appeng.block.orientation;

import java.util.Collection;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Extends {@link FacingStrategy} to also allow the block to be rotated around its facing axis.
 */
class FacingWithSpinStrategy implements IOrientationStrategy {

    private final List<Property<?>> properties;

    public FacingWithSpinStrategy() {
        this.properties = List.of(
                BlockStateProperties.FACING,
                SPIN);
    }

    @Override
    public Direction getFacing(BlockState state) {
        return state.getValue(BlockStateProperties.FACING);
    }

    @Override
    public int getSpin(BlockState state) {
        return state.getValue(SPIN);
    }

    @Override
    public BlockState setFacing(BlockState state, Direction facing) {
        return state.setValue(BlockStateProperties.FACING, facing);
    }

    @Override
    public BlockState setSpin(BlockState state, int spin) {
        return state.setValue(SPIN, spin);
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, BlockPlaceContext context) {
        var up = Direction.UP;
        var forward = context.getHorizontalDirection().getOpposite();
        var player = context.getPlayer();
        if (player != null) {
            if (player.getXRot() > 65) {
                up = forward.getOpposite();
                forward = Direction.UP;
            } else if (player.getXRot() < -65) {
                up = forward.getOpposite();
                forward = Direction.DOWN;
            }
        }

        return setOrientation(state, forward, up);
    }

    @Override
    public Collection<Property<?>> getProperties() {
        return properties;
    }
}
