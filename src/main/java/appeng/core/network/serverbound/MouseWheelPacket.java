package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.helpers.IMouseWheelItem;

public record MouseWheelPacket(boolean wheelUp) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, MouseWheelPacket> STREAM_CODEC = StreamCodec.ofMember(
            MouseWheelPacket::write,
            MouseWheelPacket::decode);

    public static final Type<MouseWheelPacket> TYPE = CustomAppEngPayload.createType("mouse_wheel");

    @Override
    public Type<MouseWheelPacket> type() {
        return TYPE;
    }

    public static MouseWheelPacket decode(RegistryFriendlyByteBuf byteBuf) {
        var wheelUp = byteBuf.readBoolean();
        return new MouseWheelPacket(wheelUp);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeBoolean(wheelUp);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        var mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        var offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        if (mainHand.getItem() instanceof IMouseWheelItem mouseWheelItem) {
            mouseWheelItem.onWheel(mainHand, wheelUp);
        } else if (offHand.getItem() instanceof IMouseWheelItem mouseWheelItem) {
            mouseWheelItem.onWheel(offHand, wheelUp);
        }
    }
}
