package appeng.menu.locator;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jline.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A locator for stacks of items that implement {@link appeng.api.implementations.menuobjects.IMenuItem}. This allows
 * such items to be stored in various places (player inventory, curio slots) when they host a menu.
 */
public interface ItemMenuHostLocator extends MenuHostLocator {
    Logger LOG = LoggerFactory.getLogger(ItemMenuHostLocator.class);

    default <T> T locate(Player player, Class<T> hostInterface) {
        ItemStack it = locateItem(player);

        if (!it.isEmpty() && it.getItem() instanceof IMenuItem menuItem) {
            ItemMenuHost menuHost = menuItem.getMenuHost(player, this, it, null);
            if (hostInterface.isInstance(menuHost)) {
                return hostInterface.cast(menuHost);
            } else if (menuHost != null) {
                Log.warn("Item in {} of {} did not create a compatible menu of type {}: {}",
                        this, player, hostInterface, menuHost);
            }
        } else {
            Log.warn("Item in {} of {} is not an IMenuItem: {}",
                    this, player, it);
        }

        return null;
    }

    /**
     * Locates the MenuItem in the Players inventory and returns it if it satisfies the expected menu host interface.
     */
    ItemStack locateItem(Player player);

    /**
     * Replace the Item the MenuItemLocator is pointing to
     *
     * @return true if the modification was successful
     */
    boolean setItem(Player player, ItemStack stack);
}
