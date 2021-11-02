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
import java.util.Map.Entry;

import javax.annotation.Nullable;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.util.inv.ItemListIgnoreCrafting;

/**
 * Wraps another inventory transparently but allows switching out the underlying inventory implementation. When the
 * underlying inventory is switched, the resulting changes in the observable inventory are reported to listeners of this
 * monitor.
 * <p/>
 * Additionally, the crafting flag is cleared for any exposed item, because crafting requests across networks are not
 * supported.
 */
public class MEMonitorPassThrough<T extends IAEStack> extends MEPassThrough<T>
        implements IMEMonitor<T>, IMEMonitorListener<T> {

    private final HashMap<IMEMonitorListener<T>, Object> listeners = new HashMap<>();

    @Nullable
    private IActionSource changeSource;

    @Nullable
    private IMEMonitor<T> monitor;

    public MEMonitorPassThrough(IMEInventory<T> i) {
        super(i);
    }

    @Override
    public void setInternal(IMEInventory<T> i) {
        if (this.monitor != null) {
            this.monitor.removeListener(this);
            this.monitor = null;
        }

        var before = this.getAvailableStacks(getChannel().createList());

        super.setInternal(i);
        if (i instanceof IMEMonitor) {
            this.monitor = (IMEMonitor<T>) i;
        }

        var after = this.getAvailableStacks(getChannel().createList());

        if (this.monitor != null) {
            this.monitor.addListener(this, this.monitor);
        }

        StorageHelper.postListChanges(before, after, this, changeSource);
    }

    @Override
    public IAEStackList<T> getAvailableStacks(IAEStackList<T> out) {
        // Note how ItemListIgnoreCrafting will clear the crafting flag.
        // This is used because crafting items from other networks is not possible.
        super.getAvailableStacks(new ItemListIgnoreCrafting<>(out));
        return out;
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
    public IAEStackList<T> getCachedAvailableStacks() {
        if (this.monitor == null) {
            return this.getAvailableStacks(getChannel().createList());
        }
        return this.monitor.getCachedAvailableStacks();
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return verificationToken == this.monitor;
    }

    @Override
    public void postChange(IMEMonitor<T> monitor, final Iterable<T> change, final IActionSource source) {
        final Iterator<Entry<IMEMonitorListener<T>, Object>> i = this.listeners.entrySet().iterator();
        while (i.hasNext()) {
            final Entry<IMEMonitorListener<T>, Object> e = i.next();
            final IMEMonitorListener<T> receiver = e.getKey();
            if (receiver.isValid(e.getValue())) {
                receiver.postChange(this, change, source);
            } else {
                i.remove();
            }
        }
    }

    @Override
    public void onListUpdate() {
        final Iterator<Entry<IMEMonitorListener<T>, Object>> i = this.listeners.entrySet().iterator();
        while (i.hasNext()) {
            final Entry<IMEMonitorListener<T>, Object> e = i.next();
            final IMEMonitorListener<T> receiver = e.getKey();
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
}
