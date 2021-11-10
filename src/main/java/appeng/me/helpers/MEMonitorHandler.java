/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
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

package appeng.me.helpers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

/**
 * Common implementation of a simple class that monitors injection/extraction of a inventory to send events to a list of
 * listeners.
 * <p>
 * TODO: Needs to be redesigned to solve performance issues.
 */
public class MEMonitorHandler<T extends AEKey> implements IMEMonitor<T> {

    private final IMEInventory<T> internalHandler;
    private final KeyCounter<T> cachedList;
    private final HashMap<IMEMonitorListener<T>, Object> listeners = new HashMap<>();

    protected boolean hasChanged = true;

    public MEMonitorHandler(IMEInventory<T> t) {
        this.internalHandler = t;
        this.cachedList = new KeyCounter<>();
    }

    @Override
    public void addListener(final IMEMonitorListener<T> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorListener<T> l) {
        this.listeners.remove(l);
    }

    @Override
    public long insert(T what, long amount, Actionable mode, IActionSource source) {
        var inserted = internalHandler.insert(what, amount, mode, source);
        if (mode == Actionable.MODULATE) {
            monitorDifference(what, inserted, source);
        }
        return inserted;
    }

    @Override
    public long extract(T what, long amount, Actionable mode, IActionSource source) {
        var extracted = internalHandler.extract(what, amount, mode, source);
        if (mode == Actionable.MODULATE) {
            monitorDifference(what, -extracted, source);
        }
        return extracted;
    }

    protected IMEInventory<T> getHandler() {
        return this.internalHandler;
    }

    private void monitorDifference(T what, long difference, IActionSource source) {
        if (difference != 0) {
            this.postChangesToListeners(ImmutableList.of(what), source);
        }
    }

    protected void postChangesToListeners(final Iterable<T> changes, final IActionSource src) {
        this.notifyListenersOfChange(changes, src);
    }

    protected void notifyListenersOfChange(final Iterable<T> diff, final IActionSource src) {
        this.hasChanged = true;// need to update the cache.
        final Iterator<Entry<IMEMonitorListener<T>, Object>> i = this.getListeners();
        while (i.hasNext()) {
            final Entry<IMEMonitorListener<T>, Object> o = i.next();
            final IMEMonitorListener<T> receiver = o.getKey();
            if (receiver.isValid(o.getValue())) {
                receiver.postChange(this, diff, src);
            } else {
                i.remove();
            }
        }
    }

    protected Iterator<Entry<IMEMonitorListener<T>, Object>> getListeners() {
        return this.listeners.entrySet().iterator();
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.getHandler().getChannel();
    }

    @Override
    public KeyCounter<T> getCachedAvailableStacks() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.reset();
            this.getAvailableStacks(this.cachedList);
            this.cachedList.removeZeros();
        }

        return this.cachedList;
    }

    @Override
    public void getAvailableStacks(final KeyCounter<T> out) {
        this.getHandler().getAvailableStacks(out);
    }

}
