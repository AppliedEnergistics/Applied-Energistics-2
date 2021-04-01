package appeng.core.sync.network;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketDirection;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;

public class ClientNetworkHandler extends ServerNetworkHandler {

    private final ClientSidePacketRegistry registry = ClientSidePacketRegistry.INSTANCE;

    public ClientNetworkHandler() {
        registry.register(BasePacket.CHANNEL, this::handlePacketFromServer);
    }

    @Override
    public void sendToServer(BasePacket message) {
        registry.sendToServer(message.toPacket(PacketDirection.SERVERBOUND));
    }

    private void handlePacketFromServer(PacketContext packetContext, PacketBuffer payload) {
        final int packetType = payload.readInt();
        final BasePacket packet = BasePacketHandler.PacketTypes.getPacket(packetType).parsePacket(payload);

        packetContext.getTaskQueue().execute(() -> {
            try {
                packet.clientPacketData(null, packetContext.getPlayer());
            } catch (final IllegalArgumentException e) {
                AELog.debug(e);
            }
        });
    }

}
