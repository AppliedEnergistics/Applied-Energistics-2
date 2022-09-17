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


import appeng.core.AELog;
import appeng.services.version.Channel;
import appeng.services.version.Version;
import appeng.services.version.VersionCheckerConfig;
import appeng.services.version.VersionParser;
import appeng.services.version.exceptions.VersionCheckerException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public final class ReleaseFetcher {
    private static final String GITHUB_RELEASES_URL = "https://api.github.com/repos/AppliedEnergistics/Applied-Energistics-2/releases";
    private static final FormattedRelease EXCEPTIONAL_RELEASE = new MissingFormattedRelease();

    @Nonnull
    private final VersionCheckerConfig config;
    @Nonnull
    private final VersionParser parser;

    public ReleaseFetcher(@Nonnull final VersionCheckerConfig config, @Nonnull final VersionParser parser) {
        this.config = config;
        this.parser = parser;
    }

    public FormattedRelease get() {
        final Gson gson = new Gson();
        final Type type = new ReleasesTypeToken().getType();

        try {
            final URL releasesURL = new URL(GITHUB_RELEASES_URL);
            final String rawReleases = this.getRawReleases(releasesURL);

            this.config.updateLastCheck();

            final List<Release> releases = gson.fromJson(rawReleases, type);
            final FormattedRelease latestFitRelease = this.getLatestFitRelease(releases);

            return latestFitRelease;
        } catch (final VersionCheckerException e) {
            AELog.debug(e);
        } catch (final MalformedURLException e) {
            AELog.debug(e);
        } catch (final IOException e) {
            AELog.debug(e);
        }

        return EXCEPTIONAL_RELEASE;
    }

    private String getRawReleases(final URL url) throws IOException {
        return IOUtils.toString(url);
    }

    private FormattedRelease getLatestFitRelease(final Iterable<Release> releases) throws VersionCheckerException {
        final String levelInConfig = this.config.level();
        final Channel level = Channel.valueOf(levelInConfig);
        final int levelOrdinal = level.ordinal();

        for (final Release release : releases) {
            final String rawVersion = release.tag_name;
            final String changelog = release.body;

            final Version version = this.parser.parse(rawVersion);

            if (version.channel().ordinal() >= levelOrdinal) {
                return new DefaultFormattedRelease(version, changelog);
            }
        }

        return EXCEPTIONAL_RELEASE;
    }

    private static final class ReleasesTypeToken extends TypeToken<List<Release>> {
    }
}
