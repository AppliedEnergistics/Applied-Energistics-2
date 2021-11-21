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
     * Used to show the user interface of this part when returning from the priority GUI.
     */
    void returnToMainMenu(Player player, ISubMenu subMenu);

    /**
     * Used to show the user interface of this part when returning from the priority GUI.
     */
    ItemStack getMainMenuIcon();
}
