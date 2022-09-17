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

package appeng.util.item;


import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemContainer;

import java.util.Collection;


public class ItemModList implements IItemContainer<IAEItemStack> {

    private final IItemContainer<IAEItemStack> backingStore;
    private final IItemContainer<IAEItemStack> overrides = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

    public ItemModList(final IItemContainer<IAEItemStack> backend) {
        this.backingStore = backend;
    }

    @Override
    public void add(final IAEItemStack option) {
        IAEItemStack over = this.overrides.findPrecise(option);
        if (over == null) {
            over = this.backingStore.findPrecise(option);
            if (over == null) {
                this.overrides.add(option);
            } else {
                option.add(over);
                this.overrides.add(option);
            }
        } else {
            this.overrides.add(option);
        }
    }

    @Override
    public IAEItemStack findPrecise(final IAEItemStack i) {
        final IAEItemStack over = this.overrides.findPrecise(i);
        if (over == null) {
            return this.backingStore.findPrecise(i);
        }
        return over;
    }

    @Override
    public Collection<IAEItemStack> findFuzzy(final IAEItemStack input, final FuzzyMode fuzzy) {
        return this.overrides.findFuzzy(input, fuzzy);
    }

    @Override
    public boolean isEmpty() {
        return this.overrides.isEmpty() && this.backingStore.isEmpty();
    }
}
