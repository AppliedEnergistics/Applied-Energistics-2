package appeng.api.storage.data;

import java.util.*;

import com.google.common.collect.Iterators;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;

/**
 * List of generic IAEStacks.
 */
public final class MixedItemList implements Iterable<IAEStack> {
    private final Map<IStorageChannel<?>, IItemList<?>> lists = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends IAEStack> IItemList<T> getList(IStorageChannel<T> channel) {
        return (IItemList<T>) lists.computeIfAbsent(channel, IStorageChannel::createList);
    }

    private IItemList getList(IAEStack stack) {
        return getList(stack.getChannel());
    }

    public void add(IAEStack stack) {
        if (stack != null) {
            getList(stack).add(stack);
        }
    }

    public void addCrafting(IAEStack stack) {
        if (stack != null) {
            getList(stack).addCrafting(stack);
        }
    }

    public void addStorage(IAEStack stack) {
        if (stack != null) {
            getList(stack).addStorage(stack);
        }
    }

    public void addRequestable(IAEStack stack) {
        if (stack != null) {
            getList(stack).addRequestable(stack);
        }
    }

    public IAEStack findPrecise(IAEStack stack) {
        if (stack != null) {
            return getList(stack).findPrecise(stack);
        } else {
            return null;
        }
    }

    public Collection<IAEStack> findFuzzy(IAEStack stack, FuzzyMode fuzzy) {
        if (stack != null) {
            return getList(stack).findFuzzy(stack, fuzzy);
        } else {
            return Collections.emptyList();
        }
    }

    public boolean isEmpty() {
        for (IItemList<?> list : lists.values()) {
            if (!list.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        int tot = 0;
        for (IItemList<?> list : lists.values()) {
            tot += list.size();
        }
        return tot;
    }

    public void resetStatus() {
        lists.values().forEach(IItemList::resetStatus);
    }

    @Override
    public Iterator<IAEStack> iterator() {
        return Iterators.concat(
                Iterators.transform(lists.values().iterator(), IItemList::iterator));
    }
}
