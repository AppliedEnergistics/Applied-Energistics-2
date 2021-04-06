package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.container.me.networktool.NetworkStatus;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class NetworkStatusPacket extends BasePacket {

    private final NetworkStatus status;

    public NetworkStatusPacket(PacketBuffer data) {
        this.status = NetworkStatus.read(data);
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
            ((NetworkStatusScreen) gs).processServerUpdate(status);
        }
    }

}
