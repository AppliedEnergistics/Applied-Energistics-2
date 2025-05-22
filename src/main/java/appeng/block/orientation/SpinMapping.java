package appeng.block.orientation;

import net.minecraft.core.Direction;

/**
 * Defines the final rotation around the axis the block is facing. (a.k.a. spin).
 */
public final class SpinMapping {
    private SpinMapping() {
    }

    // When looking at the block from the given direction
    // the list contains the rotation in clockwise orientation
    // For UP/DOWN we consider NORTH to be up
    private static final Direction[][] SPIN_DIRECTIONS = new Direction[][] {
            // DOWN
            { Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST },
            // UP
            { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST },
            // NORTH
            { Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST },
            // SOUTH
            { Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST },
            // WEST
            { Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH },
            // EAST
            { Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH },
    };

    public static int getSpinFromUp(Direction facing, Direction up) {
        var spinDirs = SPIN_DIRECTIONS[facing.ordinal()];
        for (int i = 0; i < spinDirs.length; i++) {
            if (spinDirs[i] == up) {
                return i;
            }
        }
        return 0; // Degenerated up direction just falls back to no spin
    }

    public static int getUpFromSpin(Direction facing, Direction up) {
        var spinDirs = SPIN_DIRECTIONS[facing.ordinal()];
        for (int i = 0; i < spinDirs.length; i++) {
            if (spinDirs[i] == up) {
                return i;
            }
        }
        return 0; // Degenerated up direction just falls back to no spin
    }
}
