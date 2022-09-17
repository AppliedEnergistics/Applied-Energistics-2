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


import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;


public class GridCacheWrapper implements IGridCache {

    private final IGridCache myCache;
    private final String name;

    public GridCacheWrapper(final IGridCache gc) {
        this.myCache = gc;
        this.name = this.getCache().getClass().getName();
    }

    @Override
    public void onUpdateTick() {
        this.getCache().onUpdateTick();
    }

    @Override
    public void removeNode(final IGridNode gridNode, final IGridHost machine) {
        this.getCache().removeNode(gridNode, machine);
    }

    @Override
    public void addNode(final IGridNode gridNode, final IGridHost machine) {
        this.getCache().addNode(gridNode, machine);
    }

    @Override
    public void onSplit(final IGridStorage storageB) {
        this.getCache().onSplit(storageB);
    }

    @Override
    public void onJoin(final IGridStorage storageB) {
        this.getCache().onJoin(storageB);
    }

    @Override
    public void populateGridStorage(final IGridStorage storage) {
        this.getCache().populateGridStorage(storage);
    }

    public String getName() {
        return this.name;
    }

    IGridCache getCache() {
        return this.myCache;
    }
}
