package appeng.spatial;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.time.Instant;

/**
 * Defines the source world and area of a transition into the spatial storage plot.
 */
public final class TransitionInfo {

    private final ResourceLocation worldId;

    private final BlockPos min;

    private final BlockPos max;

    private final Instant timestamp;

    public TransitionInfo(ResourceLocation worldId, BlockPos min, BlockPos max, Instant timestamp) {
        this.worldId = worldId;
        this.min = min.toImmutable();
        this.max = max.toImmutable();
        this.timestamp = timestamp;
    }

    public ResourceLocation getWorldId() {
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

}
