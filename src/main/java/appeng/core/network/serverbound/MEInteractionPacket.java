
package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.IMEInteractionHandler;

/**
 * Packet sent by clients to interact with an ME inventory such as an item terminal.
 */
public record MEInteractionPacket(int containerId, long serial, InventoryAction action) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, MEInteractionPacket> STREAM_CODEC = StreamCodec.ofMember(
            MEInteractionPacket::write,
            MEInteractionPacket::decode);

    public static final Type<MEInteractionPacket> TYPE = CustomAppEngPayload.createType("me_interaction");

    @Override
    public Type<MEInteractionPacket> type() {
        return TYPE;
    }

    public static MEInteractionPacket decode(RegistryFriendlyByteBuf buffer) {
        var containerId = buffer.readInt();
        var serial = buffer.readVarLong();
        var action = buffer.readEnum(InventoryAction.class);
        return new MEInteractionPacket(containerId, serial, action);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeInt(containerId);
        data.writeVarLong(serial);
        data.writeEnum(action);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof IMEInteractionHandler handler) {
            // The open screen has changed since the client sent the packet
            if (player.containerMenu.containerId != containerId) {
                return;
            }

            handler.handleInteraction(serial, action);
        }
    }
}
