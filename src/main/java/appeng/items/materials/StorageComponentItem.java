package appeng.items.materials;

import net.minecraft.item.ItemStack;

import appeng.api.implementations.items.IStorageComponent;
import appeng.items.AEBaseItem;

public class StorageComponentItem extends AEBaseItem implements IStorageComponent {
    private final int storageInKb;

    public StorageComponentItem(Properties properties, int storageInKb) {
        super(properties);
        this.storageInKb = storageInKb;
    }

    @Override
    public int getBytes(final ItemStack is) {
        return this.storageInKb * 1024;
    }

    @Override
    public boolean isStorageComponent(final ItemStack is) {
        return true;
    }
}
