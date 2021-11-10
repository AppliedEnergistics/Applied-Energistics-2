package appeng.api.storage;

import appeng.api.storage.data.AEKey;

/**
 * Provides {@link IStorageProvider} with a convenient way to control the storage they provide to the network.
 */
public interface IStorageMounts {
    int DEFAULT_PRIORITY = 0;

    default <T extends AEKey> void mount(IMEInventory<T> inventory) {
        mount(inventory, DEFAULT_PRIORITY);
    }

    <T extends AEKey> void mount(IMEInventory<T> inventory, int priority);
}
