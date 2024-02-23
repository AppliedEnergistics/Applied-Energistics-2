package appeng.core.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.stacks.AEKeyType;
import appeng.core.network.ServerboundPacket;
import appeng.menu.interfaces.KeyTypeSelectionMenu;

public record SelectKeyTypePacket(AEKeyType keyType, boolean enabled) implements ServerboundPacket {
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(keyType.getRawId());
        buf.writeBoolean(enabled);
    }

    public static SelectKeyTypePacket decode(FriendlyByteBuf buf) {
        return new SelectKeyTypePacket(AEKeyType.fromRawId(buf.readVarInt()), buf.readBoolean());
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof KeyTypeSelectionMenu menu) {
            menu.getServerKeyTypeSelection().setEnabled(keyType, enabled);
        }
    }
}
