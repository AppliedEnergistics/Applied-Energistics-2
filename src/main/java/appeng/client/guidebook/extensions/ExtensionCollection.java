package appeng.client.guidebook.extensions;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of extensions registered to modify the guidebook.
 */
public class ExtensionCollection {
    private static final ExtensionCollection EMPTY = new ExtensionCollection(Map.of());

    private final List<ExtensionPoint<?>> extensionPoints;
    private final Map<ExtensionPoint<?>, List<Object>> extensions;

    /**
     * @return An empty extension collection.
     */
    public static ExtensionCollection empty() {
        return EMPTY;
    }

    private ExtensionCollection(Map<ExtensionPoint<?>, List<Object>> extensions) {
        extensionPoints = List.copyOf(extensions.keySet());
        var checkedCollection = new IdentityHashMap<>(extensions);
        for (var entry : checkedCollection.entrySet()) {
            var extensionPoint = entry.getKey();
            entry.setValue(List.copyOf(entry.getValue()));
            for (Object o : entry.getValue()) {
                if (!extensionPoint.extensionPointClass().isInstance(o)) {
                    throw new IllegalArgumentException("Extension point " + extensionPoint
                            + " has incompatible extension registered: " + o);
                }
            }
        }
        this.extensions = checkedCollection;
    }

    /**
     * Returns the registered extensions for the given extension point.
     */
    @SuppressWarnings("unchecked")
    public <T extends Extension> List<T> get(ExtensionPoint<T> extensionPoint) {
        var extensions = this.extensions.get(extensionPoint);
        if (extensions == null) {
            return List.of();
        }
        return (List<T>) (Object) extensions;
    }

    /**
     * @return The extension points for which this collection contains extensions.
     */
    public List<ExtensionPoint<?>> getExtensionPoints() {
        return extensionPoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<ExtensionPoint<?>, List<Object>> extensions = new IdentityHashMap<>();

        /**
         * Adds an extension to the given extension point for this guide.
         */
        public <T extends Extension> Builder add(ExtensionPoint<T> extensionPoint, T extension) {
            var extensions = this.extensions.computeIfAbsent(extensionPoint,
                    guidebookExtensionPoint -> new ArrayList<>());
            for (Object o : extensions) {
                if (o == extension) {
                    throw new IllegalStateException(
                            "Extension " + extension + " is already registered for " + extensionPoint);
                }
            }
            extensions.add(extension);
            return this;
        }

        /**
         * Adds all extensions from the given collection to this builder.
         */
        public Builder addAll(ExtensionCollection collection) {
            for (var extensionPoint : collection.getExtensionPoints()) {
                addAll(collection, extensionPoint);
            }
            return this;
        }

        /**
         * Adds all extensions from the given collection to this builder.
         */
        public Builder addAll(ExtensionCollection.Builder builder) {
            for (var entry : builder.extensions.entrySet()) {
                for (Object o : entry.getValue()) {
                    addUntyped(entry.getKey(), o);
                }
            }
            return this;
        }

        private <T extends Extension> void addUntyped(ExtensionPoint<T> extensionPoint, Object extension) {
            var castExtension = extensionPoint.extensionPointClass().cast(extension);
            add(extensionPoint, castExtension);
        }

        private <T extends Extension> void addAll(ExtensionCollection collection, ExtensionPoint<T> extensionPoint) {
            for (var extension : collection.get(extensionPoint)) {
                add(extensionPoint, extension);
            }
        }

        public ExtensionCollection build() {
            var collection = new ExtensionCollection(extensions);

            for (var extensionPoint : collection.extensionPoints) {
                callInitializationCallback(collection, extensionPoint);
            }

            return collection;
        }

        private <T extends Extension> void callInitializationCallback(ExtensionCollection collection,
                ExtensionPoint<T> extensionPoint) {
            for (var extension : collection.get(extensionPoint)) {
                extension.onExtensionsBuilt(collection);
            }
        }
    }
}
