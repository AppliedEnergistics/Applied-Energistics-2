package appeng.core.network;

import java.util.Locale;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import appeng.core.AppEng;

public interface ClientboundPacket extends CustomAppEngPayload {
    @Override
    default ResourceLocation id() {
        return AppEng.makeId(getClass().getSimpleName().toLowerCase(Locale.ROOT));
    }

    default void handleOnClient(PlayPayloadContext context) {
        context.workHandler().execute(() -> {
            context.player().ifPresent(this::handleOnClient);
        });
    }

    default void handleOnClient(Player player) {
        throw new AbstractMethodError("Unimplemented method on " + getClass());
    }
}
