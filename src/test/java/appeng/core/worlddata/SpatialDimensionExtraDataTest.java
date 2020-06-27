package appeng.core.worlddata;

import static org.junit.Assert.*;

import net.minecraft.network.PacketByteBuf;
import org.junit.Test;

import io.netty.buffer.Unpooled;

import net.minecraft.util.math.BlockPos;

import appeng.spatial.SpatialDimensionExtraData;

public class SpatialDimensionExtraDataTest {

    /**
     * The returned buffer should have just the right capacity.
     */
    @Test
    public void testCreatedBufferSize() {
        PacketByteBuf buffer = new SpatialDimensionExtraData(BlockPos.ZERO).write();
        assertEquals(1 + 8 + 8, buffer.array().length);
    }

    @Test
    public void testReadWriteSize() {
        BlockPos capacity = new BlockPos(1, 2, 3);
        PacketByteBuf buffer = new SpatialDimensionExtraData(capacity).write();

        PacketByteBuf readBackBuf = new PacketByteBuf(Unpooled.wrappedBuffer(buffer.array()));
        SpatialDimensionExtraData extraData = SpatialDimensionExtraData.read(readBackBuf);
        assertNotNull(extraData);
        assertEquals(capacity, extraData.getSize());
    }

    /**
     * Gracefully handle format version errors.
     */
    @Test
    public void testHandleInvalidFormatVersion() {
        PacketByteBuf buffer = new SpatialDimensionExtraData(BlockPos.ZERO).write();
        buffer.writerIndex(0);
        buffer.writeByte(5);

        PacketByteBuf readBackBuf = new PacketByteBuf(Unpooled.wrappedBuffer(buffer.array()));
        assertNull(SpatialDimensionExtraData.read(readBackBuf));
    }

}