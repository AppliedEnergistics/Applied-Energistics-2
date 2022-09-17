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


import appeng.util.inv.ItemSlot;
import net.minecraft.item.ItemStack;

import java.util.Iterator;


public class StackToSlotIterator implements Iterator<ItemSlot> {

    private final ItemSlot iss = new ItemSlot();
    private final Iterator<ItemStack> is;
    private int x = 0;

    public StackToSlotIterator(final Iterator<ItemStack> is) {
        this.is = is;
    }

    @Override
    public boolean hasNext() {
        return this.is.hasNext();
    }

    @Override
    public ItemSlot next() {
        this.iss.setSlot(this.x);
        this.x++;
        this.iss.setItemStack(this.is.next());
        return this.iss;
    }

    @Override
    public void remove() {
        // uhh no.
    }
}
