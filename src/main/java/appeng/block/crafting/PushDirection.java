package appeng.block.crafting;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

/**
 * Extends {@link net.minecraft.core.Direction} with an 'all' key.
 */
public enum PushDirection implements StringRepresentable {
    DOWN(Direction.DOWN),
    UP(Direction.UP),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    EAST(Direction.EAST),
    ALL;

    @org.jetbrains.annotations.Nullable
    private final Direction direction;

    PushDirection(Direction direction) {
        this.direction = direction;
    }

    PushDirection() {
        this.direction = null;
    }

    @org.jetbrains.annotations.Nullable
    public Direction getDirection() {
        return direction;
    }

    @Override
    public String getSerializedName() {
        return direction != null ? direction.getSerializedName() : "all";
    }

    public static PushDirection fromDirection(@Nullable Direction direction) {
        return direction != null ? values()[direction.ordinal()] : ALL;
    }
}
