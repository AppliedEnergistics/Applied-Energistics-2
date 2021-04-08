package appeng.util.item;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import appeng.api.storage.data.IAEStack;

public class IncrementalUpdateHelper<T extends IAEStack<T>> implements Iterable<T> {

    /**
     * Maps stacks to serial numbers. This relies on the fact that these stacks are equal iff their type is equal, and
     * two stacks with different counts are still equal.
     */
    private final BiMap<T, Long> mapping;

    private final Set<T> changes = new HashSet<>();

    private long serial;

    /**
     * Indicates that a full update should be sent.
     */
    private boolean fullUpdate = true;

    public IncrementalUpdateHelper() {
        this.mapping = HashBiMap.create();
    }

    @Nullable
    public Long getSerial(T stack) {
        return mapping.get(stack);
    }

    public long getOrAssignSerial(T stack) {
        return mapping.computeIfAbsent(stack, key -> ++this.serial);
    }

    public T getBySerial(long serial) {
        return mapping.inverse().get(serial);
    }

    public void clear() {
        this.mapping.clear();
        this.changes.clear();
        fullUpdate = true;
    }

    public void addChange(T entry) {
        if (!changes.add(entry)) {
            changes.remove(entry);
            changes.add(entry);
        }
    }

    public void commitChanges() {
        // Stacks that have become empty will lose their mapping to avoid leaking memory in the mapping over time
        for (T stack : changes) {
            if (!stack.isMeaningful()) {
                mapping.remove(stack);
            }
        }
        changes.clear();
        fullUpdate = false;
    }

    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    public boolean isFullUpdate() {
        return fullUpdate;
    }

    @Override
    public Iterator<T> iterator() {
        return changes.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        changes.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return changes.spliterator();
    }

}
