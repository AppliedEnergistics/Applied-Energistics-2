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

package appeng.util.inv;


import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

import java.util.Collection;
import java.util.Iterator;


public class ItemListIgnoreCrafting<T extends IAEStack<T>> implements IItemList<T> {

    private final IItemList<T> target;

    public ItemListIgnoreCrafting(final IItemList<T> cla) {
        this.target = cla;
    }

    @Override
    public void add(T option) {
        if (option != null && option.isCraftable()) {
            option = option.copy();
            option.setCraftable(false);
        }

        this.target.add(option);
    }

    @Override
    public T findPrecise(final T i) {
        return this.target.findPrecise(i);
    }

    @Override
    public Collection<T> findFuzzy(final T input, final FuzzyMode fuzzy) {
        return this.target.findFuzzy(input, fuzzy);
    }

    @Override
    public boolean isEmpty() {
        return this.target.isEmpty();
    }

    @Override
    public void addStorage(final T option) {
        this.target.addStorage(option);
    }

    @Override
    public void addCrafting(final T option) {
        // nothing.
    }

    @Override
    public void addRequestable(final T option) {
        this.target.addRequestable(option);
    }

    @Override
    public T getFirstItem() {
        return this.target.getFirstItem();
    }

    @Override
    public int size() {
        return this.target.size();
    }

    @Override
    public Iterator<T> iterator() {
        return this.target.iterator();
    }

    @Override
    public void resetStatus() {
        this.target.resetStatus();
    }
}
