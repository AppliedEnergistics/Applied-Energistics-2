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
     * Enable to skip and remove keys that are mapped to zero.
     */
    private boolean dropZeros;

    /**
     * The current state of this counter, determining which fields are valid and contain item information.
     */
    enum CounterState {
        EMPTY, // zero types
        SINGLE, // one type
        GENERIC, // 2+ stacks that do not have durability
        FUZZY, // 2+ stacks that have durability
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
            boolean dropZeros,
            CounterState state,
            AEKey key,
            long count,
            AEKey2LongMap.OpenHashMap genericRecords,
            AEKey2LongMap.AVLTreeMap fuzzyRecords) {
        this.dropZeros = dropZeros;
        this.state = state;
        this.key = key;
        this.count = count;
        this.genericRecords = genericRecords;
        this.fuzzyRecords = fuzzyRecords;
    }

    public boolean isDropZeros() {
        return dropZeros;
    }

    public void setDropZeros(boolean dropZeros) {
        this.dropZeros = dropZeros;
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
            case GENERIC -> genericRecords.addTo(key, amount);
            case FUZZY -> fuzzyRecords.addTo(key, amount);
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
        if (this.key.getFuzzySearchMaxValue() <= 0) {
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

    public long set(AEKey key, long amount) {
        if (dropZeros && amount == 0) {
            return remove(key);
        } else {
            return switch (state) {
                case EMPTY -> setEmpty(key, amount);
                case SINGLE -> setSingle(key, amount);
                case GENERIC -> genericRecords.put(key, amount);
                case FUZZY -> fuzzyRecords.put(key, amount);
            };
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
            case SINGLE ->
                key.fuzzyEquals(filter, fuzzy) ? Collections.singletonList(singleton()) : Collections.emptyList();
            case GENERIC -> genericRecords.object2LongEntrySet();
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
            case SINGLE -> dropZeros && count == 0 ? 0 : 1;
            case GENERIC -> mapSize(genericRecords);
            case FUZZY -> mapSize(fuzzyRecords);
        };
    }

    private int mapSize(AEKey2LongMap records) {
        if (!dropZeros) {
            return records.size();
        }
        var size = 0;
        for (var value : records.values()) {
            if (value != 0) {
                size++;
            }
        }
        return size;
    }

    public boolean isEmpty() {
        return switch (state) {
            case EMPTY -> true;
            case SINGLE -> dropZeros && count == 0;
            case GENERIC -> mapIsEmpty(genericRecords);
            case FUZZY -> mapIsEmpty(fuzzyRecords);
        };
    }

    private boolean mapIsEmpty(AEKey2LongMap records) {
        if (!dropZeros) {
            return records.isEmpty();
        }
        for (var value : records.values()) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull Iterator<Object2LongMap.Entry<AEKey>> iterator() {
        return switch (state) {
            case EMPTY -> Collections.emptyIterator();
            case SINGLE -> dropZeros && count == 0 ? Collections.emptyIterator()
                    : Collections.singletonList(singleton()).iterator();
            case GENERIC -> iterator(genericRecords);
            case FUZZY -> iterator(fuzzyRecords);
        };
    }

    private Iterator<Object2LongMap.Entry<AEKey>> iterator(AEKey2LongMap records) {
        var it = Object2LongMaps.fastIterator(records);
        if (!dropZeros) {
            return it;
        }
        return new NonDefaultIterator(it);
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
        if (!dropZeros || count != 0) {
            action.accept(singleton());
        }
    }

    private void mapForEach(AEKey2LongMap records, Consumer<? super Object2LongMap.Entry<AEKey>> action) {
        records.object2LongEntrySet().forEach(dropZeros ? new NonDefaultConsumer(action) : action);
    }

    /**
     * Sets all amounts to zero.
     */
    public void reset() {
        if (dropZeros) {
            clear();
        } else {
            switch (state) {
                case EMPTY -> {
                }
                case SINGLE -> count = 0;
                case GENERIC -> genericRecords.replaceAll((key, value) -> 0L);
                case FUZZY -> fuzzyRecords.replaceAll((key, value) -> 0L);
            }
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
                dropZeros,
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

    /**
     * Only returns entries that do not have amount 0.
     */
    private static class NonDefaultIterator implements Iterator<Object2LongMap.Entry<AEKey>> {
        private final Iterator<Object2LongMap.Entry<AEKey>> parent;
        private Object2LongMap.Entry<AEKey> next;

        public NonDefaultIterator(Iterator<Object2LongMap.Entry<AEKey>> parent) {
            this.parent = parent;
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

    private record NonDefaultConsumer(
            Consumer<? super Object2LongMap.Entry<AEKey>> action) implements Consumer<Object2LongMap.Entry<AEKey>> {

        @Override
        public void accept(Object2LongMap.Entry<AEKey> entry) {
            if (entry.getLongValue() != 0) {
                action.accept(entry);
            }
        }
    }
}
