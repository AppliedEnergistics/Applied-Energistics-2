package appeng.core.sync.packets;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.sync.BasePacket;
import appeng.menu.implementations.PatternAccessTermMenu;

/**
 * Used for the pattern access terminal when the client shift-clicks an item in the player inventory.
 */
public class QuickMovePatternPacket extends BasePacket {
    private int containerId;
    private int clickedSlot;
    private List<Long> allowedPatternContainers;

    public QuickMovePatternPacket(FriendlyByteBuf stream) {
        this.containerId = stream.readVarInt();
        this.clickedSlot = stream.readVarInt();
        int count = stream.readVarInt();
        this.allowedPatternContainers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.allowedPatternContainers.add(stream.readVarLong());
        }
    }

    public QuickMovePatternPacket(int containerId, int clickedSlot, List<Long> allowedPatternContainers) {
        this.containerId = containerId;
        this.clickedSlot = clickedSlot;
        this.allowedPatternContainers = allowedPatternContainers;

        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeVarInt(containerId);
        data.writeVarInt(clickedSlot);
        data.writeVarInt(allowedPatternContainers.size());
        allowedPatternContainers.forEach(data::writeVarLong);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(ServerPlayer player) {
        if (player.containerMenu.containerId == containerId
                && player.containerMenu instanceof PatternAccessTermMenu menu) {
            menu.quickMovePattern(player, clickedSlot, allowedPatternContainers);
        }
    }
}
