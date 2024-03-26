package appeng.block.crafting;

import com.mojang.serialization.Codec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

/**
 * Extends {@link Direction} with an 'all' key.
 */
public enum PushDirection implements StringRepresentable {
    DOWN(Direction.DOWN),
    UP(Direction.UP),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    EAST(Direction.EAST),
    ALL;

    public static final Codec<PushDirection> CODEC = StringRepresentable.fromEnum(PushDirection::values);

    public static final StreamCodec<FriendlyByteBuf, PushDirection> STREAM_CODEC = NeoForgeStreamCodecs
            .enumCodec(PushDirection.class);

    @Nullable
    private final Direction direction;

    PushDirection(Direction direction) {
        this.direction = direction;
    }

    PushDirection() {
        this.direction = null;
    }

    @Nullable
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
