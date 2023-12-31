package appeng.menu.locator;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface MenuItemLocator extends MenuLocator {
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
