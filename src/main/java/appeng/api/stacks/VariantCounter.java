package appeng.api.stacks;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMap;

import appeng.api.config.FuzzyMode;

/**
 * Tallies a negative or positive amount for sub-variants of a {@link AEKey}.
 */
abstract class VariantCounter implements Iterable<Object2LongMap.Entry<AEKey>> {
    /**
     * Enable to skip and remove keys that are mapped to zero.
     */
    private boolean dropZeros;

    /**
     * Creates a variant map that is suitable for the given key, considering it's support for fuzzy search.
     */
    public static VariantCounter create(AEKey keyTemplate) {
        if (keyTemplate.getFuzzySearchMaxValue() > 0) {
            return new FuzzyVariantMap();
        } else {
            return new UnorderedVariantMap();
        }
    }

    public boolean isDropZeros() {
        return dropZeros;
    }

    public void setDropZeros(boolean dropZeros) {
        this.dropZeros = dropZeros;
    }

    public long get(AEKey key) {
        return this.getRecords().getOrDefault(key, 0);
    }

    public void add(AEKey key, long amount) {
        this.getRecords().mergeLong(key, amount, Long::sum);
    }

    public void set(AEKey key, long amount) {
        if (dropZeros && amount == 0) {
            getRecords().removeLong(key);
        } else {
            getRecords().put(key, amount);
        }
    }

    public void addAll(VariantCounter other) {
        for (var entry : other.getRecords().object2LongEntrySet()) {
            add(entry.getKey(), entry.getLongValue());
        }
    }

    public void removeAll(VariantCounter other) {
        for (var entry : other.getRecords().object2LongEntrySet()) {
            add(entry.getKey(), -entry.getLongValue());
        }
    }

    public abstract Collection<Object2LongMap.Entry<AEKey>> findFuzzy(AEKey filter, FuzzyMode fuzzy);

    public int size() {
        if (!dropZeros) {
            return getRecords().size();
        }

        var size = 0;
        for (var value : getRecords().values()) {
            if (value != 0) {
                size++;
            }
        }

        return size;
    }

    public boolean isEmpty() {
        if (!dropZeros) {
            return getRecords().isEmpty();
        }

        for (var value : getRecords().values()) {
            if (value != 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Iterator<Object2LongMap.Entry<AEKey>> iterator() {
        if (!dropZeros) {
            return getRecords().object2LongEntrySet().iterator();
        }

        return new NonDefaultIterator();
    }

    abstract Object2LongMap<AEKey> getRecords();

    /**
     * Sets all amounts to zero.
     */
    public void reset() {
        if (dropZeros) {
            getRecords().clear();
        } else {
            getRecords().replaceAll((key, value) -> 0L);
        }
    }

    public void clear() {
        getRecords().clear();
    }

    public abstract VariantCounter copy();

    public void invert() {
        for (var entry : getRecords().object2LongEntrySet()) {
            entry.setValue(-entry.getLongValue());
        }
    }

    public void removeZeros() {
        var it = getRecords().values().iterator();
        while (it.hasNext()) {
            var entry = it.nextLong();
            if (entry == 0) {
                it.remove();
            }
        }
    }

    /**
     * Only returns entries that do not have amount 0.
     */
    private class NonDefaultIterator implements Iterator<Object2LongMap.Entry<AEKey>> {
        private final Iterator<Object2LongMap.Entry<AEKey>> parent;
        private Object2LongMap.Entry<AEKey> next;

        public NonDefaultIterator() {
            this.parent = getRecords().object2LongEntrySet().iterator();
            this.next = seekNext();
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public Object2LongMap.Entry<AEKey> next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }

            var result = this.next;
            this.next = this.seekNext();
            return result;
        }

        private Object2LongMap.Entry<AEKey> seekNext() {
            while (this.parent.hasNext()) {
                var entry = this.parent.next();

                if (entry.getLongValue() == 0) {
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
    private static class UnorderedVariantMap extends VariantCounter {
        private final Object2LongMap<AEKey> records = new Object2LongOpenHashMap<>();

        /**
         * For keys whose primary key does not support fuzzy range lookups, we simply return all records, which amounts
         * to ignoring NBT.
         */
        @Override
        public Collection<Object2LongMap.Entry<AEKey>> findFuzzy(AEKey filter, FuzzyMode fuzzy) {
            return records.object2LongEntrySet();
        }

        @Override
        Object2LongMap<AEKey> getRecords() {
            return records;
        }

        @Override
        public VariantCounter copy() {
            var result = new UnorderedVariantMap();
            result.records.putAll(records);
            return result;
        }
    }

    /**
     * This variant list is optimized for damageable items, and supports selecting durability ranges with
     * {@link #findFuzzy}.
     */
    private static class FuzzyVariantMap extends VariantCounter {
        private final Object2LongSortedMap<AEKey> records = FuzzySearch.createMap2Long();

        @Override
        public Collection<Object2LongMap.Entry<AEKey>> findFuzzy(AEKey key, FuzzyMode fuzzy) {
            return FuzzySearch.findFuzzy(records, key, fuzzy).object2LongEntrySet();
        }

        @Override
        Object2LongMap<AEKey> getRecords() {
            return this.records;
        }

        @Override
        public VariantCounter copy() {
            var result = new FuzzyVariantMap();
            result.records.putAll(records);
            return result;
        }
    }
}
