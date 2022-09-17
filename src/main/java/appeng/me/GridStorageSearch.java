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

package appeng.me;


import java.lang.ref.WeakReference;


public class GridStorageSearch {

    private final long id;
    private WeakReference<GridStorage> gridStorage;

    /**
     * for use with the world settings
     *
     * @param id ID of grid storage search
     */
    public GridStorageSearch(final long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return ((Long) this.id).hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final GridStorageSearch other = (GridStorageSearch) obj;
        return this.id == other.id;
    }

    public WeakReference<GridStorage> getGridStorage() {
        return this.gridStorage;
    }

    public void setGridStorage(final WeakReference<GridStorage> gridStorage) {
        this.gridStorage = gridStorage;
    }
}
