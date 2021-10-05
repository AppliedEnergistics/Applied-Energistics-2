/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.storage.data;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Iterators;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;

/**
 * List of generic IAEStacks.
 */
public final class MixedStackList implements Iterable<IAEStack> {
    private final Map<IStorageChannel<?>, IAEStackList<?>> lists = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends IAEStack> IAEStackList<T> getList(IStorageChannel<T> channel) {
        return (IAEStackList<T>) lists.computeIfAbsent(channel, IStorageChannel::createList);
    }

    private IAEStackList getList(IAEStack stack) {
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
        for (IAEStackList<?> list : lists.values()) {
            if (!list.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        int tot = 0;
        for (IAEStackList<?> list : lists.values()) {
            tot += list.size();
        }
        return tot;
    }

    public void resetStatus() {
        lists.values().forEach(IAEStackList::resetStatus);
    }

    @Override
    public Iterator<IAEStack> iterator() {
        return Iterators.concat(
                Iterators.transform(lists.values().iterator(), IAEStackList::iterator));
    }
}
