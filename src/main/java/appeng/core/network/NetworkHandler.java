package appeng.core.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.core.AppEng;

public class NetworkHandler {
    private static final NetworkHandler instance = new NetworkHandler();

    public static NetworkHandler instance() {
        return instance;
    }

    public void sendToAll(ClientboundPacket message) {
        PacketDistributor.ALL.noArg().send(message);
    }

    public void sendTo(ClientboundPacket message, ServerPlayer player) {
        player.connection.send(message);
    }

    public void sendToAllAround(ClientboundPacket message, PacketDistributor.TargetPoint point) {
        var server = AppEng.instance().getCurrentServer();
        if (server != null) {
            PacketDistributor.NEAR.with(point).send(message);
        }
    }

    public void sendToServer(ServerboundPacket message) {
        PacketDistributor.SERVER.noArg().send(message);
    }
}
