
package appeng.core.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;

public record SwapSlotsPacket(int slotA, int slotB) implements ServerboundPacket {
    public static SwapSlotsPacket decode(FriendlyByteBuf stream) {
        var slotA = stream.readInt();
        var slotB = stream.readInt();
        return new SwapSlotsPacket(slotA, slotB);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeInt(slotA);
        data.writeInt(slotB);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player != null && player.containerMenu instanceof AEBaseMenu) {
            ((AEBaseMenu) player.containerMenu).swapSlotContents(this.slotA, this.slotB);
        }
    }
}
