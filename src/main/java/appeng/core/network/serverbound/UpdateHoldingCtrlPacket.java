package appeng.core.network.serverbound;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;

public record UpdateHoldingCtrlPacket(boolean keyDown) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHoldingCtrlPacket> STREAM_CODEC = StreamCodec
            .ofMember(UpdateHoldingCtrlPacket::write, UpdateHoldingCtrlPacket::decode);

    public static final Type<UpdateHoldingCtrlPacket> TYPE = CustomAppEngPayload.createType("toggle_ctrl_down");

    @NotNull
    @Override
    public Type<UpdateHoldingCtrlPacket> type() {
        return TYPE;
    }

    public static UpdateHoldingCtrlPacket decode(RegistryFriendlyByteBuf buf) {
        var keyDown = buf.readBoolean();
        return new UpdateHoldingCtrlPacket(keyDown);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBoolean(keyDown);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        player.setData(AEAttachmentTypes.HOLDING_CTRL, keyDown);
    }
}
