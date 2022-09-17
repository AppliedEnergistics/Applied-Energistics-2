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


import appeng.core.AELog;
import appeng.services.version.exceptions.VersionCheckerException;

import javax.annotation.Nonnull;


/**
 * Wrapper for {@link VersionParser} to check if the check is happening in developer environment or in a pull request.
 * <p>
 * In that case ignore the check.
 */
public final class ModVersionFetcher implements VersionFetcher {
    private static final Version EXCEPTIONAL_VERSION = new MissingVersion();

    @Nonnull
    private final String rawModVersion;
    @Nonnull
    private final VersionParser parser;

    public ModVersionFetcher(@Nonnull final String rawModVersion, @Nonnull final VersionParser parser) {
        this.rawModVersion = rawModVersion;
        this.parser = parser;
    }

    /**
     * Parses only, if not checked in developer environment or in a pull request
     *
     * @return {@link DoNotCheckVersion} if in developer environment or pull request, {@link MissingVersion} in case of
     * a parser exception or else the parsed {@link Version}.
     */
    @Override
    public Version get() {
        if (this.rawModVersion.equals("@version@") || this.rawModVersion.contains("pr")) {
            return new DoNotCheckVersion();
        }

        try {
            final Version version = this.parser.parse(this.rawModVersion);

            return version;
        } catch (final VersionCheckerException e) {
            AELog.debug(e);

            return EXCEPTIONAL_VERSION;
        }
    }
}
