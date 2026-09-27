
package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;

/**
 * This packet is used for triggering generic menu-specific GUI actions.
 */
public record GuiActionPacket(int containerId, String actionName, byte[] argumentPayload) implements ServerboundPacket {

    public static final int MAX_ARGUMENT_PAYLOAD = 32767;

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiActionPacket> STREAM_CODEC = StreamCodec.ofMember(
            GuiActionPacket::write,
            GuiActionPacket::decode);

    public static final Type<GuiActionPacket> TYPE = CustomAppEngPayload.createType("gui_action");

    public GuiActionPacket {
        // We do not allow for longer payloads than 32kb
        if (argumentPayload != null && argumentPayload.length > MAX_ARGUMENT_PAYLOAD) {
            throw new IllegalArgumentException(
                    "Cannot send client action " + actionName + " because serialized argument is longer than "
                            + MAX_ARGUMENT_PAYLOAD + " (" + argumentPayload.length + ")");
        }
    }

    @Override
    public Type<GuiActionPacket> type() {
        return TYPE;
    }

    public static GuiActionPacket decode(RegistryFriendlyByteBuf data) {
        var containerId = data.readVarInt();
        var actionName = data.readUtf();
        var argumentPayload = data.readByteArray(MAX_ARGUMENT_PAYLOAD);
        return new GuiActionPacket(containerId, actionName, argumentPayload);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarInt(containerId);
        data.writeUtf(actionName);
        data.writeByteArray(argumentPayload);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu baseMenu && c.containerId == this.containerId) {
            baseMenu.receiveClientAction(actionName, argumentPayload);
        }
    }

}
