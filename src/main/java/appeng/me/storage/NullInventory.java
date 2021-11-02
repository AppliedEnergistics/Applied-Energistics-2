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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;

/**
 * An immutable inventory that is empty.
 */
public class NullInventory<T extends IAEStack> implements IMEInventory<T> {
    private static final Map<IStorageChannel<?>, NullInventory<?>> NULL_INVENTORIES = new ConcurrentHashMap<>();

    private final IStorageChannel<T> storageChannel;

    private NullInventory(IStorageChannel<T> storageChannel) {
        this.storageChannel = storageChannel;
    }

    @SuppressWarnings("unchecked")
    public static <T extends IAEStack> IMEInventory<T> of(IStorageChannel<T> channel) {
        return (IMEInventory<T>) NULL_INVENTORIES.computeIfAbsent(channel, NullInventory::new);
    }

    @Override
    public T injectItems(final T input, final Actionable mode, final IActionSource src) {
        return input;
    }

    @Override
    public T extractItems(final T request, final Actionable mode, final IActionSource src) {
        return null;
    }

    @Override
    public IAEStackList<T> getAvailableStacks(IAEStackList<T> out) {
        return out;
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return storageChannel;
    }
}
