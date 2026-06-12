
package appeng.core.network.clientbound;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

/**
 * This packet is used to synchronize menu-fields from server to client.
 */
public record GuiDataSyncPacket(int containerId, byte[] syncData) implements ClientboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, GuiDataSyncPacket> STREAM_CODEC = StreamCodec.ofMember(
            GuiDataSyncPacket::write,
            GuiDataSyncPacket::decode);

    public static final Type<GuiDataSyncPacket> TYPE = CustomAppEngPayload.createType("");

    @Override
    public Type<GuiDataSyncPacket> type() {
        return TYPE;
    }

    public GuiDataSyncPacket(int containerId, Consumer<RegistryFriendlyByteBuf> writer, RegistryAccess registryAccess) {
        this(containerId, createSyncData(writer, registryAccess));
    }

    private static byte[] createSyncData(Consumer<RegistryFriendlyByteBuf> writer, RegistryAccess registryAccess) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        writer.accept(buffer);
        var result = new byte[buffer.readableBytes()];
        buffer.readBytes(result);
        return result;
    }

    public static GuiDataSyncPacket decode(RegistryFriendlyByteBuf data) {
        var containerId = data.readVarInt();
        var syncData = data.readByteArray();
        return new GuiDataSyncPacket(containerId, syncData);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarInt(containerId);
        data.writeByteArray(syncData);
    }

}
