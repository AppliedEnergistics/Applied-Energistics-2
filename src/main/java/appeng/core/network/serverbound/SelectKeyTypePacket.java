package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.stacks.AEKeyType;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.interfaces.KeyTypeSelectionMenu;

public record SelectKeyTypePacket(AEKeyType keyType, boolean enabled) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectKeyTypePacket> STREAM_CODEC = StreamCodec.ofMember(
            SelectKeyTypePacket::write,
            SelectKeyTypePacket::decode);

    public static final Type<SelectKeyTypePacket> TYPE = CustomAppEngPayload.createType("select_key_type");

    @Override
    public Type<SelectKeyTypePacket> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(keyType.getRawId());
        buf.writeBoolean(enabled);
    }

    public static SelectKeyTypePacket decode(RegistryFriendlyByteBuf buf) {
        return new SelectKeyTypePacket(AEKeyType.fromRawId(buf.readVarInt()), buf.readBoolean());
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof KeyTypeSelectionMenu menu) {
            menu.getServerKeyTypeSelection().setEnabled(keyType, enabled);
        }
    }
}
