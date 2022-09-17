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


import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;


class GridSplitDetector implements IGridVisitor {

    private final IGridNode pivot;
    private boolean pivotFound;

    public GridSplitDetector(final IGridNode pivot) {
        this.pivot = pivot;
    }

    @Override
    public boolean visitNode(final IGridNode n) {
        if (n == this.pivot) {
            this.setPivotFound(true);
        }

        return !this.isPivotFound();
    }

    public boolean isPivotFound() {
        return this.pivotFound;
    }

    private void setPivotFound(final boolean pivotFound) {
        this.pivotFound = pivotFound;
    }
}
