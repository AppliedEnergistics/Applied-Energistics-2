/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.util.fluid;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.util.iterators.MeaningfulStackIterator;

public final class FluidList implements IAEStackList<IAEFluidStack> {

    private final Map<IAEFluidStack, IAEFluidStack> records = new HashMap<>();

    @Override
    public void add(final IAEFluidStack option) {
        if (option == null) {
            return;
        }

        final IAEFluidStack st = this.getFluidRecord(option);

        if (st != null) {
            IAEStack.add(st, option);
            return;
        }

        final IAEFluidStack opt = option.copy();

        this.putFluidRecord(opt);
    }

    @Override
    public IAEFluidStack findPrecise(final IAEFluidStack fluidStack) {
        if (fluidStack == null) {
            return null;
        }

        return this.getFluidRecord(fluidStack);
    }

    @Override
    public Collection<IAEFluidStack> findFuzzy(final IAEFluidStack filter, final FuzzyMode fuzzy) {
        var precise = findPrecise(filter);
        if (precise == null) {
            return Collections.emptyList();
        } else {
            return List.of(precise);
        }
    }

    @Override
    public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    @Override
    public void addStorage(final IAEFluidStack option) {
        if (option == null) {
            return;
        }

        final IAEFluidStack st = this.getFluidRecord(option);

        if (st != null) {
            st.incStackSize(option.getStackSize());
            return;
        }

        final IAEFluidStack opt = option.copy();

        this.putFluidRecord(opt);
    }

    /*
     * public synchronized void clean() { Iterator<StackType> i = iterator(); while (i.hasNext()) { StackType AEI =
     * i.next(); if ( !AEI.isMeaningful() ) i.remove(); } }
     */

    @Override
    public void addCrafting(final IAEFluidStack option) {
        if (option == null) {
            return;
        }

        final IAEFluidStack st = this.getFluidRecord(option);

        if (st != null) {
            st.setCraftable(true);
            return;
        }

        final IAEFluidStack opt = option.copy();
        opt.setStackSize(0);
        opt.setCraftable(true);

        this.putFluidRecord(opt);
    }

    @Override
    public void addRequestable(final IAEFluidStack option) {
        if (option == null) {
            return;
        }

        final IAEFluidStack st = this.getFluidRecord(option);

        if (st != null) {
            st.setCountRequestable(st.getCountRequestable() + option.getCountRequestable());
            return;
        }

        final IAEFluidStack opt = option.copy();
        opt.setStackSize(0);
        opt.setCraftable(false);
        opt.setCountRequestable(option.getCountRequestable());

        this.putFluidRecord(opt);
    }

    @Override
    public IAEFluidStack getFirstItem() {
        for (final IAEFluidStack stackType : this) {
            return stackType;
        }

        return null;
    }

    @Override
    public int size() {
        return this.records.values().size();
    }

    @Override
    public Iterator<IAEFluidStack> iterator() {
        return new MeaningfulStackIterator<>(this.records.values());
    }

    @Override
    public void resetStatus() {
        for (final IAEFluidStack i : this) {
            i.reset();
        }
    }

    private IAEFluidStack getFluidRecord(final IAEFluidStack fluid) {
        return this.records.get(fluid);
    }

    private IAEFluidStack putFluidRecord(final IAEFluidStack fluid) {
        return this.records.put(fluid, fluid);
    }
}
