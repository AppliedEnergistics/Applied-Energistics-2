package appeng.api.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.menu.ISubMenu;

/**
 * Implemented by objects that open a menu, which then opens a submenu. It's used to determine how to return to the main
 * menu from a submenu.
 */
public interface ISubMenuHost {
    /**
     * Returns to the primary user interface for this host. Used by sub-menus when players want to return to the
     * previous screen.
     */
    void returnToMainMenu(Player player, ISubMenu subMenu);

    /**
     * Gets the icon to represent the host of the submenu. Used as the icon for the back button.
     */
    ItemStack getMainMenuIcon();
}
