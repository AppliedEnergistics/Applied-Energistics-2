package appeng.menu.me.items;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.storage.ITerminalHost;
import appeng.api.util.KeyTypeSelectionHost;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;

public class BasicCellChestMenu extends MEStorageMenu {
    public static final MenuType<MEStorageMenu> TYPE = MenuTypeBuilder
            .<MEStorageMenu, ITerminalHost>create(BasicCellChestMenu::new, ITerminalHost.class)
            .build("basic_cell_chest");

    public BasicCellChestMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    /**
     * {@link ChestBlockEntity} is an instance of {@link KeyTypeSelectionHost}, however basic cells only support a
     * single type.
     */
    @Override
    public boolean canConfigureTypeFilter() {
        return false;
    }
}
