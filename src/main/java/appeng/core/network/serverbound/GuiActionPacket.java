
package appeng.core.network.serverbound;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
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
public record GuiActionPacket(int containerId, String actionName,
        @Nullable String jsonPayload) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, GuiActionPacket> STREAM_CODEC = StreamCodec.ofMember(
            GuiActionPacket::write,
            GuiActionPacket::decode);

    public static final Type<GuiActionPacket> TYPE = CustomAppEngPayload.createType("gui_action");

    @Override
    public Type<GuiActionPacket> type() {
        return TYPE;
    }

    public static GuiActionPacket decode(RegistryFriendlyByteBuf data) {
        var containerId = data.readVarInt();
        var actionName = data.readUtf();
        var jsonPayload = data.readOptional(FriendlyByteBuf::readUtf).orElse(null);
        return new GuiActionPacket(containerId, actionName, jsonPayload);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarInt(containerId);
        data.writeUtf(actionName);
        data.writeOptional(Optional.ofNullable(jsonPayload), FriendlyByteBuf::writeUtf);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu baseMenu && c.containerId == this.containerId) {
            baseMenu.receiveClientAction(actionName, jsonPayload);
        }
    }

}
