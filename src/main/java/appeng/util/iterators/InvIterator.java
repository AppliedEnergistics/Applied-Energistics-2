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

package appeng.util.iterators;

import java.util.Iterator;

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.item.FixedItemInv;

public final class InvIterator implements Iterator<ItemStack> {
    private final FixedItemInv inventory;
    private final int size;

    private int counter = 0;

    public InvIterator(final FixedItemInv inventory) {
        this.inventory = inventory;
        this.size = this.inventory.getSlotCount();
    }

    @Override
    public boolean hasNext() {
        return this.counter < this.size;
    }

    @Override
    public ItemStack next() {
        final ItemStack result = this.inventory.getInvStack(this.counter);
        this.counter++;

        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
