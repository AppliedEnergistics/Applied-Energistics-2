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

package appeng.util.item;

import appeng.api.storage.data.IAEStack;

public abstract class AEStack<T extends IAEStack<T>> implements IAEStack<T> {

    private boolean isCraftable;
    private long stackSize;
    private long countRequestable;

    @Override
    public long getStackSize() {
        return this.stackSize;
    }

    @Override
    public T setStackSize(final long ss) {
        this.stackSize = ss;
        return (T) this;
    }

    @Override
    public long getCountRequestable() {
        return this.countRequestable;
    }

    @Override
    public T setCountRequestable(final long countRequestable) {
        this.countRequestable = countRequestable;
        return (T) this;
    }

    @Override
    public boolean isCraftable() {
        return this.isCraftable;
    }

    @Override
    public T setCraftable(final boolean isCraftable) {
        this.isCraftable = isCraftable;
        return (T) this;
    }

    @Override
    public T reset() {
        this.stackSize = 0;
        this.setCountRequestable(0);
        this.setCraftable(false);
        return (T) this;
    }

    @Override
    public T empty() {
        final T dup = this.copy();
        dup.reset();
        return dup;
    }

    @Override
    public boolean isMeaningful() {
        return this.stackSize != 0 || this.countRequestable > 0 || this.isCraftable;
    }

    @Override
    public void incCountRequestable(final long i) {
        this.countRequestable += i;
    }

    @Override
    public void decCountRequestable(final long i) {
        this.countRequestable -= i;
    }

    protected abstract boolean hasTagCompound();
}
