package appeng.block.orientation;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class OrientationStrategies {
    private static final IOrientationStrategy none = new NoOrientationStrategy();
    private static final IOrientationStrategy horizontalFacing = new FacingStrategy(
            BlockStateProperties.HORIZONTAL_FACING, false);
    private static final IOrientationStrategy facing = new FacingStrategy(BlockStateProperties.FACING, false);
    private static final IOrientationStrategy facingAttached = new FacingStrategy(BlockStateProperties.FACING, true);
    private static final IOrientationStrategy full = new FacingWithSpinStrategy();

    /**
     * The blocks orientation cannot be changed.
     */
    public static IOrientationStrategy none() {
        return none;
    }

    public static IOrientationStrategy horizontalFacing() {
        return horizontalFacing;
    }

    /**
     * Block can be oriented in 6 directions, but not swivel around that axis. Semantically the block is attached to the
     * face of another block, but the facing points away from the side it is attached to. This influences how the
     * default placement is deduced.
     */
    public static IOrientationStrategy facingAttached(boolean allowRotation) {
        return facingAttached;
    }

    public static IOrientationStrategy facingAttached() {
        return facingAttached(true);
    }

    /**
     * Block can be oriented in 6 directions, but not swivel around that axis.
     */
    public static IOrientationStrategy facing() {
        return facing;
    }

    /**
     * Block can be oriented in 6 directions and then can also be swiveled around that axis in 90° increments.
     */
    public static IOrientationStrategy full() {
        return full;
    }
}
