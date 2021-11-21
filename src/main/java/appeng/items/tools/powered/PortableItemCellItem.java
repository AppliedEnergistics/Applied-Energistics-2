package appeng.items.tools.powered;

import net.minecraft.world.inventory.MenuType;

import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.AEItemKey;
import appeng.menu.me.items.PortableItemCellMenu;

public class PortableItemCellItem extends PortableCellItem<AEItemKey> {
    public PortableItemCellItem(StorageTier tier, Properties props) {
        super(StorageChannels.items(), tier, props);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return PortableItemCellMenu.TYPE;
    }
}
