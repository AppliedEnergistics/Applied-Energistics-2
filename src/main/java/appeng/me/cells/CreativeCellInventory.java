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

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IConfigurableMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.items.contents.CellConfig;

class CreativeCellInventory<T extends IAEStack> implements IConfigurableMEInventory<T> {
    private final IAEStackList<T> cache;
    private final IStorageChannel<T> channel;

    protected CreativeCellInventory(IStorageChannel<T> channel, ItemStack o) {
        this.channel = channel;
        this.cache = channel.createList();

        var cc = new CellConfig(o);
        for (var is : cc) {
            var i = channel.createStack(is);
            if (i != null) {
                i.setStackSize(Integer.MAX_VALUE);
                this.cache.add(i);
            }
        }
    }

    @Override
    public T injectItems(final T input, final Actionable mode, final IActionSource src) {
        var local = this.cache.findPrecise(input);
        if (local == null) {
            return input;
        }

        return null;
    }

    @Override
    public T extractItems(final T request, final Actionable mode, final IActionSource src) {
        var local = this.cache.findPrecise(request);
        if (local == null) {
            return null;
        }

        return IAEStack.copy(request);
    }

    @Override
    public IAEStackList<T> getAvailableStacks(IAEStackList<T> out) {
        for (var ais : this.cache) {
            out.add(ais);
        }
        return out;
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return channel;
    }

    @Override
    public boolean isPreferredStorageFor(T input, IActionSource source) {
        return this.cache.findPrecise(input) != null;
    }

    @Override
    public boolean canAccept(T input) {
        return this.cache.findPrecise(input) != null;
    }

}
