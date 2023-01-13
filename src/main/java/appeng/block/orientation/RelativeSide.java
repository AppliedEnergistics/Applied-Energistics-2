package appeng.block.orientation;

import net.minecraft.core.Direction;

public enum RelativeSide {
    FRONT(Direction.NORTH),
    BACK(Direction.SOUTH),
    TOP(Direction.UP),
    BOTTOM(Direction.DOWN),
    LEFT(Direction.WEST),
    RIGHT(Direction.EAST);

    private static final RelativeSide[] BY_UNROTATED_SIDE = new RelativeSide[Direction.values().length];

    static {
        for (var side : values()) {
            BY_UNROTATED_SIDE[side.unrotatedSide.ordinal()] = side;
        }
    }

    private final Direction unrotatedSide;

    RelativeSide(Direction unrotatedSide) {
        this.unrotatedSide = unrotatedSide;
    }

    /**
     * Find the relative side on the given absolute side of a block, assuming its default orientation.
     */
    public static RelativeSide fromUnrotatedSide(Direction side) {
        return BY_UNROTATED_SIDE[side.ordinal()];
    }

    public Direction getUnrotatedSide() {
        return unrotatedSide;
    }
}
