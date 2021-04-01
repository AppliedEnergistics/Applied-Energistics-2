/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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
import java.util.WeakHashMap;
import net.minecraft.nbt.CompoundNBT;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridStorage;
import appeng.core.worlddata.WorldData;

public class GridStorage implements IGridStorage {

    private final long myID;
    private final CompoundNBT data;
    private final WeakHashMap<GridStorage, Boolean> divided = new WeakHashMap<>();
    private WeakReference<IGrid> internalGrid = null;

    /**
     * for use with world settings
     *
     * @param id ID of grid storage
     */
    public GridStorage(final long id) {
        this.myID = id;
        this.data = new CompoundNBT();
    }

    /**
     * for use with world settings
     *
     * @param data The Grid data.
     * @param id   ID of grid storage
     */
    public GridStorage(final long id, final CompoundNBT data) {
        this.myID = id;
        this.data = data;
    }

    /**
     * fake storage.
     */
    public GridStorage() {
        this.myID = 0;
        this.data = new CompoundNBT();
    }

    public void saveState() {
        final Grid currentGrid = (Grid) this.getGrid();
        if (currentGrid != null) {
            currentGrid.saveState();
        }
    }

    public IGrid getGrid() {
        return this.internalGrid == null ? null : this.internalGrid.get();
    }

    void setGrid(final IGrid grid) {
        this.internalGrid = new WeakReference<>(grid);
    }

    @Override
    public CompoundNBT dataObject() {
        return this.data;
    }

    @Override
    public long getID() {
        return this.myID;
    }

    void addDivided(final GridStorage gs) {
        this.divided.put(gs, true);
    }

    boolean hasDivided(final GridStorage myStorage) {
        return this.divided.containsKey(myStorage);
    }

    void remove() {
        WorldData.instance().storageData().destroyGridStorage(this.myID);
    }
}
