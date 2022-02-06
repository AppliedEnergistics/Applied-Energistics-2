package appeng.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Thread-safe copy-on-write map wrapper. Does not accept null keys or values.
 */
public class CowMap<K, V> {
    private final IntFunction<? extends Map<K, V>> mapSupplier;
    private volatile Map<K, V> map;

    public CowMap(IntFunction<? extends Map<K, V>> mapSupplier) {
        this.mapSupplier = mapSupplier;
        this.map = Collections.unmodifiableMap(mapSupplier.apply(0));
    }

    public static <K, V> CowMap<K, V> identityHashMap() {
        return new CowMap<>(IdentityHashMap::new);
    }

    /**
     * Add the value to the map, or throw an IllegalArgumentException if it is already present.
     */
    public void putIfAbsent(K key, V value) throws IllegalArgumentException {
        Objects.requireNonNull(key, "Key may not be null");
        Objects.requireNonNull(value, "Value may not be null");

        synchronized (this) {
            if (map.containsKey(key)) {
                throw new IllegalArgumentException("Map already contains a value for the following key: " + key);
            }
            var newMap = mapSupplier.apply(map.size() + 1);
            newMap.putAll(map);
            newMap.put(key, value);
            map = Collections.unmodifiableMap(newMap);
        }
    }

    /**
     * Return the current unmodifiable map. Further additions will not be reflected in the returned object.
     */
    public Map<K, V> getMap() {
        return map;
    }
}
