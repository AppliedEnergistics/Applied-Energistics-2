package appeng.core.network.serverbound;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.me.common.MEStorageMenu;

public record PullItemToPlayerPacket(int containerId, NonNullList<ItemStack> stacks, long toPull) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, PullItemToPlayerPacket> STREAM_CODEC = StreamCodec.ofMember(
            PullItemToPlayerPacket::write,
            PullItemToPlayerPacket::decode);

    public static final Type<PullItemToPlayerPacket> TYPE = CustomAppEngPayload.createType("me_pull");

    @Override
    public Type<PullItemToPlayerPacket> type() {
        return TYPE;
    }

    public static PullItemToPlayerPacket decode(RegistryFriendlyByteBuf buffer) {
        int containerId = buffer.readInt();
        NonNullList<ItemStack> stacks = NonNullList.withSize(buffer.readInt(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
        }
        long toPull = buffer.readVarLong();
        return new PullItemToPlayerPacket(containerId, stacks, toPull);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeInt(containerId);
        data.writeInt(stacks.size());
        for (ItemStack stack : stacks) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(data, stack);
        }
        data.writeVarLong(toPull);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof MEStorageMenu aeMenu) {
            // The open screen has changed since the client sent the packet
            if (player.containerMenu.containerId != containerId) {
                return;
            }

            aeMenu.transferMatchingStacksToPlayer(stacks, toPull);
        }
    }

}
