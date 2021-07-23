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

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.me.common.ClientReadOnlySlot;
import appeng.client.gui.me.common.Repo;
import appeng.container.me.common.GridInventoryEntry;
import net.minecraft.world.inventory.Slot;

/**
 * This is a virtual slot that has no corresponding slot on the server-side. It displays an item stack from the
 * client-side {@link ItemRepo}.
 */
public class RepoSlot<T extends IAEStack<T>> extends ClientReadOnlySlot {

    private final Repo<T> repo;
    private final int offset;

    public RepoSlot(Repo<T> repo, int offset, int displayX, int displayY) {
        super(displayX, displayY);
        this.repo = repo;
        this.offset = offset;
    }

    public GridInventoryEntry<T> getEntry() {
        if (this.repo.hasPower()) {
            return this.repo.get(this.offset);
        }
        return null;
    }

    /**
     * @see IAEItemStack#getStackSize()
     */
    public long getStoredAmount() {
        GridInventoryEntry<T> entry = getEntry();
        return entry != null ? entry.getStoredAmount() : 0;
    }

    /**
     * @see IAEItemStack#getCountRequestable()
     */
    public long getRequestableAmount() {
        GridInventoryEntry<T> entry = getEntry();
        return entry != null ? entry.getRequestableAmount() : 0;
    }

    /**
     * @see IAEItemStack#isCraftable()
     */
    public boolean isCraftable() {
        GridInventoryEntry<T> entry = getEntry();
        return entry != null && entry.isCraftable();
    }

    @Override
    public ItemStack getItem() {
        GridInventoryEntry<T> entry = getEntry();
        if (entry != null) {
            return entry.getStack().asItemStackRepresentation();
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public boolean hasItem() {
        return getEntry() != null;
    }

    /**
     * Tries to cast any given slot (which may be null) to a {@link RepoSlot} of the same type as the given repo.
     * Returns null when the given slot is not compatible.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends IAEStack<T>> RepoSlot<T> tryCast(Repo<T> repo, @Nullable Slot slot) {
        if (slot instanceof RepoSlot) {
            RepoSlot<?> repoSlot = (RepoSlot<?>) slot;
            if (repoSlot.repo == repo) {
                return (RepoSlot<T>) repoSlot;
            }
        }
        return null;
    }

}
