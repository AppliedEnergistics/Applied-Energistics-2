package appeng.core.sync.packets;

import appeng.client.gui.me.NetworkStatusScreen;
import appeng.container.me.NetworkStatus;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

public class NetworkStatusPacket extends BasePacket {

    private final NetworkStatus status;

    public NetworkStatusPacket(PacketBuffer data) {
        this.status = new NetworkStatus();
        this.status.read(data);
    }

    public NetworkStatusPacket(NetworkStatus status) {
        this.status = null;

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        status.write(data);
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().currentScreen;

        if (gs instanceof NetworkStatusScreen) {
            ((NetworkStatusScreen) gs).postUpdate(status);
        }
    }

}
