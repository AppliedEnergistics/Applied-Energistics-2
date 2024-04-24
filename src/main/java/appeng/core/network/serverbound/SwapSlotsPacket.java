
package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;

public record SwapSlotsPacket(int slotA, int slotB) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, SwapSlotsPacket> STREAM_CODEC = StreamCodec.ofMember(
            SwapSlotsPacket::write,
            SwapSlotsPacket::decode);

    public static final Type<SwapSlotsPacket> TYPE = CustomAppEngPayload.createType("swap_slots");

    @Override
    public Type<SwapSlotsPacket> type() {
        return TYPE;
    }

    public static SwapSlotsPacket decode(RegistryFriendlyByteBuf stream) {
        var slotA = stream.readInt();
        var slotB = stream.readInt();
        return new SwapSlotsPacket(slotA, slotB);
    }

    public void write(RegistryFriendlyByteBuf data) {
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
