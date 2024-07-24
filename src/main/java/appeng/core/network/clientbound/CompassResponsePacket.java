
package appeng.core.network.clientbound;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.hooks.CompassManager;

public record CompassResponsePacket(ChunkPos requestedPos,
        Optional<BlockPos> closestMeteorite) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, CompassResponsePacket> STREAM_CODEC = StreamCodec
            .composite(
                    NeoForgeStreamCodecs.CHUNK_POS, CompassResponsePacket::requestedPos,
                    ByteBufCodecs.optional(BlockPos.STREAM_CODEC), CompassResponsePacket::closestMeteorite,
                    CompassResponsePacket::new);

    public static final Type<CompassResponsePacket> TYPE = CustomAppEngPayload.createType("compass_response");

    @Override
    public Type<CompassResponsePacket> type() {
        return TYPE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        CompassManager.INSTANCE.postResult(requestedPos, closestMeteorite.orElse(null));
    }
}
