/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2020, AlgorithmX2, All rights reserved.
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
import java.util.Collections;
import java.util.Iterator;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class NullItemList implements IItemList<IAEItemStack> {

    @Override
    public void add(IAEItemStack option) {
    }

    @Override
    public IAEItemStack findPrecise(IAEItemStack i) {
        return null;
    }

    @Override
    public Collection<IAEItemStack> findFuzzy(IAEItemStack input, FuzzyMode fuzzy) {
        return Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void addStorage(IAEItemStack option) {
    }

    @Override
    public void addCrafting(IAEItemStack option) {
    }

    @Override
    public void addRequestable(IAEItemStack option) {
    }

    @Override
    public IAEItemStack getFirstItem() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Iterator<IAEItemStack> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public void resetStatus() {
    }

}
