package appeng.core.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public interface ServerboundPacket extends CustomAppEngPayload {
    default void handleOnServer(PlayPayloadContext context) {
        context.workHandler().execute(() -> {
            if (context.player().orElse(null) instanceof ServerPlayer serverPlayer) {
                handleOnServer(serverPlayer);
            }
        });
    }

    void handleOnServer(ServerPlayer player);
}
