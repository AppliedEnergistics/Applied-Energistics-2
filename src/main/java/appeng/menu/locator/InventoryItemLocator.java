package appeng.menu.locator;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.core.AELog;

/**
 * Locates a menu host based on {@link IMenuItem} in the player inventory.
 * <p/>
 * Optionally also contains a block position and side in case the menu is to be opened by the item but for a clicked
 * host (i.e. network tool).
 */
record InventoryItemLocator(int itemIndex, @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    @Nullable
    public <T> T locate(Player player, Class<T> hostInterface) {
        ItemStack it = locateItem(player);

        if (!it.isEmpty() && it.getItem() instanceof IMenuItem guiItem) {
            // Optionally contains the block the item was used on to open the menu
            ItemMenuHost menuHost = guiItem.getMenuHost(player, this, it, hitResult);
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

    public ItemStack locateItem(Player player) {
        return player.getInventory().getItem(itemIndex);
    }

    @Override
    public boolean setItem(Player player, ItemStack stack) {
        player.getInventory().setItem(itemIndex, stack);
        return true;
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(itemIndex);
        buf.writeOptional(Optional.ofNullable(hitResult), FriendlyByteBuf::writeBlockHitResult);
    }

    public static InventoryItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new InventoryItemLocator(
                buf.readInt(),
                buf.readOptional(FriendlyByteBuf::readBlockHitResult).orElse(null));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("slot ").append(itemIndex);
        if (hitResult != null) {
            result.append(" used on ").append(hitResult.getBlockPos());
        }
        return result.toString();
    }
}
