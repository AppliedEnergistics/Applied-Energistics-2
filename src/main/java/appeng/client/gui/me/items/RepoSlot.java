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

import net.minecraft.world.item.ItemStack;

import appeng.client.gui.me.common.ClientReadOnlySlot;
import appeng.client.gui.me.common.Repo;
import appeng.menu.me.common.GridInventoryEntry;

/**
 * This is a virtual slot that has no corresponding slot on the server-side. It displays an item stack from the
 * client-side {@link Repo}.
 */
public class RepoSlot extends ClientReadOnlySlot {

    private final Repo repo;
    private final int offset;

    public RepoSlot(Repo repo, int offset, int displayX, int displayY) {
        super(displayX, displayY);
        this.repo = repo;
        this.offset = offset;
    }

    public GridInventoryEntry getEntry() {
        if (this.repo.hasPower()) {
            return this.repo.get(this.offset);
        }
        return null;
    }

    public long getStoredAmount() {
        GridInventoryEntry entry = getEntry();
        return entry != null ? entry.getStoredAmount() : 0;
    }

    public long getRequestableAmount() {
        GridInventoryEntry entry = getEntry();
        return entry != null ? entry.getRequestableAmount() : 0;
    }

    public boolean isCraftable() {
        GridInventoryEntry entry = getEntry();
        return entry != null && entry.isCraftable();
    }

    @Override
    public ItemStack getItem() {
        GridInventoryEntry entry = getEntry();
        if (entry != null) {
            return entry.getWhat().wrapForDisplayOrFilter();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasItem() {
        return getEntry() != null;
    }

}
