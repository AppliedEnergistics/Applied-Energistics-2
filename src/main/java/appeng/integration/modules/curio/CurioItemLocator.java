package appeng.integration.modules.curio;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.core.AELog;
import appeng.menu.locator.MenuLocator;

public record CurioItemLocator(int index) implements MenuLocator {
    @Override
    public <T> @Nullable T locate(Player player, Class<T> hostInterface) {
        ItemStack it = locateItem(player);
        if (!it.isEmpty()) {
            Item item = it.getItem();
            if (item instanceof IMenuItem guiItem) {
                ItemMenuHost menuHost = guiItem.getMenuHost(player, this, it);
                if (hostInterface.isInstance(menuHost))
                    return hostInterface.cast(menuHost);

                if (menuHost != null) {
                    AELog.warn("Item in Curio slot with index %s of %s did not create a compatible menu of type %s: %s",
                            index, player, hostInterface, menuHost);
                }

                return null;
            }
        }

        AELog.warn("Item in Curio slot with  index %s of %s is not an IMenuItem: %s", index, player, it);
        return null;
    }

    public ItemStack locateItem(Player player) {
        var cap = CurioModule.ITEM_HANDLER.getCapability(player, null);
        if (cap == null)
            return ItemStack.EMPTY;
        return cap.getStackInSlot(index);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(index);
    }

    public static CurioItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CurioItemLocator(buf.readInt());
    }
}
