package appeng.menu;

import appeng.api.storage.ISubMenuHost;
import appeng.menu.locator.MenuLocator;

/**
 * A menu that is usually opened from another menu, and that offers a way to return to that main menu.
 */
public interface ISubMenu {
    /**
     * @return The locator used to open this sub-menu.
     */
    MenuLocator getLocator();

    /**
     * @return The host used to open this sub-menu. Can be used to return to the main menu.
     */
    ISubMenuHost getHost();
}
