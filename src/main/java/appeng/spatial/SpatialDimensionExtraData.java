package appeng.spatial;

import appeng.core.AELog;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Helps with encoding and decoding the extra data we attach to the
 * spatial {@link net.minecraft.world.dimension.DimensionType} as "extra data".
 * Keep in mind this data will also be sent to the client unless
 * {@link net.minecraftforge.common.ModDimension#write(PacketBuffer, boolean)} is overridden.
 */
public final class SpatialDimensionExtraData {

    // Used to allow forward compatibility
    private static final int CURRENT_FORMAT = 1;

    /**
     * The storage size of this dimension. This is dicateted by the pylon structure size used to perform
     * the first transfer into this dimension. Once it's set, it cannot be changed anymore.
     */
    private final BlockPos size;

    public SpatialDimensionExtraData(BlockPos size) {
        this.size = size;
    }

    public PacketBuffer write() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeByte(CURRENT_FORMAT);
        buf.writeBlockPos(size);
        buf.capacity(buf.writerIndex()); // This cuts the backing buffer to the required size
        return buf;
    }

    public BlockPos getSize() {
        return size;
    }

    @Nullable
    public static SpatialDimensionExtraData read(@Nullable PacketBuffer buf) {
        if (buf == null) {
            return null;
        }

        try {
            buf.readerIndex(0);
            byte version = buf.readByte();
            if (version != CURRENT_FORMAT) {
                // Currently no new format has been defined, as such anything but the current version is invalid
                return null;
            }

            BlockPos size = buf.readBlockPos();
            return new SpatialDimensionExtraData(size);
        } catch (IndexOutOfBoundsException e) {
            AELog.warn(e, "Failed to read spatial storage dimension data.");
            return null;
        }
    }

}
