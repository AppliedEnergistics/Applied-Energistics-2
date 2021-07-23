/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;

/**
 * Stores variants of a single type of {@link net.minecraft.world.item.Item}, i.e. versions with different durability, or
 * different NBT or capabilities.
 */
abstract class ItemVariantList {

    public void add(final IAEItemStack option) {
        final IAEItemStack st = this.getRecords().get(((AEItemStack) option).getSharedStack());

        if (st != null) {
            st.add(option);
            return;
        }

        final IAEItemStack opt = option.copy();

        this.putItemRecord(opt);
    }

    public IAEItemStack findPrecise(final IAEItemStack itemStack) {
        return this.getRecords().get(((AEItemStack) itemStack).getSharedStack());
    }

    public void addStorage(final IAEItemStack option) {
        final IAEItemStack st = this.getRecords().get(((AEItemStack) option).getSharedStack());

        if (st != null) {
            st.incStackSize(option.getStackSize());
            return;
        }

        final IAEItemStack opt = option.copy();

        this.putItemRecord(opt);
    }

    public void addCrafting(final IAEItemStack option) {
        final IAEItemStack st = this.getRecords().get(((AEItemStack) option).getSharedStack());

        if (st != null) {
            st.setCraftable(true);
            return;
        }

        final IAEItemStack opt = option.copy();
        opt.setStackSize(0);
        opt.setCraftable(true);

        this.putItemRecord(opt);
    }

    public void addRequestable(final IAEItemStack option) {
        final IAEItemStack st = this.getRecords().get(((AEItemStack) option).getSharedStack());

        if (st != null) {
            st.setCountRequestable(st.getCountRequestable() + option.getCountRequestable());
            return;
        }

        final IAEItemStack opt = option.copy();
        opt.setStackSize(0);
        opt.setCraftable(false);
        opt.setCountRequestable(option.getCountRequestable());

        this.putItemRecord(opt);
    }

    public int size() {
        int size = 0;
        for (IAEItemStack entry : getRecords().values()) {
            if (entry.isMeaningful()) {
                size++;
            }
        }

        return size;
    }

    public Iterator<IAEItemStack> iterator() {
        return new MeaningfulItemIterator<>(this.getRecords().values());
    }

    private void putItemRecord(final IAEItemStack itemStack) {
        this.getRecords().put(((AEItemStack) itemStack).getSharedStack(), itemStack);
    }

    abstract Map<AESharedItemStack, IAEItemStack> getRecords();

    public abstract Collection<IAEItemStack> findFuzzy(final IAEItemStack filter, final FuzzyMode fuzzy);

}
