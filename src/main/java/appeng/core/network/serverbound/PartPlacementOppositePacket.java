package appeng.core.network.serverbound;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;

public record PartPlacementOppositePacket(boolean keyDown) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, PartPlacementOppositePacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    PartPlacementOppositePacket::write,
                    PartPlacementOppositePacket::decode);

    public static final Type<PartPlacementOppositePacket> TYPE = CustomAppEngPayload.createType("ctrl_down");

    @NotNull
    @Override
    public Type<PartPlacementOppositePacket> type() {
        return TYPE;
    }

    public static PartPlacementOppositePacket decode(RegistryFriendlyByteBuf buf) {
        var keyDown = buf.readBoolean();
        return new PartPlacementOppositePacket(keyDown);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBoolean(keyDown);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        player.setData(AEAttachmentTypes.HOLDING_CTRL, keyDown);
    }
}
