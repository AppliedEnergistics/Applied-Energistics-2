package appeng.core.worlddata;

import appeng.spatial.SpatialDimensionExtraData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpatialDimensionExtraDataTest {

    /**
     * The returned buffer should have just the right capacity.
     */
    @Test
    public void testCreatedBufferSize() {
        PacketBuffer buffer = SpatialDimensionExtraData.create(BlockPos.ZERO);
        assertEquals(9, buffer.array().length);
    }

    @Test
    public void testReadWriteSize() {
        BlockPos pos = new BlockPos(1, 2, 3);
        PacketBuffer buffer = SpatialDimensionExtraData.create(pos);

        PacketBuffer readBackBuf = new PacketBuffer(Unpooled.wrappedBuffer(buffer.array()));
        BlockPos actualPos = SpatialDimensionExtraData.getContentSize(readBackBuf);
        assertEquals(pos, actualPos);
    }

    /**
     * Gracefully handle format version errors.
     */
    @Test
    public void testHandleInvalidFormatVersion() {
        BlockPos pos = new BlockPos(1, 2, 3);
        PacketBuffer buffer = SpatialDimensionExtraData.create(pos);
        buffer.writerIndex(0);
        buffer.writeByte(5);

        PacketBuffer readBackBuf = new PacketBuffer(Unpooled.wrappedBuffer(buffer.array()));
        BlockPos actualPos = SpatialDimensionExtraData.getContentSize(readBackBuf);
        assertEquals(BlockPos.ZERO, actualPos);
    }

}