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

import java.util.Iterator;
import java.util.NoSuchElementException;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;

class ItemHandlerIterator implements Iterator<ItemSlot> {

    private final FixedItemInv itemHandler;

    private final ItemSlot itemSlot = new ItemSlot();

    private int slot = 0;

    ItemHandlerIterator(FixedItemInv itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public boolean hasNext() {
        return this.slot < this.itemHandler.getSlotCount();
    }

    @Override
    public ItemSlot next() {
        if (this.slot >= this.itemHandler.getSlotCount()) {
            throw new NoSuchElementException();
        }
        this.itemSlot.setExtractable(
                !this.itemHandler.getSlot(this.slot).attemptAnyExtraction(1, Simulation.SIMULATE).isEmpty());
        this.itemSlot.setItemStack(this.itemHandler.getInvStack(this.slot));
        this.itemSlot.setSlot(this.slot);
        this.slot++;
        return this.itemSlot;
    }

}
