package appeng.util.item;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.AEKey;

/**
 * Stores variants of a single type of {@link net.minecraft.world.item.Item}, i.e. versions with different durability,
 * or different NBT or capabilities.
 */
public abstract class VariantMap<K extends AEKey, V> {
    private final V defaultValue;
    private final Supplier<V> defaultValueFactory;

    /**
     * Creates a variant map that is suitable for the given key, considering it's support for fuzzy search.
     */
    public static <K extends AEKey, V> VariantMap<K, V> create(K keyTemplate, V defaultValue,
            Supplier<V> defaultValueFactory) {
        if (keyTemplate.getFuzzySearchMaxValue() > 0) {
            return new FuzzyVariantMap<>(defaultValue, defaultValueFactory);
        } else {
            return new UnorderedVariantMap<>(defaultValue, defaultValueFactory);
        }
    }

    public VariantMap(V defaultValue, Supplier<V> defaultValueSupplier) {
        this.defaultValue = defaultValue;
        this.defaultValueFactory = defaultValueSupplier;
    }

    public V mapping(K key) {
        return this.getRecords().computeIfAbsent(key, this::createDefaultValue);
    }

    private V createDefaultValue(K k) {
        return defaultValueFactory.get();
    }

    public V findPrecise(K key) {
        return this.getRecords().get(key);
    }

    public abstract Collection<Map.Entry<K, V>> findFuzzy(K filter, FuzzyMode fuzzy);

    public int size() {
        int size = 0;
        for (var value : getRecords().values()) {
            if (value.equals(defaultValue)) {
                size++;
            }
        }

        return size;
    }

    public boolean isEmpty() {
        for (var value : getRecords().values()) {
            if (!value.equals(defaultValue)) {
                return false;
            }
        }

        return true;
    }

    public Iterator<Map.Entry<K, V>> iterator() {
        return new NonDefaultIterator();
    }

    abstract Map<K, V> getRecords();

    /**
     * Only returns entries that are not {@link #defaultValue default values}.
     */
    private class NonDefaultIterator implements Iterator<Map.Entry<K, V>> {
        private final Iterator<Map.Entry<K, V>> parent;
        private Map.Entry<K, V> next;

        public NonDefaultIterator() {
            this.parent = getRecords().entrySet().iterator();
            this.next = seekNext();
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public Map.Entry<K, V> next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }

            Map.Entry<K, V> result = this.next;
            this.next = this.seekNext();
            return result;
        }

        private Map.Entry<K, V> seekNext() {
            while (this.parent.hasNext()) {
                Map.Entry<K, V> entry = this.parent.next();

                if (entry.getValue().equals(defaultValue)) {
                    this.parent.remove();
                } else {
                    return entry;
                }
            }

            return null;
        }
    }

    /**
     * This variant list is optimized for items that cannot be damaged and thus do not support querying durability
     * ranges via {@link #findFuzzy}.
     */
    private static class UnorderedVariantMap<K extends AEKey, V> extends VariantMap<K, V> {
        public UnorderedVariantMap(V defaultValue, Supplier<V> defaultValueSupplier) {
            super(defaultValue, defaultValueSupplier);
        }

        private final Reference2ObjectMap<K, V> records = new Reference2ObjectOpenHashMap<>();

        /**
         * For keys whose primary key does not support fuzzy range lookups, we simply return all records, which amounts
         * to ignoring NBT.
         */
        @Override
        public Collection<Map.Entry<K, V>> findFuzzy(K filter, FuzzyMode fuzzy) {
            return records.entrySet();
        }

        @Override
        Map<K, V> getRecords() {
            return records;
        }
    }

    /**
     * This variant list is optimized for damageable items, and supports selecting durability ranges with
     * {@link #findFuzzy}.
     */
    private static class FuzzyVariantMap<K extends AEKey, V> extends VariantMap<K, V> {
        private final Object2ObjectSortedMap<K, V> records = FuzzySearch.createMap();

        public FuzzyVariantMap(V defaultValue, Supplier<V> defaultValueSupplier) {
            super(defaultValue, defaultValueSupplier);
        }

        @Override
        public Collection<Map.Entry<K, V>> findFuzzy(K key, FuzzyMode fuzzy) {
            return FuzzySearch.findFuzzy(records, key, fuzzy).entrySet();
        }

        @Override
        Map<K, V> getRecords() {
            return this.records;
        }
    }
}
