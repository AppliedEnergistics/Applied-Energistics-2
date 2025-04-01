package appeng.client;

import appeng.core.network.ClientboundPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.IdentityHashMap;
import java.util.Map;

public final class PakcetHandlerMap {
    private final Map<CustomPacketPayload.Type<?>, ClientPacketHandler<?>> handlers = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends ClientboundPacket> ClientPacketHandler<T> get(CustomPacketPayload.Type<T> type) {
        return (ClientPacketHandler<T>) handlers.get(type);
    }

    public <T extends ClientboundPacket> void register(CustomPacketPayload.Type<T> type, ClientPacketHandler<T> handler) {
        if (handlers.put(type, handler) != null) {
            throw new IllegalStateException("Duplicate clientside packet handler for packet type: " + type);
        }
    }

    @FunctionalInterface
    public interface ClientPacketHandler<T extends ClientboundPacket> {
        void handle(T payload, Minecraft minecraft, Player player);
    }
}
