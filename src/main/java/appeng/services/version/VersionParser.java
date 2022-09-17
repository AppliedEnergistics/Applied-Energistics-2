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


import appeng.services.version.exceptions.*;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * can parse a version in form of rv2-beta-8 or rv2.beta.8
 */
public final class VersionParser {
    private static final Pattern PATTERN_DOT = Pattern.compile("\\.");
    private static final Pattern PATTERN_DASH = Pattern.compile("-");
    private static final Pattern PATTERN_REVISION = Pattern.compile("[^0-9]+");
    private static final Pattern PATTERN_BUILD = Pattern.compile("[^0-9]+");
    private static final Pattern PATTERN_NATURAL = Pattern.compile("[0-9]+");
    private static final Pattern PATTERN_VALID_REVISION = Pattern.compile("^rv\\d+$");

    /**
     * Parses the {@link Version} out of a String
     *
     * @param raw String in form of rv2-beta-8 or rv2.beta.8
     * @return {@link Version} encoded in the raw String
     * @throws VersionCheckerException if parsing the raw string was not successful.
     */
    public Version parse(@Nonnull final String raw) throws VersionCheckerException {
        Preconditions.checkNotNull(raw);

        final String transformed = this.transformDelimiter(raw);
        final String[] split = transformed.split("_");

        return this.parseVersion(split);
    }

    /**
     * Replaces all "." and "-" into "_" to make them uniform
     *
     * @param raw raw version string containing "." or "-"
     * @return transformed raw, where "." and "-" are replaced by "_"
     * @throws MissingSeparatorException if not containing valid separators
     */
    private String transformDelimiter(@Nonnull final String raw) throws MissingSeparatorException {
        if (!(raw.contains(".") || raw.contains("-"))) {
            throw new MissingSeparatorException();
        }

        final String withoutDot = PATTERN_DOT.matcher(raw).replaceAll("_");
        final String withoutDash = PATTERN_DASH.matcher(withoutDot).replaceAll("_");

        return withoutDash;
    }

    /**
     * parses the {@link Version} out of the split.
     * The split must have a length of 3,
     * representing revision, channel and build.
     *
     * @param splitRaw raw version split with length of 3
     * @return {@link Version} represented by the splitRaw
     * @throws InvalidVersionException  when length not 3
     * @throws InvalidRevisionException {@link VersionParser#parseRevision(String)}
     * @throws InvalidChannelException  {@link VersionParser#parseChannel(String)}
     * @throws InvalidBuildException    {@link VersionParser#parseBuild(String)}
     */
    private Version parseVersion(@Nonnull final String[] splitRaw) throws InvalidVersionException, InvalidRevisionException, InvalidChannelException, InvalidBuildException {
        if (splitRaw.length != 3) {
            throw new InvalidVersionException();
        }

        final String rawRevision = splitRaw[0];
        final String rawChannel = splitRaw[1];
        final String rawBuild = splitRaw[2];

        final int revision = this.parseRevision(rawRevision);
        final Channel channel = this.parseChannel(rawChannel);
        final int build = this.parseBuild(rawBuild);

        return new DefaultVersion(revision, channel, build);
    }

    /**
     * A revision starts with the keyword "rv", followed by a natural number
     *
     * @param rawRevision String containing the revision number
     * @return revision number
     * @throws InvalidRevisionException if not matching "rv" followed by a natural number.
     */
    private int parseRevision(@Nonnull final String rawRevision) throws InvalidRevisionException {
        if (!PATTERN_VALID_REVISION.matcher(rawRevision).matches()) {
            throw new InvalidRevisionException();
        }

        final Scanner scanner = new Scanner(rawRevision);

        final int revision = scanner.useDelimiter(PATTERN_REVISION).nextInt();

        scanner.close();

        return revision;
    }

    /**
     * A channel is atm either one of {@link Channel#Alpha}, {@link Channel#Beta} or {@link Channel#Stable}
     *
     * @param rawChannel String containing the channel
     * @return matching {@link Channel} to the String
     * @throws InvalidChannelException if not one of {@link Channel} values.
     */
    private Channel parseChannel(@Nonnull final String rawChannel) throws InvalidChannelException {
        if (!(rawChannel.equalsIgnoreCase(Channel.Alpha.name()) || rawChannel.equalsIgnoreCase(Channel.Beta.name()) || rawChannel
                .equalsIgnoreCase(Channel.Stable.name()))) {
            throw new InvalidChannelException();
        }

        for (final Channel channel : Channel.values()) {
            if (channel.name().equalsIgnoreCase(rawChannel)) {
                return channel;
            }
        }

        throw new InvalidChannelException();
    }

    /**
     * A build is just a natural number
     *
     * @param rawBuild String containing the build number
     * @return build number
     * @throws InvalidBuildException if not a natural number.
     */
    private int parseBuild(@Nonnull final String rawBuild) throws InvalidBuildException {
        if (!PATTERN_NATURAL.matcher(rawBuild).matches()) {
            throw new InvalidBuildException();
        }

        final Scanner scanner = new Scanner(rawBuild);

        final int build = scanner.useDelimiter(PATTERN_BUILD).nextInt();

        scanner.close();

        return build;
    }
}
