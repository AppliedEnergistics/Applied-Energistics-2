package appeng.items.tools.powered;

import appeng.api.storage.data.AEItemKey;
import appeng.menu.me.items.PortableItemCellMenu;
import net.minecraft.world.inventory.MenuType;

public class PortableItemCellItem extends PortableCellItem<AEItemKey> {
    public PortableItemCellItem(StorageTier tier, Properties props) {
        super(AEItemKey.filter(), tier, props);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return PortableItemCellMenu.TYPE;
    }
}
