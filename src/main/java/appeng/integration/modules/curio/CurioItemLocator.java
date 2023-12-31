package appeng.integration.modules.curio;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.core.AELog;
import appeng.menu.locator.MenuItemLocator;

public record CurioItemLocator(int itemIndex) implements MenuItemLocator {
    @Override
    public <T> @Nullable T locate(Player player, Class<T> hostInterface) {
        ItemStack it = locateItem(player);

        if (!it.isEmpty() && it.getItem() instanceof IMenuItem guiItem) {
            ItemMenuHost menuHost = guiItem.getMenuHost(player, this, it, null);
            if (hostInterface.isInstance(menuHost)) {
                return hostInterface.cast(menuHost);
            } else if (menuHost != null) {
                AELog.warn("Item in Curio slot %d of %s did not create a compatible menu of type %s: %s",
                        itemIndex, player, hostInterface, menuHost);
            }
        } else {
            AELog.warn("Item in Curio slot %d of %s is not an IMenuItem: %s",
                    itemIndex, player, it);
        }

        return null;
    }

    public ItemStack locateItem(Player player) {
        var cap = CurioModule.ITEM_HANDLER.getCapability(player, null);
        if (cap == null)
            return ItemStack.EMPTY;
        return cap.getStackInSlot(itemIndex);
    }

    @Override
    public boolean setItem(Player player, ItemStack stack) {
        var cap = CurioModule.ITEM_HANDLER.getCapability(player, null);
        if (cap == null)
            return false;
        cap.extractItem(itemIndex, 1, false);
        return cap.insertItem(itemIndex, stack, true).isEmpty();// TODO test if this actually works as expected
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(itemIndex);
    }

    public static CurioItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CurioItemLocator(buf.readInt());
    }
}
