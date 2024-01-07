package appeng.core.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import appeng.core.network.ServerboundPacket;
import appeng.helpers.IMouseWheelItem;

public record MouseWheelPacket(boolean wheelUp) implements ServerboundPacket {
    public static MouseWheelPacket decode(FriendlyByteBuf byteBuf) {
        var wheelUp = byteBuf.readBoolean();
        return new MouseWheelPacket(wheelUp);
    }

    @Override
    public void write(FriendlyByteBuf data) {
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
