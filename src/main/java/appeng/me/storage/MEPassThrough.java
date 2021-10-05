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

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;

public class MEPassThrough<T extends IAEStack> implements IMEInventoryHandler<T> {

    private final IStorageChannel<T> wrappedChannel;
    private IMEInventory<T> internal;

    public MEPassThrough(final IMEInventory<T> i, final IStorageChannel<T> channel) {
        this.wrappedChannel = channel;
        this.setInternal(i);
    }

    protected IMEInventory<T> getInternal() {
        return this.internal;
    }

    public void setInternal(final IMEInventory<T> i) {
        this.internal = i;
    }

    @Override
    public T injectItems(final T input, final Actionable type, final IActionSource src) {
        return this.internal.injectItems(input, type, src);
    }

    @Override
    public T extractItems(final T request, final Actionable type, final IActionSource src) {
        return this.internal.extractItems(request, type, src);
    }

    @Override
    public IAEStackList<T> getAvailableItems(final IAEStackList<T> out) {
        return this.internal.getAvailableItems(out);
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.internal.getChannel();
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    IStorageChannel<T> getWrappedChannel() {
        return this.wrappedChannel;
    }
}
