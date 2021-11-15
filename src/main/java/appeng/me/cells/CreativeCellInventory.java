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

package appeng.me.cells;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.items.contents.CellConfig;

class CreativeCellInventory<T extends AEKey> implements ICellInventory<T> {
    private final Set<T> configured;
    private final IStorageChannel<T> channel;

    protected CreativeCellInventory(IStorageChannel<T> channel, ItemStack o) {
        this.channel = channel;
        this.configured = new HashSet<>();

        var cc = CellConfig.create(channel, o);
        configured.addAll(cc.keySet());
    }

    @Override
    public long insert(T what, long amount, Actionable mode, IActionSource source) {
        return configured.contains(what) ? amount : 0;
    }

    @Override
    public long extract(T what, long amount, Actionable mode, IActionSource source) {
        return configured.contains(what) ? amount : 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter<T> out) {
        for (var key : this.configured) {
            out.add(key, Integer.MAX_VALUE);
        }
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return channel;
    }

    @Override
    public boolean isPreferredStorageFor(T input, IActionSource source) {
        return this.configured.contains(input);
    }

    @Override
    public CellState getStatus() {
        return CellState.TYPES_FULL;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public void persist() {
    }
}
