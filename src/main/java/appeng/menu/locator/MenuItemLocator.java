package appeng.menu.locator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.core.AELog;

/**
 * Locates a menu host based on {@link appeng.api.implementations.menuobjects.IMenuItem} in the player inventory.
 * <p/>
 * Optionally also contains a block position and side in case the menu is to be opened by the item but for a clicked
 * host (i.e. network tool).
 */
record MenuItemLocator(
        int itemIndex,
        @Nullable BlockPos blockPos) implements MenuLocator {
    @Nullable
    public <T> T locate(Player player, Class<T> hostInterface) {
        ItemStack it = player.getInventory().getItem(itemIndex);

        if (!it.isEmpty() && it.getItem() instanceof IMenuItem guiItem) {
            // Optionally contains the block the item was used on to open the menu
            ItemMenuHost menuHost = guiItem.getMenuHost(player, itemIndex, it, blockPos);
            if (hostInterface.isInstance(menuHost)) {
                return hostInterface.cast(menuHost);
            } else if (menuHost != null) {
                AELog.warn("Item in slot %d of %s did not create a compatible menu of type %s: %s",
                        itemIndex, player, hostInterface, menuHost);
            }
        } else {
            AELog.warn("Item in slot %d of %s is not an IMenuItem: %s",
                    itemIndex, player, it);
        }

        return null;
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(itemIndex);
        buf.writeBoolean(blockPos != null);
        if (blockPos != null) {
            buf.writeBlockPos(blockPos);
        }
    }

    public static MenuItemLocator readFromPacket(FriendlyByteBuf buf) {
        var itemIndex = buf.readInt();
        BlockPos blockPos = null;
        if (buf.readBoolean()) {
            blockPos = buf.readBlockPos();
        }
        return new MenuItemLocator(itemIndex, blockPos);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("MenuItem");
        result.append('{');
        result.append("slot=").append(itemIndex);
        if (blockPos != null) {
            result.append(',').append("pos=").append(blockPos);
        }
        result.append('}');
        return result.toString();
    }

}
