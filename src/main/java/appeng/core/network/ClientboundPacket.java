package appeng.core.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public interface ClientboundPacket extends CustomAppEngPayload {
    default void handleOnClient(PlayPayloadContext context) {
        context.workHandler().execute(() -> {
            context.player().ifPresent(this::handleOnClient);
        });
    }

    default void handleOnClient(Player player) {
        throw new AbstractMethodError("Unimplemented method on " + getClass());
    }
}
