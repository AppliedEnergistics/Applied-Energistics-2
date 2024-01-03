package appeng.core.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.core.network.ServerboundPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;
import appeng.util.Platform;

public record InventoryActionPacket(InventoryAction action,
        int slot,
        long extraId,
        ItemStack slotItem) implements ServerboundPacket {

    // api
    public InventoryActionPacket(InventoryAction action, int slot, long id) {
        this(action, slot, id, ItemStack.EMPTY);
    }

    // api
    public InventoryActionPacket(InventoryAction action, int slot, ItemStack slotItem) {
        this(action, slot, 0, slotItem.copy());
        if (Platform.isClient() && action != InventoryAction.SET_FILTER) {
            throw new IllegalStateException("invalid packet, client cannot post inv actions with stacks.");
        }
    }

    public static InventoryActionPacket decode(FriendlyByteBuf stream) {
        var action = stream.readEnum(InventoryAction.class);
        var slot = stream.readInt();
        var extraId = stream.readLong();
        var slotItem = stream.readItem();
        return new InventoryActionPacket(action, slot, extraId, slotItem);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeEnum(action);
        data.writeInt(slot);
        data.writeLong(extraId);
        data.writeItem(slotItem);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof AEBaseMenu baseMenu) {
            if (action == InventoryAction.SET_FILTER) {
                baseMenu.setFilter(this.slot, this.slotItem);
            } else {
                baseMenu.doAction(player, this.action, this.slot, this.extraId);
            }
        }
    }

}
