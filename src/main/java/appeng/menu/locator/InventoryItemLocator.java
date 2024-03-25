package appeng.menu.locator;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.menuobjects.IMenuItem;

/**
 * Locates a menu host based on {@link IMenuItem} in the player inventory.
 * <p/>
 * Optionally also contains a block position and side in case the menu is to be opened by the item but for a clicked
 * host (i.e. network tool).
 */
record InventoryItemLocator(int itemIndex, @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    public ItemStack locateItem(Player player) {
        return player.getInventory().getItem(itemIndex);
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

    @Override
    public Integer getPlayerInventorySlot() {
        return itemIndex;
    }
}
