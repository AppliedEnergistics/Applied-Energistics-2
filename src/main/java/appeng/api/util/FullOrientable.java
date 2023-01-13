package appeng.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

/**
 * Models a block that can be facing in all directions and can also be rotated around that facing axis in 90°
 * increments.
 */
public class FullOrientable {
//
//    public static int convertToSwivel(Direction facing, Direction up) {
//        var swivelDirections = SPIN_DIRECTIONS[facing.ordinal()];
//        for (int i = 0; i < swivelDirections.length; i++) {
//            if (swivelDirections[i] == up) {
//                return i;
//            }
//        }
//        // Map denormalized rotations to the un-swiveled up
//        return 0;
//    }
//
//    public static Direction convertFromSwivel(Direction facing, int swivel) {
//        return SPIN_DIRECTIONS[facing.ordinal()][swivel];
//    }

    /**
     * Rotates around the given Axis (usually the current up axis).
     */
    public boolean rotateAroundFaceAxis(LevelAccessor level, BlockPos pos, Direction face) {
//        final IOrientable rotatable = this.getOrientable(level, pos);

//        if (rotatable != null && rotatable.canBeRotated()) {
//            if (this.hasCustomRotation()) {
//                this.customRotateBlock(rotatable, face);
//                return true;
//            } else {
//                Direction forward = rotatable.getForward();
//                Direction up = rotatable.getUp();

//                for (int rs = 0; rs < 4; rs++) {
//                    forward = Platform.rotateAround(forward, face);
//                    up = Platform.rotateAround(up, face);

//                    if (this.isValidOrientation(level, pos, forward, up)) {
//                        rotatable.setOrientation(forward, up);
//                        return true;
//                    }
//                }
//            }
//        }

        return false;
    }

}
