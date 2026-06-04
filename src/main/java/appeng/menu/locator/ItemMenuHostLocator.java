package appeng.menu.locator;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;

/**
 * A locator for stacks of items that implement {@link appeng.api.implementations.menuobjects.IMenuItem}. This allows
 * such items to be stored in various places (player inventory, curio slots) when they host a menu.
 */
public interface ItemMenuHostLocator extends MenuHostLocator {
    Logger LOG = LoggerFactory.getLogger(ItemMenuHostLocator.class);

    default <T> T locate(Player player, Class<T> hostInterface) {
        ItemStack it = locateItem(player);

        if (!it.isEmpty() && it.getItem() instanceof IMenuItem menuItem) {
            var menuHost = menuItem.getMenuHost(player, this, hitResult());
            if (hostInterface.isInstance(menuHost)) {
                return hostInterface.cast(menuHost);
            } else if (menuHost != null) {
                LOG.warn("Item in {} of {} did not create a compatible menu of type {}: {}",
                        this, player, hostInterface, menuHost);
            }
        } else {
            LOG.warn("Item in {} of {} is not an IMenuItem: {}",
                    this, player, it);
        }

        return null;
    }

    /**
     * @return The optional location where the item was used on to open the menu.
     */
    @Nullable
    BlockHitResult hitResult();

    /**
     * Locates the MenuItem in the Players inventory and returns it if it satisfies the expected menu host interface.
     * <strong>The returned stack will be modified by the {@link ItemMenuHost}</strong>, it must be updated in-place.
     */
    ItemStack locateItem(Player player);

    /**
     * @return The slot of the item in the player inventory if this locator represents a location in the player
     *         inventory. Used to lock the slot against accidentally moving the item out.
     */
    @Nullable
    default Integer getPlayerInventorySlot() {
        return null;
    }
}
