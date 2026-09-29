package appeng.api.stacks;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongSortedMap;

import appeng.api.config.FuzzyMode;

/**
 * Tallies a negative or positive amount for sub-variants of a {@link AEKey}.
 */
class VariantCounter implements Iterable<Object2LongMap.Entry<AEKey>> {
    /**
     * The current state of this counter, determining which fields are valid and contain item information.
     */
    enum CounterState {
        EMPTY, // zero types
        SINGLE, // one type
        GENERIC, // 2+ stacks, none of which have durability
        FUZZY, // 2+ stacks, at least one of which has durability
    }

    private CounterState state;

    // valid IFF state == CounterState.SINGLE
    private AEKey key;
    // valid IFF state == CounterState.SINGLE
    private long count;
    // valid IFF state == CounterState.GENERIC
    private AEKey2LongMap.OpenHashMap genericRecords;
    // valid IFF state == CounterState.FUZZY
    private AEKey2LongMap.AVLTreeMap fuzzyRecords;

    public VariantCounter() {
        state = CounterState.EMPTY;
    }

    private VariantCounter(
            CounterState state,
            AEKey key,
            long count,
            AEKey2LongMap.OpenHashMap genericRecords,
            AEKey2LongMap.AVLTreeMap fuzzyRecords) {
        this.state = state;
        this.key = key;
        this.count = count;
        this.genericRecords = genericRecords;
        this.fuzzyRecords = fuzzyRecords;
    }

    public long get(AEKey key) {
        return switch (state) {
            case EMPTY -> 0;
            case SINGLE -> this.key.equals(key) ? count : 0;
            case GENERIC -> genericRecords.getOrDefault(key, 0);
            case FUZZY -> fuzzyRecords.getOrDefault(key, 0);
        };
    }

    public void add(AEKey key, long amount) {
        switch (state) {
            case EMPTY -> addEmpty(key, amount);
            case SINGLE -> addSingle(key, amount);
            case GENERIC -> addGeneric(key, amount);
            case FUZZY -> fuzzyRecords.addTo(key, amount);
        }
    }

    // valid IFF state == CounterState.GENERIC
    private void addGeneric(AEKey key, long amount) {
        if (key.getFuzzySearchMaxValue() > 0) {
            convertGenericToFuzzy();
            fuzzyRecords.addTo(key, amount);
        } else {
            genericRecords.addTo(key, amount);
        }
    }

    // valid IFF state == CounterState.EMPTY
    private void addEmpty(AEKey key, long amount) {
        this.key = key;
        this.count = amount;
        state = CounterState.SINGLE;
    }

    // valid IFF state == CounterState.SINGLE
    private void addSingle(AEKey key, long amount) {
        if (this.key.equals(key)) {
            count += amount;
        } else {
            addSingleDistinct(key, amount);
        }
    }

    // valid IFF state == CounterState.SINGLE && !this.key.equals(key)
    private void addSingleDistinct(AEKey key, long amount) {
        // Since durability is a component, variants of the same item may or may not have durability
        if (this.key.getFuzzySearchMaxValue() <= 0 && key.getFuzzySearchMaxValue() <= 0) {
            genericRecords = new AEKey2LongMap.OpenHashMap();
            genericRecords.put(this.key, this.count);
            genericRecords.put(key, amount);
            this.key = null;
            this.count = 0;
            state = CounterState.GENERIC;
        } else {
            fuzzyRecords = FuzzySearch.createMap2Long();
            fuzzyRecords.put(this.key, this.count);
            fuzzyRecords.put(key, amount);
            this.key = null;
            this.count = 0;
            state = CounterState.FUZZY;
        }
    }

    // valid IFF state == CounterState.GENERIC
    private void convertGenericToFuzzy() {
        fuzzyRecords = FuzzySearch.createMap2Long();
        fuzzyRecords.putAll(genericRecords);
        genericRecords = null;
        state = CounterState.FUZZY;
    }

    public long set(AEKey key, long amount) {
        return switch (state) {
            case EMPTY -> setEmpty(key, amount);
            case SINGLE -> setSingle(key, amount);
            case GENERIC -> setGeneric(key, amount);
            case FUZZY -> fuzzyRecords.put(key, amount);
        };
    }

    // valid IFF state == CounterState.GENERIC
    private long setGeneric(AEKey key, long amount) {
        if (key.getFuzzySearchMaxValue() > 0) {
            convertGenericToFuzzy();
            return fuzzyRecords.put(key, amount);
        } else {
            return genericRecords.put(key, amount);
        }
    }

    // valid IFF state == CounterState.EMPTY
    private long setEmpty(AEKey key, long amount) {
        this.key = key;
        this.count = amount;
        state = CounterState.SINGLE;
        return 0;
    }

    // valid IFF state == CounterState.SINGLE
    private long setSingle(AEKey key, long amount) {
        if (this.key.equals(key)) {
            long ret = count;
            count = amount;
            return ret;
        }
        addSingleDistinct(key, amount);
        return 0;
    }

    public long remove(AEKey key) {
        return switch (state) {
            case EMPTY -> 0;
            case SINGLE -> removeSingle(key);
            case GENERIC -> genericRecords.removeLong(key);
            case FUZZY -> fuzzyRecords.removeLong(key);
        };
    }

    // valid IFF state == CounterState.SINGLE
    private long removeSingle(AEKey key) {
        if (this.key.equals(key)) {
            long ret = this.count;
            this.count = 0;
            this.state = CounterState.EMPTY;
            return ret;
        }
        return 0;
    }

    public void addAll(VariantCounter other) {
        for (var entry : other) {
            add(entry.getKey(), entry.getLongValue());
        }
    }

    public void removeAll(VariantCounter other) {
        for (var entry : other) {
            add(entry.getKey(), -entry.getLongValue());
        }
    }

    public Collection<Object2LongMap.Entry<AEKey>> findFuzzy(AEKey filter, FuzzyMode fuzzy) {
        return switch (state) {
            case EMPTY -> Collections.emptyList();
            case SINGLE -> FuzzySearch.matches(key, filter, fuzzy) ? Collections.singletonList(singleton())
                    : Collections.emptyList();
            // None of the keys have durability
            case GENERIC -> FuzzySearch.matchesUndamaged(filter, fuzzy) ? genericRecords.object2LongEntrySet()
                    : Collections.emptyList();
            case FUZZY ->
                FuzzySearch.findFuzzy((Object2LongSortedMap<AEKey>) fuzzyRecords, filter, fuzzy).object2LongEntrySet();
        };
    }

    // valid IFF state == CounterState.SINGLE
    private Object2LongMap.Entry<AEKey> singleton() {
        final AEKey keyCapture = key;
        return new Object2LongMap.Entry<>() {
            @Override
            public long getLongValue() {
                return get(keyCapture);
            }

            @Override
            public long setValue(long l) {
                return VariantCounter.this.set(keyCapture, l);
            }

            @Override
            public AEKey getKey() {
                return keyCapture;
            }
        };
    }

    public int size() {
        return switch (state) {
            case EMPTY -> 0;
            case SINGLE -> 1;
            case GENERIC -> mapSize(genericRecords);
            case FUZZY -> mapSize(fuzzyRecords);
        };
    }

    private int mapSize(AEKey2LongMap records) {
        return records.size();
    }

    public boolean isEmpty() {
        return switch (state) {
            case EMPTY -> true;
            case SINGLE -> false;
            case GENERIC -> mapIsEmpty(genericRecords);
            case FUZZY -> mapIsEmpty(fuzzyRecords);
        };
    }

    private boolean mapIsEmpty(AEKey2LongMap records) {
        return records.isEmpty();
    }

    @Override
    public @NotNull Iterator<Object2LongMap.Entry<AEKey>> iterator() {
        return switch (state) {
            case EMPTY -> Collections.emptyIterator();
            case SINGLE -> new SingleIterator();
            case GENERIC -> iterator(genericRecords);
            case FUZZY -> iterator(fuzzyRecords);
        };
    }

    private Iterator<Object2LongMap.Entry<AEKey>> iterator(AEKey2LongMap records) {
        return Object2LongMaps.fastIterator(records);
    }

    @Override
    public void forEach(Consumer<? super Object2LongMap.Entry<AEKey>> action) {
        switch (state) {
            case EMPTY -> {
            }
            case SINGLE -> forEachSingle(action);
            case GENERIC -> mapForEach(genericRecords, action);
            case FUZZY -> mapForEach(fuzzyRecords, action);
        }
    }

    // valid IFF state == CounterState.SINGLE
    private void forEachSingle(Consumer<? super Object2LongMap.Entry<AEKey>> action) {
        action.accept(singleton());
    }

    private void mapForEach(AEKey2LongMap records, Consumer<? super Object2LongMap.Entry<AEKey>> action) {
        records.object2LongEntrySet().forEach(action);
    }

    /**
     * Sets all amounts to zero.
     */
    public void reset() {
        switch (state) {
            case EMPTY -> {
            }
            case SINGLE -> count = 0;
            case GENERIC -> genericRecords.replaceAll((key, value) -> 0L);
            case FUZZY -> fuzzyRecords.replaceAll((key, value) -> 0L);
        }
    }

    public void clear() {
        switch (state) {
            case EMPTY -> {
            }
            case SINGLE -> clearSingle();
            case GENERIC -> genericRecords.clear();
            case FUZZY -> fuzzyRecords.clear();
        }
    }

    private void clearSingle() {
        this.key = null;
        this.count = 0;
        this.state = CounterState.EMPTY;
    }

    public VariantCounter copy() {
        return new VariantCounter(
                state,
                key,
                count,
                genericRecords != null ? copyGenericRecords() : null,
                fuzzyRecords != null ? copyFuzzyRecords() : null);
    }

    private AEKey2LongMap.OpenHashMap copyGenericRecords() {
        AEKey2LongMap.OpenHashMap records = new AEKey2LongMap.OpenHashMap();
        records.putAll(this.genericRecords);
        return records;
    }

    private AEKey2LongMap.AVLTreeMap copyFuzzyRecords() {
        AEKey2LongMap.AVLTreeMap records = FuzzySearch.createMap2Long();
        records.putAll(this.fuzzyRecords);
        return records;
    }

    public void invert() {
        switch (state) {
            case EMPTY -> {
            }
            case SINGLE -> count = -count;
            case GENERIC -> mapInvert(genericRecords);
            case FUZZY -> mapInvert(fuzzyRecords);
        }
    }

    private void mapInvert(AEKey2LongMap records) {
        for (var entry : records.object2LongEntrySet()) {
            entry.setValue(-entry.getLongValue());
        }
    }

    public void removeZeros() {
        switch (state) {
            case EMPTY -> {
            }
            case SINGLE -> removeZerosSingle();
            case GENERIC -> mapRemoveZeros(genericRecords);
            case FUZZY -> mapRemoveZeros(fuzzyRecords);
        }
    }

    // valid IFF state == CounterState.SINGLE
    private void removeZerosSingle() {
        if (count == 0) {
            clearSingle();
        }
    }

    private void mapRemoveZeros(AEKey2LongMap records) {
        var it = records.values().iterator();
        while (it.hasNext()) {
            var entry = it.nextLong();
            if (entry == 0) {
                it.remove();
            }
        }
    }

    // valid IFF state == CounterState.SINGLE
    private class SingleIterator implements Iterator<Object2LongMap.Entry<AEKey>> {
        private final AEKey key = VariantCounter.this.key;
        private boolean consumed;
        private boolean removed;

        @Override
        public boolean hasNext() {
            return !consumed;
        }

        @Override
        public Object2LongMap.Entry<AEKey> next() {
            if (consumed) {
                throw new NoSuchElementException();
            }
            consumed = true;
            return singleton();
        }

        @Override
        public void remove() {
            if (!consumed || removed) {
                throw new IllegalStateException();
            }
            removed = true;
            VariantCounter.this.remove(key);
        }
    }
}
