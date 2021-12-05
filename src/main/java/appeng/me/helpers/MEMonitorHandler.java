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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.MEStorage;

/**
 * Common implementation of a simple class that monitors injection/extraction of a inventory to send events to a list of
 * listeners.
 * <p>
 * TODO: Needs to be redesigned to solve performance issues.
 */
public class MEMonitorHandler implements MEMonitorStorage {

    private final MEStorage internalHandler;
    private final KeyCounter cachedList;
    private final HashMap<IMEMonitorListener, Object> listeners = new HashMap<>();

    protected boolean hasChanged = true;

    public MEMonitorHandler(MEStorage t) {
        this.internalHandler = t;
        this.cachedList = new KeyCounter();
    }

    @Override
    public void addListener(final IMEMonitorListener l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorListener l) {
        this.listeners.remove(l);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        var inserted = internalHandler.insert(what, amount, mode, source);
        if (mode == Actionable.MODULATE) {
            monitorDifference(what, inserted, source);
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        var extracted = internalHandler.extract(what, amount, mode, source);
        if (mode == Actionable.MODULATE) {
            monitorDifference(what, -extracted, source);
        }
        return extracted;
    }

    protected MEStorage getHandler() {
        return this.internalHandler;
    }

    private void monitorDifference(AEKey what, long difference, IActionSource source) {
        if (difference != 0) {
            this.postChangesToListeners(Collections.singleton(what), source);
        }
    }

    protected void postChangesToListeners(Set<AEKey> changes, final IActionSource src) {
        this.notifyListenersOfChange(changes, src);
    }

    protected void notifyListenersOfChange(Set<AEKey> diff, final IActionSource src) {
        this.hasChanged = true;// need to update the cache.
        MEMonitorStorage.postDifference(this, listeners, diff, src);
    }

    protected Iterator<Entry<IMEMonitorListener, Object>> getListeners() {
        return this.listeners.entrySet().iterator();
    }

    @Override
    public KeyCounter getCachedAvailableStacks() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.reset();
            this.getAvailableStacks(this.cachedList);
            this.cachedList.removeZeros();
        }

        return this.cachedList;
    }

    @Override
    public void getAvailableStacks(final KeyCounter out) {
        this.getHandler().getAvailableStacks(out);
    }

    @Override
    public Component getDescription() {
        return getHandler().getDescription();
    }

}
