
package appeng.core.network.serverbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.clientbound.CompassResponsePacket;
import appeng.server.services.compass.ServerCompassService;

public record RequestClosestMeteoritePacket(ChunkPos pos) implements ServerboundPacket {
    private static final Logger LOG = LoggerFactory.getLogger(RequestClosestMeteoritePacket.class);

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestClosestMeteoritePacket> STREAM_CODEC = StreamCodec
            .composite(
                    NeoForgeStreamCodecs.CHUNK_POS, RequestClosestMeteoritePacket::pos,
                    RequestClosestMeteoritePacket::new);

    public static final Type<RequestClosestMeteoritePacket> TYPE = CustomAppEngPayload.createType("compass_request");

    @Override
    public Type<RequestClosestMeteoritePacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        var result = ServerCompassService.getClosestMeteorite(player.serverLevel(), pos);
        LOG.trace("{} requested closest meteorite for {} in {} -> {}", player, pos, player.serverLevel(), result);
        player.connection.send(new CompassResponsePacket(pos, result));
    }
}
