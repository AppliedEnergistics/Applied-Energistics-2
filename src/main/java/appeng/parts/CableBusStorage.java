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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;

/**
 * Thin data storage to optimize memory usage for cables.
 */
public class CableBusStorage {

    @Nullable
    private ICablePart center;
    @Nullable
    private IPart[] parts;
    @Nullable
    private IFacadePart[] facades;

    protected ICablePart getCenter() {
        return this.center;
    }

    protected void setCenter(final ICablePart center) {
        this.center = center;
    }

    protected IPart getPart(@Nonnull Direction side) {
        if (this.parts == null) {
            return null;
        }

        var index = side.ordinal();
        return this.parts[index];
    }

    protected void setPart(@Nonnull Direction side, final IPart part) {
        if (this.parts == null) {
            this.parts = new IPart[Direction.values().length];
        }

        var index = side.ordinal();
        this.parts[index] = part;
    }

    protected void removePart(@Nonnull Direction side) {
        if (this.parts == null) {
            return;
        }

        var index = side.ordinal();
        this.parts[index] = null;

        if (isNullArray(this.parts)) {
            this.parts = null;
        }
    }

    public IFacadePart getFacade(@Nonnull Direction side) {
        if (this.facades == null) {
            return null;
        }

        var index = side.ordinal();
        return this.facades[index];
    }

    public void setFacade(@Nonnull Direction side, @Nullable final IFacadePart facade) {
        if (facade == null) {
            removeFacade(side);
            return;
        }

        if (facades == null) {
            this.facades = new IFacadePart[Direction.values().length];
        }

        var index = side.ordinal();
        this.facades[index] = facade;
    }

    public void removeFacade(@Nonnull Direction side) {
        if (this.facades == null) {
            return;
        }

        var index = side.ordinal();
        this.facades[index] = null;

        if (isNullArray(this.facades)) {
            this.facades = null;
        }
    }

    private static <T> boolean isNullArray(T[] array) {
        if (array == null) {
            return true;
        }

        for (var o : array) {
            if (o != null) {
                return false;
            }
        }

        return true;
    }
}
