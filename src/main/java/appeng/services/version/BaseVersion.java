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

package appeng.services.version;


import com.google.common.base.Preconditions;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * Base version of {@link Version}.
 * <p>
 * Provides a unified way to test for equality and print a formatted string
 */
public abstract class BaseVersion implements Version {
    @Nonnegative
    private final int revision;
    @Nonnull
    private final Channel channel;
    @Nonnegative
    private final int build;

    /**
     * @param revision revision in natural number
     * @param channel  channel
     * @param build    build in natural number
     * @throws AssertionError if assertion are enabled and revision or build are not natural numbers
     */
    public BaseVersion(@Nonnegative final int revision, @Nonnull final Channel channel, @Nonnegative final int build) {
        Preconditions.checkArgument(revision >= 0);
        Preconditions.checkNotNull(channel);
        Preconditions.checkArgument(build >= 0);

        this.revision = revision;
        this.channel = channel;
        this.build = build;
    }

    @Override
    public final int revision() {
        return this.revision;
    }

    @Override
    public final Channel channel() {
        return this.channel;
    }

    @Override
    public final int build() {
        return this.build;
    }

    @Override
    public String formatted() {
        return "rv" + this.revision + '-' + this.channel.name().toLowerCase() + '-' + this.build;
    }

    @Override
    public final int hashCode() {
        int result = this.revision;
        result = 31 * result + this.channel.hashCode();
        result = 31 * result + this.build;
        return result;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Version)) {
            return false;
        }

        final Version that = (Version) o;

        if (this.revision != that.revision()) {
            return false;
        }
        if (this.build != that.build()) {
            return false;
        }
        return this.channel == that.channel();
    }

    @Override
    public final String toString() {
        return "Version{" + "revision=" + this.revision + ", channel=" + this.channel + ", build=" + this.build + '}';
    }
}
