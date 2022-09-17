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
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Date;


/**
 * Separate config file to handle the version checker
 */
public final class VersionCheckerConfig {
    private static final int DEFAULT_INTERVAL_HOURS = 24;
    private static final int MIN_INTERVAL_HOURS = 0;
    private static final int MAX_INTERVAL_HOURS = 7 * 24;

    @Nonnull
    private final Configuration config;

    private final boolean isEnabled;

    @Nonnull
    private final String lastCheck;
    private final int interval;

    @Nonnull
    private final String level;

    private final boolean shouldNotifyPlayer;
    private final boolean shouldPostChangelog;

    /**
     * @param file requires fully qualified file in which the config is saved
     */
    public VersionCheckerConfig(@Nonnull final File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkState(!file.isDirectory());

        this.config = new Configuration(file);

        // initializes default values by caching
        this.isEnabled = this.config.getBoolean("enabled", "general", true, "If true, the version checker is enabled. Acts as a master switch.");

        this.lastCheck = this.config.getString("lastCheck", "cache", "0",
                "The number of milliseconds since January 1, 1970, 00:00:00 GMT of the last successful check.");
        this.interval = this.config.getInt("interval", "cache", DEFAULT_INTERVAL_HOURS, MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS,
                "Waits as many hours, until it checks again.");

        this.level = this.config.getString("level", "channel", "Beta",
                "Determines the channel level which should be checked for updates. Can be either Stable, Beta or Alpha.");

        this.shouldNotifyPlayer = this.config.getBoolean("notify", "client", true,
                "If true, the player is getting a notification, that a new version is available.");
        this.shouldPostChangelog = this.config.getBoolean("changelog", "client", true,
                "If true, the player is getting a notification including changelog. Only happens if notification are enabled.");
    }

    public boolean isVersionCheckingEnabled() {
        return this.isEnabled;
    }

    public String lastCheck() {
        return this.lastCheck;
    }

    /**
     * Stores the current date in milli seconds into the "lastCheck" field of the config and makes it persistent.
     */
    public void updateLastCheck() {
        final Date now = new Date();
        final long nowInMs = now.getTime();
        final String nowAsString = Long.toString(nowInMs);

        this.config.get("cache", "lastCheck", "0").set(nowAsString);

        this.config.save();
    }

    public int interval() {
        return this.interval;
    }

    public String level() {
        return this.level;
    }

    public boolean shouldNotifyPlayer() {
        return this.shouldNotifyPlayer;
    }

    public boolean shouldPostChangelog() {
        return this.shouldPostChangelog;
    }

    public void save() {
        if (this.config.hasChanged()) {
            this.config.save();
        }
    }
}
