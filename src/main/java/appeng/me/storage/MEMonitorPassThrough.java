/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.me.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

/**
 * Wraps another inventory transparently but allows switching out the underlying inventory implementation. When the
 * underlying inventory is switched, the resulting changes in the observable inventory are reported to listeners of this
 * monitor.
 */
public class MEMonitorPassThrough implements MEMonitorStorage, IMEMonitorListener {

    private final Map<IMEMonitorListener, Object> listeners = new HashMap<>();

    @Nullable
    private IActionSource changeSource;

    @Nullable
    private MEMonitorStorage monitor;

    public void setMonitor(@Nullable MEMonitorStorage monitor) {
        if (this.monitor != null) {
            this.monitor.removeListener(this);
            this.monitor = null;
        }

        var before = this.getAvailableStacks();

        this.monitor = monitor;

        var after = this.getAvailableStacks();

        if (this.monitor != null) {
            this.monitor.addListener(this, this.monitor);
        }

        StorageHelper.postListChanges(before, after, this, changeSource);
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
    public boolean isValid(final Object verificationToken) {
        return verificationToken == this.monitor;
    }

    @Override
    public void postChange(MEMonitorStorage monitor, Iterable<AEKey> change, final IActionSource source) {
        final Iterator<Entry<IMEMonitorListener, Object>> i = this.listeners.entrySet().iterator();
        while (i.hasNext()) {
            final Entry<IMEMonitorListener, Object> e = i.next();
            final IMEMonitorListener receiver = e.getKey();
            if (receiver.isValid(e.getValue())) {
                receiver.postChange(this, change, source);
            } else {
                i.remove();
            }
        }
    }

    @Override
    public void onListUpdate() {
        var i = this.listeners.entrySet().iterator();
        while (i.hasNext()) {
            final Entry<IMEMonitorListener, Object> e = i.next();
            final IMEMonitorListener receiver = e.getKey();
            if (receiver.isValid(e.getValue())) {
                receiver.onListUpdate();
            } else {
                i.remove();
            }
        }
    }

    public void setChangeSource(@Nullable IActionSource changeSource) {
        this.changeSource = changeSource;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return monitor != null ? monitor.insert(what, amount, mode, source) : 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return monitor != null ? monitor.extract(what, amount, mode, source) : 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (monitor != null) {
            monitor.getAvailableStacks(out);
        }
    }

    @Override
    public KeyCounter getCachedAvailableStacks() {
        if (this.monitor == null) {
            return new KeyCounter();
        }
        return this.monitor.getCachedAvailableStacks();
    }
}
