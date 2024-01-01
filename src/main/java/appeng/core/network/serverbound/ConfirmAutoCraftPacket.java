
package appeng.core.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.network.ServerboundPacket;
import appeng.menu.me.crafting.CraftAmountMenu;

public record ConfirmAutoCraftPacket(int amount,
        boolean craftMissingAmount,
        boolean autoStart) implements ServerboundPacket {

    public static ConfirmAutoCraftPacket decode(FriendlyByteBuf stream) {
        var amount = stream.readInt();
        var craftMissingAmount = stream.readBoolean();
        var autoStart = stream.readBoolean();
        return new ConfirmAutoCraftPacket(amount, craftMissingAmount, autoStart);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeInt(amount);
        data.writeBoolean(craftMissingAmount);
        data.writeBoolean(autoStart);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof CraftAmountMenu menu) {
            menu.confirm(amount, craftMissingAmount, autoStart);
        }
    }
}
