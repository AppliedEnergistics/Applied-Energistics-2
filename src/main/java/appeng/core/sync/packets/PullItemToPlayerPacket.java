package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.core.sync.BasePacket;
import appeng.menu.me.common.MEStorageMenu;

public class PullItemToPlayerPacket extends BasePacket {

    private int containerId;
    private NonNullList<ItemStack> stacks;
    private long toPull;

    public PullItemToPlayerPacket(FriendlyByteBuf stream) {
        containerId = stream.readInt();
        stacks = NonNullList.withSize(stream.readInt(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, stream.readItem());
        }
        toPull = stream.readVarLong();
    }

    public PullItemToPlayerPacket(int containerId, NonNullList<ItemStack> stacks, long toPull) {
        var data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());

        data.writeInt(containerId);
        data.writeInt(stacks.size());
        for (ItemStack stack : stacks) {
            data.writeItem(stack);
        }
        data.writeVarLong(toPull);

        configureWrite(data);
    }

    @Override
    public void serverPacketData(ServerPlayer player) {
        if (player.containerMenu instanceof MEStorageMenu aeMenu) {
            if (player.containerMenu.containerId != containerId) {
                return;
            }

            aeMenu.transferMatchingStacksToPlayer(stacks, toPull);
        }
    }

}
