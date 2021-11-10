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

import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

/**
 * An immutable inventory that is empty.
 */
public class NullInventory<T extends AEKey> implements IMEInventory<T> {
    private static final Map<IStorageChannel<?>, NullInventory<?>> NULL_INVENTORIES = new ConcurrentHashMap<>();

    private final IStorageChannel<T> storageChannel;

    private NullInventory(IStorageChannel<T> storageChannel) {
        this.storageChannel = storageChannel;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AEKey> IMEInventory<T> of(IStorageChannel<T> channel) {
        return (IMEInventory<T>) NULL_INVENTORIES.computeIfAbsent(channel, NullInventory::new);
    }

    @Override
    public void getAvailableStacks(KeyCounter<T> out) {
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return storageChannel;
    }
}
