package appeng.menu.locator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.core.AELog;
import appeng.integration.modules.curios.CuriosIntegration;

record CuriosItemLocator(int itemIndex) implements MenuItemLocator {
    @Override
    public <T> @Nullable T locate(Player player, Class<T> hostInterface) {
        ItemStack it = locateItem(player);

        if (!it.isEmpty() && it.getItem() instanceof IMenuItem guiItem) {
            ItemMenuHost menuHost = guiItem.getMenuHost(player, this, it, null);
            if (hostInterface.isInstance(menuHost)) {
                return hostInterface.cast(menuHost);
            } else if (menuHost != null) {
                AELog.warn("Item in Curios slot %d of %s did not create a compatible menu of type %s: %s",
                        itemIndex, player, hostInterface, menuHost);
            }
        } else {
            AELog.warn("Item in Curios slot %d of %s is not an IMenuItem: %s",
                    itemIndex, player, it);
        }

        return null;
    }

    public ItemStack locateItem(Player player) {
        var cap = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (cap == null)
            return ItemStack.EMPTY;
        return cap.getStackInSlot(itemIndex);
    }

    @Override
    public boolean setItem(Player player, ItemStack stack) {
        var cap = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (cap == null)
            return false;
        cap.extractItem(itemIndex, 1, false);
        return cap.insertItem(itemIndex, stack, false).isEmpty();
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(itemIndex);
    }

    public static CuriosItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CuriosItemLocator(buf.readInt());
    }

    @Override
    public String toString() {
        return "MenuItem{CuriosSlot=" + itemIndex + "}";
    }
}
