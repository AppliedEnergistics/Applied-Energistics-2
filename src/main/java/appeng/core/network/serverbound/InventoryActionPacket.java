package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;
import appeng.util.Platform;

public record InventoryActionPacket(InventoryAction action,
        int slot,
        long extraId,
        ItemStack slotItem) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryActionPacket> STREAM_CODEC = StreamCodec.ofMember(
            InventoryActionPacket::write,
            InventoryActionPacket::decode);

    public static final Type<InventoryActionPacket> TYPE = CustomAppEngPayload.createType("inventory_action");

    @Override
    public Type<InventoryActionPacket> type() {
        return TYPE;
    }

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

    public static InventoryActionPacket decode(RegistryFriendlyByteBuf stream) {
        var action = stream.readEnum(InventoryAction.class);
        var slot = stream.readInt();
        var extraId = stream.readLong();
        var slotItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(stream);
        return new InventoryActionPacket(action, slot, extraId, slotItem);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeEnum(action);
        data.writeInt(slot);
        data.writeLong(extraId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(data, slotItem);
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
