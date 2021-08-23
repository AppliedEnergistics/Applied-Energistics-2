package appeng.util;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public final class StackList {
    private final Map<IStorageChannel<?>, IItemList<?>> chans = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    private <T extends IAEStack<T>> IItemList<T> getList(T stack) {
        return (IItemList<T>) chans.computeIfAbsent(stack.getChannel(), IStorageChannel::createList);
    }

    public <T extends IAEStack<T>> void add(T stack) {
        getList(stack).add(stack);
    }

    public <T extends IAEStack<T>> T findPrecise(T stack) {
        return getList(stack).findPrecise(stack);
    }

    public <T extends IAEStack<T>> Collection<T> findFuzzy(T stack, FuzzyMode fuzzy) {
        return getList(stack).findFuzzy(stack, fuzzy);
    }

    public boolean isEmpty() {
        for (IItemList<?> list : chans.values()) {
            if (!list.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
