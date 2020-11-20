package appeng.spatial;

import java.time.Instant;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Defines the source world and area of a transition into the spatial storage plot.
 */
public final class TransitionInfo {

    public static final String TAG_WORLD_ID = "world_id";
    public static final String TAG_MIN = "min";
    public static final String TAG_MAX = "max";
    public static final String TAG_TIMESTAMP = "timestamp";

    private final Identifier worldId;

    private final BlockPos min;

    private final BlockPos max;

    private final Instant timestamp;

    public TransitionInfo(Identifier worldId, BlockPos min, BlockPos max, Instant timestamp) {
        this.worldId = worldId;
        this.min = min.toImmutable();
        this.max = max.toImmutable();
        this.timestamp = timestamp;
    }

    public Identifier getWorldId() {
        return worldId;
    }

    public BlockPos getMin() {
        return min;
    }

    public BlockPos getMax() {
        return max;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_WORLD_ID, worldId.toString());
        tag.put(TAG_MIN, NbtHelper.fromBlockPos(min));
        tag.put(TAG_MAX, NbtHelper.fromBlockPos(max));
        tag.putLong(TAG_TIMESTAMP, timestamp.toEpochMilli());
        return tag;
    }

    public static TransitionInfo fromTag(CompoundTag tag) {
        Identifier worldId = new Identifier(tag.getString(TAG_WORLD_ID));
        BlockPos min = NbtHelper.toBlockPos(tag.getCompound(TAG_MIN));
        BlockPos max = NbtHelper.toBlockPos(tag.getCompound(TAG_MAX));
        Instant timestamp = Instant.ofEpochMilli(tag.getLong(TAG_TIMESTAMP));
        return new TransitionInfo(worldId, min, max, timestamp);
    }

}
