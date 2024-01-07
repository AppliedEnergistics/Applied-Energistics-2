
package appeng.core.network.serverbound;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;

/**
 * This packet is used for triggering generic menu-specific GUI actions.
 */
public record GuiActionPacket(int containerId, String actionName,
        @Nullable String jsonPayload) implements ServerboundPacket {
    public static GuiActionPacket decode(FriendlyByteBuf data) {
        var containerId = data.readVarInt();
        var actionName = data.readUtf();
        var jsonPayload = data.readOptional(FriendlyByteBuf::readUtf).orElse(null);
        return new GuiActionPacket(containerId, actionName, jsonPayload);
    }

    @Override
    public void write(FriendlyByteBuf data) {
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
