package appeng.client.guidebook.extensions;

/**
 * The base interface for all other classes and interfaces acting as extensions.
 *
 * @see ExtensionCollection
 */
public interface Extension {
    default void onExtensionsBuilt(ExtensionCollection extensions) {
    }
}
