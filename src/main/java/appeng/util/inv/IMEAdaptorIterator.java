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


import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import net.minecraft.item.ItemStack;

import java.util.Iterator;


public final class IMEAdaptorIterator implements Iterator<ItemSlot> {
    private final Iterator<IAEItemStack> stack;
    private final ItemSlot slot = new ItemSlot();
    private final IMEAdaptor parent;
    private final int containerSize;

    private int offset = 0;
    private boolean hasNext;

    public IMEAdaptorIterator(final IMEAdaptor parent, final IItemList<IAEItemStack> availableItems) {
        this.stack = availableItems.iterator();
        this.containerSize = parent.getMaxSlots();
        this.parent = parent;
    }

    @Override
    public boolean hasNext() {
        this.hasNext = this.stack.hasNext();
        return this.offset < this.containerSize || this.hasNext;
    }

    @Override
    public ItemSlot next() {
        this.slot.setSlot(this.offset);
        this.offset++;
        this.slot.setExtractable(true);

        if (this.parent.getMaxSlots() < this.offset) {
            this.parent.setMaxSlots(this.offset);
        }

        if (this.hasNext) {
            final IAEItemStack item = this.stack.next();
            this.slot.setAEItemStack(item);
            return this.slot;
        }

        this.slot.setItemStack(ItemStack.EMPTY);
        return this.slot;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
