package appeng.spatial;

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

    private static final int FORMAT_VERSION = 1;
    private static final int OFFSET_VERSION = 0;
    private static final int OFFSET_CONTENT_SIZE = 1;

    private SpatialDimensionExtraData() {
    }

    public static PacketBuffer create(BlockPos contentSize) {
        PacketBuffer extraData = new PacketBuffer(Unpooled.buffer());
        extraData.writeByte(FORMAT_VERSION);
        extraData.writeBlockPos(contentSize);
        // Cut the buffer to minimal size
        extraData.capacity(extraData.writerIndex());
        return extraData;
    }

    private static boolean checkVersion(PacketBuffer data){
        return data.getByte(OFFSET_VERSION) == FORMAT_VERSION;
    }

    public static BlockPos getContentSize(@Nullable PacketBuffer data) {
        if (data == null || !checkVersion(data)) {
            return BlockPos.ZERO;
        }

        data.readerIndex(OFFSET_CONTENT_SIZE);
        return data.readBlockPos();
    }
}
