package appeng.core.sync.packets;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

import appeng.container.AEBaseContainer;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class GuiDataSyncPacket extends BasePacket {
    private final int windowId;

    private final PacketBuffer data;

    public GuiDataSyncPacket(int windowId, Consumer<PacketBuffer> writer) {
        this.windowId = 0;
        this.data = null;

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(getPacketID());
        data.writeVarInt(windowId);
        writer.accept(data);
        configureWrite(data);
    }

    public GuiDataSyncPacket(PacketBuffer data) {
        this.windowId = data.readVarInt();
        this.data = new PacketBuffer(data.copy());
    }

    public PacketBuffer getData() {
        return data;
    }

    @Override
    public void clientPacketData(final INetworkInfo manager, final PlayerEntity player) {
        Container c = player.openContainer;
        if (c instanceof AEBaseContainer && c.windowId == this.windowId) {
            ((AEBaseContainer) c).receiveSyncData(this);
        }
    }

}
