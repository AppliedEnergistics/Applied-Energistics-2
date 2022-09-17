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

package appeng.parts;


import appeng.api.implementations.parts.IPartCable;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.util.AEPartLocation;

import javax.annotation.Nullable;


/**
 * Thin data storage to optimize memory usage for cables.
 */
public class CableBusStorage {

    private IPartCable center;
    private IPart[] sides;
    private IFacadePart[] facades;

    protected IPartCable getCenter() {
        return this.center;
    }

    protected void setCenter(final IPartCable center) {
        this.center = center;
    }

    protected IPart getSide(final AEPartLocation side) {
        final int x = side.ordinal();
        if (this.sides != null && this.sides.length > x) {
            return this.sides[x];
        }

        return null;
    }

    protected void setSide(final AEPartLocation side, final IPart part) {
        final int x = side.ordinal();

        if (this.sides != null && this.sides.length > x && part == null) {
            this.sides[x] = null;
            this.sides = this.shrink(this.sides, true);
        } else if (part != null) {
            this.sides = this.grow(this.sides, x, true);
            this.sides[x] = part;
        }
    }

    private <T> T[] shrink(final T[] in, final boolean parts) {
        int newSize = -1;
        for (int x = 0; x < in.length; x++) {
            if (in[x] != null) {
                newSize = x;
            }
        }

        if (newSize == -1) {
            return null;
        }

        newSize++;
        if (newSize == in.length) {
            return in;
        }

        final T[] newArray = (T[]) (parts ? new IPart[newSize] : new IFacadePart[newSize]);
        System.arraycopy(in, 0, newArray, 0, newSize);

        return newArray;
    }

    private <T> T[] grow(final T[] in, final int newValue, final boolean parts) {
        if (in != null && in.length > newValue) {
            return in;
        }

        final int newSize = newValue + 1;

        final T[] newArray = (T[]) (parts ? new IPart[newSize] : new IFacadePart[newSize]);
        if (in != null) {
            System.arraycopy(in, 0, newArray, 0, in.length);
        }

        return newArray;
    }

    public IFacadePart getFacade(final int x) {
        if (this.facades != null && this.facades.length > x) {
            return this.facades[x];
        }

        return null;
    }

    public void setFacade(final int x, @Nullable final IFacadePart facade) {
        if (this.facades != null && this.facades.length > x && facade == null) {
            this.facades[x] = null;
            this.facades = this.shrink(this.facades, false);
        } else {
            this.facades = this.grow(this.facades, x, false);
            this.facades[x] = facade;
        }
    }
}
