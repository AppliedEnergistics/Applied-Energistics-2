package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

import appeng.container.me.common.IMEInteractionHandler;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.InventoryAction;

/**
 * Packet sent by clients to interact with an ME inventory such as an item terminal.
 */
public class MEInteractionPacket extends BasePacket {

    private final int windowId;
    private final long serial;
    private final InventoryAction action;

    public MEInteractionPacket(PacketBuffer buffer) {
        this.windowId = buffer.readInt();
        this.serial = buffer.readVarLong();
        this.action = buffer.readEnumValue(InventoryAction.class);
    }

    public MEInteractionPacket(int windowId, long serial, InventoryAction action) {
        this.windowId = windowId;
        this.serial = serial;
        this.action = action;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeInt(windowId);
        data.writeVarLong(serial);
        data.writeEnumValue(action);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, PlayerEntity player) {
        if (player.openContainer instanceof IMEInteractionHandler) {
            // The open screen has changed since the client sent the packet
            if (player.openContainer.windowId != windowId) {
                return;
            }

            IMEInteractionHandler handler = (IMEInteractionHandler) player.openContainer;
            handler.handleInteraction(serial, action);
        }
    }

}
