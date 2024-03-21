package appeng.core.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ClientboundPacket extends CustomAppEngPayload {
    default void handleOnClient(IPayloadContext context) {
        context.enqueueWork(() -> {
            handleOnClient(context.player());
        });
    }

    default void handleOnClient(Player player) {
        throw new AbstractMethodError("Unimplemented method on " + getClass());
    }
}
