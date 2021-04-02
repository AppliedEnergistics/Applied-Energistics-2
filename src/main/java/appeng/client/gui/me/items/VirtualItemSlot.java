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

package appeng.client.gui.me.items;

import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.me.common.ClientReadOnlySlot;

/**
 * This is a virtual slot that has no corresponding slot on the server-side. It displays an item stack from the
 * client-side {@link ItemRepo}.
 */
public class VirtualItemSlot extends ClientReadOnlySlot {

    private final ItemRepo repo;
    private final int offset;

    public VirtualItemSlot(ItemRepo repo, int offset, int displayX, int displayY) {
        super(displayX, displayY);
        this.repo = repo;
        this.offset = offset;
    }

    public IAEItemStack getAEStack() {
        if (this.repo.hasPower()) {
            return this.repo.get(this.offset);
        }
        return null;
    }

    @Override
    public ItemStack getStack() {
        IAEItemStack aeStack = this.getAEStack();
        if (aeStack != null) {
            return aeStack.asItemStackRepresentation();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean getHasStack() {
        return getAEStack() != null;
    }

}
