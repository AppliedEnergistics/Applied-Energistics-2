
package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.me.crafting.CraftAmountMenu;

public record ConfirmAutoCraftPacket(int amount,
        boolean craftMissingAmount,
        boolean autoStart) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmAutoCraftPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    ConfirmAutoCraftPacket::write,
                    ConfirmAutoCraftPacket::decode);

    public static final Type<ConfirmAutoCraftPacket> TYPE = CustomAppEngPayload.createType("confirm_auto_craft");

    @Override
    public Type<ConfirmAutoCraftPacket> type() {
        return TYPE;
    }

    public static ConfirmAutoCraftPacket decode(RegistryFriendlyByteBuf stream) {
        var amount = stream.readInt();
        var craftMissingAmount = stream.readBoolean();
        var autoStart = stream.readBoolean();
        return new ConfirmAutoCraftPacket(amount, craftMissingAmount, autoStart);
    }

    public void write(RegistryFriendlyByteBuf data) {
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
