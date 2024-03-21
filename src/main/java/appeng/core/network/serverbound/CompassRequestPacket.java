
package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.clientbound.CompassResponsePacket;
import appeng.hooks.CompassResult;
import appeng.server.services.compass.CompassService;

public record CompassRequestPacket(long attunement,
        int cx,
        int cz,
        int cdy) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, CompassRequestPacket> STREAM_CODEC = StreamCodec.ofMember(
            CompassRequestPacket::write,
            CompassRequestPacket::decode);

    public static final Type<CompassRequestPacket> TYPE = CustomAppEngPayload.createType("compass_request");

    @Override
    public Type<CompassRequestPacket> type() {
        return TYPE;
    }

    public static CompassRequestPacket decode(RegistryFriendlyByteBuf stream) {
        var attunement = stream.readLong();
        var cx = stream.readInt();
        var cz = stream.readInt();
        var cdy = stream.readInt();
        return new CompassRequestPacket(attunement, cx, cz, cdy);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeLong(this.attunement);
        data.writeInt(this.cx);
        data.writeInt(this.cz);
        data.writeInt(this.cdy);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        var pos = new ChunkPos(this.cx, this.cz);
        var result = CompassService.getDirection(player.serverLevel(), pos, 174);

        var responsePacket = new CompassResponsePacket(attunement, cx, cz, cdy, new CompassResult(
                result.hasResult(), result.spin(), result.radians()));
        player.connection.send(responsePacket);
    }
}
