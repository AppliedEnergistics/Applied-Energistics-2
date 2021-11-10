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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Iterators;

import appeng.api.config.FuzzyMode;
import appeng.util.item.VariantMap;

/**
 * Associates a generic value of type T with AE keys and makes key/value pairs searchable with fuzzy mode semantics.
 */
public class KeyMap<K extends AEKey, V> implements Iterable<Map.Entry<K, V>> {
    private final V defaultValue;
    private final Supplier<V> defaultValueFactory;

    public KeyMap(V defaultValue, Supplier<V> defaultValueFactory) {
        this.defaultValue = defaultValue;
        this.defaultValueFactory = defaultValueFactory;
    }

    // First map contains a mapping from AEKey#primaryKey
    private final Map<Object, VariantMap<K, V>> lists = new IdentityHashMap<>();

    public V mapping(K key) {
        return getSubIndex(key).mapping(key);
    }

    public V findPrecise(K key) {
        return getSubIndex(key).findPrecise(key);
    }

    public Collection<Map.Entry<K, V>> findFuzzy(K key, FuzzyMode fuzzy) {
        return getSubIndex(key).findFuzzy(key, fuzzy);
    }

    public boolean isEmpty() {
        for (var list : lists.values()) {
            if (!list.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        int tot = 0;
        for (var list : lists.values()) {
            tot += list.size();
        }
        return tot;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return Iterators.concat(
                Iterators.transform(lists.values().iterator(), VariantMap::iterator));
    }

    private VariantMap<K, V> getSubIndex(K key) {
        var subIndex = lists.get(key.getPrimaryKey());
        if (subIndex == null) {
            subIndex = VariantMap.create(key, defaultValue, defaultValueFactory);
            lists.put(key, subIndex);
        }
        return subIndex;
    }
}
