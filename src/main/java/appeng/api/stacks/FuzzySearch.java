package appeng.api.stacks;

import java.util.Comparator;
import java.util.SortedMap;

import com.google.common.annotations.VisibleForTesting;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;

import appeng.api.config.FuzzyMode;

/**
 * Implements fuzzy search over keys based on their relative damage ({@link AEKey#getFuzzySearchValue()} divided by
 * {@link AEKey#getFuzzySearchMaxValue()}).
 * <p>
 * Since the max damage of items is just another data component, keys that share the same primary key can have different
 * max damage values, or none at all. Keys without a fuzzy max value are treated as undamaged. Fuzzy modes split keys
 * into two partitions: those damaged more than the mode's {@link FuzzyMode#breakPoint}, and all others. A key matches a
 * filter if both are in the same partition.
 */
final class FuzzySearch {
    @VisibleForTesting
    static final KeyComparator COMPARATOR = new KeyComparator();

    private FuzzySearch() {
    }

    /**
     * Creates a map that is searchable via {@link #findFuzzy}.
     */
    public static <K extends AEKey, V> Object2ObjectSortedMap<K, V> createMap() {
        return new Object2ObjectAVLTreeMap<>(COMPARATOR);
    }

    /**
     * Creates a map that is searchable via {@link #findFuzzy}.
     */
    public static AEKey2LongMap.AVLTreeMap createMap2Long() {
        return new AEKey2LongMap.AVLTreeMap(COMPARATOR);
    }

    /**
     * Does a fuzzy search. The map must have been created using {@link #createMap}. Returns a view of the map
     * containing exactly the keys for which {@link #matches} is true.
     */
    @SuppressWarnings({ "unchecked" })
    public static <T extends SortedMap<K, V>, K, V> T findFuzzy(T map, AEKey filter, FuzzyMode fuzzy) {
        if (matchesAll(filter, fuzzy)) {
            return map;
        }

        // Our comparator (see below) sorts all damaged keys before the split, and all undamaged keys after it
        var split = (K) new FuzzySplit(fuzzy.breakPoint);
        if (isDamaged(getDamagePercentage(filter), fuzzy.breakPoint)) {
            return (T) map.headMap(split);
        } else {
            return (T) map.tailMap(split);
        }
    }

    /**
     * Tests if the given key matches the filter under the given fuzzy mode. The key and filter must share the same
     * primary key.
     */
    public static boolean matches(AEKey key, AEKey filter, FuzzyMode fuzzy) {
        return matchesAll(filter, fuzzy)
                || isDamaged(getDamagePercentage(key), fuzzy.breakPoint) == isDamaged(getDamagePercentage(filter),
                        fuzzy.breakPoint);
    }

    /**
     * Same as {@link #matches}, for keys that have no fuzzy max value, and are thus undamaged.
     */
    public static boolean matchesUndamaged(AEKey filter, FuzzyMode fuzzy) {
        return matchesAll(filter, fuzzy) || !isDamaged(getDamagePercentage(filter), fuzzy.breakPoint);
    }

    private static boolean matchesAll(AEKey filter, FuzzyMode fuzzy) {
        return fuzzy == FuzzyMode.IGNORE_ALL || !filter.supportsFuzzyRangeSearch();
    }

    private static boolean isDamaged(float damagePercentage, float breakPoint) {
        return damagePercentage > breakPoint;
    }

    /**
     * Computed the same way as in {@link AEKey#fuzzyEquals}.
     */
    @VisibleForTesting
    static float getDamagePercentage(AEKey key) {
        var maxValue = key.getFuzzySearchMaxValue();
        return maxValue > 0 ? (float) key.getFuzzySearchValue() / maxValue : 0;
    }

    /**
     * Is never stored in a map. Only used as the bound for head/tail maps. It sorts after all keys that are damaged
     * more than the break point, and before all other keys.
     */
    private record FuzzySplit(float breakPoint) {
    }

    /**
     * This comparator creates a strict and total ordering over all {@link AEKey} of the same primary key, from most to
     * least damaged. To support selecting damage partitions, it is defined for type {@link Object} and also accepts a
     * {@link FuzzySplit} as an argument to compare against.
     */
    private static class KeyComparator implements Comparator<Object> {
        @Override
        public int compare(Object a, Object b) {
            // Since splits are never put into the map, only one of the arguments can possibly be a split
            if (a instanceof FuzzySplit split) {
                return isDamaged(getDamagePercentage((AEKey) b), split.breakPoint) ? 1 : -1;
            } else if (b instanceof FuzzySplit split) {
                return isDamaged(getDamagePercentage((AEKey) a), split.breakPoint) ? -1 : 1;
            }

            var keyA = (AEKey) a;
            var keyB = (AEKey) b;
            if (keyA.equals(keyB)) {
                return 0;
            }

            // Damaged items are sorted before undamaged items
            var fuzzyOrder = Float.compare(getDamagePercentage(keyB), getDamagePercentage(keyA));
            if (fuzzyOrder != 0) {
                return fuzzyOrder;
            }

            // As a final tie breaker, order by the hash code of the key
            // While this will order seemingly at random, we only need the order of
            // damage values to be predictable, while still having to satisfy the
            // complete order requirements of the sorted map
            // (We hope there won't be hash collisions... the probability is very low anyway)
            return Long.compare(keyA.hashCode(), keyB.hashCode());
        }
    }
}
