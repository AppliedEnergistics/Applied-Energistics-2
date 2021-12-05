package appeng.api.storage;

/**
 * Provides {@link IStorageProvider} with a convenient way to control the storage they provide to the network.
 */
public interface IStorageMounts {
    int DEFAULT_PRIORITY = 0;

    default void mount(MEStorage inventory) {
        mount(inventory, DEFAULT_PRIORITY);
    }

    void mount(MEStorage inventory, int priority);
}
