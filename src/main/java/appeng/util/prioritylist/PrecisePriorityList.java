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

package appeng.util.prioritylist;


import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;


public class PrecisePriorityList<T extends IAEStack<T>> implements IPartitionList<T> {

    private final IItemList<T> list;

    public PrecisePriorityList(final IItemList<T> in) {
        this.list = in;
    }

    @Override
    public boolean isListed(final T input) {
        return this.list.findPrecise(input) != null;
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public Iterable<T> getItems() {
        return this.list;
    }
}
