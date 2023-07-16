package appeng.api.orientation;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Implements a strategy that allows blocks to be oriented using a single directional property. It doesn't allow up and
 * down, and uses the player facing instead in those cases.
 */
class HorizontalFacingStrategy extends FacingStrategy {
    public HorizontalFacingStrategy() {
        super(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, BlockPlaceContext context) {
        return setFacing(state, context.getHorizontalDirection().getOpposite());
    }
}
