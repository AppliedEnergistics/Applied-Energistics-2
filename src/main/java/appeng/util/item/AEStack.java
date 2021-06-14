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
import io.netty.buffer.ByteBuf;


public abstract class AEStack<T extends IAEStack<T>> implements IAEStack<T> {

    private boolean isCraftable;
    private long stackSize;
    private long countRequestable;

    protected static long getPacketValue( final byte type, final ByteBuf tag )
    {
        if( type == 0 )
        {
            long l = tag.readByte();
            l -= Byte.MIN_VALUE;
            return l;
        }
        else if( type == 1 )
        {
            long l = tag.readShort();
            l -= Short.MIN_VALUE;
            return l;
        }
        else if( type == 2 )
        {
            long l = tag.readInt();
            l -= Integer.MIN_VALUE;
            return l;
        }

        return tag.readLong();
    }

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
    public void incStackSize(final long i) {
        this.stackSize += i;
    }

    @Override
    public void decStackSize(final long i) {
        this.stackSize -= i;
    }

    @Override
    public void incCountRequestable(final long i) {
        this.countRequestable += i;
    }

    @Override
    public void decCountRequestable(final long i) {
        this.countRequestable -= i;
    }

    protected byte getType( final long num )
    {
        if( num <= 255 )
        {
            return 0;
        }
        else if( num <= 65535 )
        {
            return 1;
        }
        else if( num <= 4294967295L )
        {
            return 2;
        }
        else
        {
            return 3;
        }
    }

    protected abstract boolean hasTagCompound();

    protected void putPacketValue( final ByteBuf tag, final long num )
    {
        if( num <= 255 )
        {
            tag.writeByte( (byte) ( num + Byte.MIN_VALUE ) );
        }
        else if( num <= 65535 )
        {
            tag.writeShort( (short) ( num + Short.MIN_VALUE ) );
        }
        else if( num <= 4294967295L )
        {
            tag.writeInt( (int) ( num + Integer.MIN_VALUE ) );
        }
        else
        {
            tag.writeLong( num );
        }
    }
}
