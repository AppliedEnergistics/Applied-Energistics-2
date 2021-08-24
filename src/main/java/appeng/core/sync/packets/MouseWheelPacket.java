package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IMouseWheelItem;

public class MouseWheelPacket extends BasePacket {
    private boolean wheelUp;

    public MouseWheelPacket(FriendlyByteBuf byteBuf) {
        wheelUp = byteBuf.readBoolean();
    }

    public MouseWheelPacket(boolean wheelUp) {
        var data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeBoolean(wheelUp);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, ServerPlayer player) {
        var mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        var offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        if (mainHand.getItem() instanceof IMouseWheelItem mouseWheelItem) {
            mouseWheelItem.onWheel(mainHand, wheelUp);
        } else if (offHand.getItem() instanceof IMouseWheelItem mouseWheelItem) {
            mouseWheelItem.onWheel(offHand, wheelUp);
        }
    }
}
