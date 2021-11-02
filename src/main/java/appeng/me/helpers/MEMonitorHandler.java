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

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;

/**
 * Common implementation of a simple class that monitors injection/extraction of a inventory to send events to a list of
 * listeners.
 * <p>
 * TODO: Needs to be redesigned to solve performance issues.
 */
public class MEMonitorHandler<T extends IAEStack> implements IMEMonitor<T> {

    private final IMEInventoryHandler<T> internalHandler;
    private final IAEStackList<T> cachedList;
    private final HashMap<IMEMonitorListener<T>, Object> listeners = new HashMap<>();

    protected boolean hasChanged = true;

    public MEMonitorHandler(final IMEInventoryHandler<T> t) {
        this.internalHandler = t;
        this.cachedList = t.getChannel().createList();
    }

    public MEMonitorHandler(final IMEInventoryHandler<T> t, final IStorageChannel<T> chan) {
        this.internalHandler = t;
        this.cachedList = chan.createList();
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
    public T injectItems(final T input, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.SIMULATE) {
            return this.getHandler().injectItems(input, mode, src);
        }
        return this.monitorDifference(IAEStack.copy(input), this.getHandler().injectItems(input, mode, src), false,
                src);
    }

    protected IMEInventoryHandler<T> getHandler() {
        return this.internalHandler;
    }

    private T monitorDifference(final T original, final T leftOvers, final boolean extraction,
            final IActionSource src) {
        final T diff = IAEStack.copy(original);

        if (extraction) {
            diff.setStackSize(leftOvers == null ? 0 : -leftOvers.getStackSize());
        } else if (leftOvers != null) {
            diff.decStackSize(leftOvers.getStackSize());
        }

        if (diff.getStackSize() != 0) {
            this.postChangesToListeners(ImmutableList.of(diff), src);
        }

        return leftOvers;
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
    public T extractItems(final T request, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.SIMULATE) {
            return this.getHandler().extractItems(request, mode, src);
        }
        return this.monitorDifference(IAEStack.copy(request), this.getHandler().extractItems(request, mode, src), true,
                src);
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.getHandler().getChannel();
    }

    @Override
    public AccessRestriction getAccess() {
        return this.getHandler().getAccess();
    }

    @Override
    public IAEStackList<T> getCachedAvailableStacks() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.resetStatus();
            return this.getAvailableStacks(this.cachedList);
        }

        return this.cachedList;
    }

    @Override
    public boolean isPrioritized(final T input) {
        return this.getHandler().isPrioritized(input);
    }

    @Override
    public boolean canAccept(final T input) {
        return this.getHandler().canAccept(input);
    }

    @Override
    public IAEStackList<T> getAvailableStacks(final IAEStackList<T> out) {
        return this.getHandler().getAvailableStacks(out);
    }

    @Override
    public int getPriority() {
        return this.getHandler().getPriority();
    }

    @Override
    public boolean validForPass(final int pass) {
        return this.getHandler().validForPass(pass);
    }

}
