package appeng.api.stacks;

import java.util.Comparator;
import java.util.SortedMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2LongAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;

import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;

final class FuzzySearch {
    @VisibleForTesting
    static final KeyComparator COMPARATOR = new KeyComparator();

    private FuzzySearch() {
    }

    /**
     * Creates a map that is searchable via {@link #findFuzzy}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <K extends AEKey, V> Object2ObjectAVLTreeMap<K, V> createMap() {
        return new Object2ObjectAVLTreeMap(COMPARATOR);
    }

    /**
     * Creates a map that is searchable via {@link #findFuzzy}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <K extends AEKey> Object2LongAVLTreeMap<K> createMap2Long() {
        return new Object2LongAVLTreeMap(COMPARATOR);
    }

    /**
     * Does a fuzzy search. The map must have been created using {@link #createMap}.
     */
    @SuppressWarnings({ "unchecked" })
    public static <T extends SortedMap<K, V>, K, V> T findFuzzy(T map, AEKey key, FuzzyMode fuzzy) {
        var lowerBound = makeLowerBound(key, fuzzy);
        var upperBound = makeUpperBound(key, fuzzy);
        Preconditions.checkState(lowerBound.itemDamage > upperBound.itemDamage);

        // We can use lower/upper bound in this map for queries because our comparator (see below) specifically
        // supports dealing with it
        return (T) map.subMap((K) lowerBound, (K) upperBound);
    }

    @VisibleForTesting
    record FuzzyBound(int itemDamage) {
    }

    /**
     * This comparator creates a strict and total ordering over all {@link AEKey} of the same item. To support selecting
     * ranges of durability, it is defined for type {@link Object} and also accepts {@link FuzzyBound} as an argument to
     * compare against.
     */
    private static class KeyComparator implements Comparator<Object> {
        @Override
        public int compare(Object a, Object b) {
            // Either argument can either be a damage bound or a shared item stack
            // Since we never put damage bounds into the map as keys, only one
            // of the two arguments can possibly be a bound
            FuzzyBound boundA = null;
            AEKey stackA = null;
            int fuzzyOrderB;
            if (a instanceof FuzzyBound) {
                boundA = (FuzzyBound) a;
                fuzzyOrderB = boundA.itemDamage;
            } else {
                stackA = (AEKey) a;
                fuzzyOrderB = stackA.getFuzzySearchValue();
            }
            FuzzyBound boundB = null;
            AEKey stackB = null;
            int fuzzyOrderA;
            if (b instanceof FuzzyBound) {
                boundB = (FuzzyBound) b;
                fuzzyOrderA = boundB.itemDamage;
            } else {
                stackB = (AEKey) b;
                fuzzyOrderA = stackB.getFuzzySearchValue();
            }

            // When either argument is a damage bound, we just compare the damage values because it is used
            // only to get a certain damage range out of the map.
            if (boundA != null || boundB != null) {
                return Integer.compare(fuzzyOrderA, fuzzyOrderB);
            }

            if (stackA.equals(stackB)) {
                return 0;
            }

            // Damaged items are sorted before undamaged items
            final var fuzzyOrder = Integer.compare(fuzzyOrderA, fuzzyOrderB);
            if (fuzzyOrder != 0) {
                return fuzzyOrder;
            }

            // As a final tie breaker, order by the object identity of the item stack
            // While this will order seemingly at random, we only need the order of
            // damage values to be predictable, while still having to satisfy the
            // complete order requirements of the sorted map
            return Long.compare(System.identityHashCode(stackA), System.identityHashCode(stackB));
        }
    }

    /**
     * Minecraft reverses the damage values. So anything with a damage of 0 is undamaged and increases the more damaged
     * the item is.
     * <p>
     * Further the used subMap follows [MAX_DAMAGE, MIN_DAMAGE), so to include undamaged items, we have to start with a
     * lower damage value than 0, while it is fine to use {@link ItemStack#getMaxDamage()} for the upper bound.
     */
    private static final int MIN_DAMAGE_VALUE = -1;

    /*
     * Keep in mind that the stack order is from most damaged to least damaged, so this lower bound will actually be a
     * higher number than the upper bound.
     */
    static FuzzyBound makeLowerBound(AEKey key, FuzzyMode fuzzy) {
        var maxValue = key.getFuzzySearchMaxValue();
        Preconditions.checkState(maxValue > 0, "Cannot use fuzzy search on keys that don't have a fuzzy max value: %s",
                key);

        int damage;
        if (fuzzy == FuzzyMode.IGNORE_ALL) {
            damage = maxValue;
        } else {
            var breakpoint = fuzzy.calculateBreakPoint(maxValue);
            damage = key.getFuzzySearchValue() <= breakpoint ? breakpoint : maxValue;
        }

        return new FuzzyBound(damage);
    }

    /*
     * Keep in mind that the stack order is from most damaged to least damaged, so this upper bound will actually be a
     * lower number than the lower bound. It also is exclusive.
     */
    static FuzzyBound makeUpperBound(AEKey key, FuzzyMode fuzzy) {
        var maxValue = key.getFuzzySearchMaxValue();
        Preconditions.checkState(maxValue > 0, "Cannot use fuzzy search on keys that don't have a fuzzy max value: %s",
                key);

        int damage;
        if (fuzzy == FuzzyMode.IGNORE_ALL) {
            damage = MIN_DAMAGE_VALUE;
        } else {
            final var breakpoint = fuzzy.calculateBreakPoint(maxValue);
            damage = key.getFuzzySearchValue() <= breakpoint ? MIN_DAMAGE_VALUE : breakpoint;
        }

        return new FuzzyBound(damage);
    }
}
