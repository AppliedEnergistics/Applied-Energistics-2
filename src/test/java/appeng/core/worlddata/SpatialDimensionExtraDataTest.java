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
        PacketBuffer buffer = new SpatialDimensionExtraData(BlockPos.ZERO).write();
        assertEquals(1 + 8 + 8, buffer.array().length);
    }

    @Test
    public void testReadWriteSize() {
        BlockPos capacity = new BlockPos(1, 2, 3);
        PacketBuffer buffer = new SpatialDimensionExtraData(capacity).write();

        PacketBuffer readBackBuf = new PacketBuffer(Unpooled.wrappedBuffer(buffer.array()));
        SpatialDimensionExtraData extraData = SpatialDimensionExtraData.read(readBackBuf);
        assertNotNull(extraData);
        assertEquals(capacity, extraData.getSize());
    }

    /**
     * Gracefully handle format version errors.
     */
    @Test
    public void testHandleInvalidFormatVersion() {
        PacketBuffer buffer = new SpatialDimensionExtraData(BlockPos.ZERO).write();
        buffer.writerIndex(0);
        buffer.writeByte(5);

        PacketBuffer readBackBuf = new PacketBuffer(Unpooled.wrappedBuffer(buffer.array()));
        assertNull(SpatialDimensionExtraData.read(readBackBuf));
    }

}