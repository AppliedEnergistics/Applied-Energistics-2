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

package appeng.services.version.github;


import appeng.services.version.MissingVersion;
import appeng.services.version.Version;

import javax.annotation.Nonnull;


/**
 * Exceptional template, when no meaningful {@link FormattedRelease} could be obtained
 */
public final class MissingFormattedRelease implements FormattedRelease {
    @Nonnull
    private final Version version;

    public MissingFormattedRelease() {
        this.version = new MissingVersion();
    }

    /**
     * @return empty string
     */
    @Override
    public String changelog() {
        return "";
    }

    /**
     * @return {@link MissingVersion}
     */
    @Override
    public Version version() {
        return this.version;
    }
}
