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

package appeng.client.me;

import javax.annotation.Nonnull;

import net.minecraft.util.text.ITextComponent;

import appeng.tile.inventory.AppEngInternalInventory;

public class ClientDCInternalInv implements Comparable<ClientDCInternalInv> {

    private final String searchName;
    private final String formattedName;
    private final AppEngInternalInventory inventory;

    private final long id;
    private final long sortBy;

    public ClientDCInternalInv(final int size, final long id, final long sortBy, final ITextComponent name) {
        this.inventory = new AppEngInternalInventory(null, size);
        this.searchName = name.getString().toLowerCase();
        this.formattedName = name.getString(); // FIXME FABRIC no longer formatted!
        this.id = id;
        this.sortBy = sortBy;
    }

    public String getSearchName() {
        return searchName;
    }

    public String getFormattedName() {
        return formattedName;
    }

    @Override
    public int compareTo(@Nonnull final ClientDCInternalInv o) {
        return Long.compare(this.sortBy, o.sortBy);
    }

    public AppEngInternalInventory getInventory() {
        return this.inventory;
    }

    public long getId() {
        return this.id;
    }

    public boolean matchesSearch(String searchFilterLowerCase) {
        return this.searchName.contains(searchFilterLowerCase);
    }
}
