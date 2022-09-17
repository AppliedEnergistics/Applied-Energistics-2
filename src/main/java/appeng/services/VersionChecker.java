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

package appeng.services;


import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.services.version.*;
import appeng.services.version.github.FormattedRelease;
import appeng.services.version.github.ReleaseFetcher;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nonnull;
import java.util.Date;


/**
 * Tries to connect to GitHub to retrieve the most current build.
 * After comparison with the local version, several path can be chosen.
 * <p>
 * If the local version is invalid, somebody might have build that version themselves
 * or it is run in a developer environment, then nothing needs to be done.
 * <p>
 * If GitHub can not be reached, then either is GitHub down
 * or the connection to GitHub disturbed, then nothing needs to be done,
 * since no comparison can be reached
 * <p>
 * If the version was just recently checked, then no need to poll again.
 * Nobody wants to bother to update several times a day.
 * <p>
 * Config enables to fine-tune when a version is considered newer
 * <p>
 * If the local version is newer or equal to the GitHub version,
 * then no update needs to be posted
 * <p>
 * Only after all that cases, if the external version is higher than the local,
 * use Version Checker Mod and post several information needed for it to update the mod.
 */
public final class VersionChecker implements Runnable {
    private static final int SEC_TO_HOUR = 3600;
    private static final int MS_TO_SEC = 1000;
    private final VersionCheckerConfig config;

    public VersionChecker(@Nonnull final VersionCheckerConfig config) {
        Preconditions.checkNotNull(config);

        this.config = config;
    }

    @Override
    public void run() {
        try {
            Thread.yield();

            // persist the config
            this.config.save();

            // retrieve data
            final String rawLastCheck = this.config.lastCheck();

            // process data
            final long lastCheck = Long.parseLong(rawLastCheck);
            final Date now = new Date();
            final long nowInMs = now.getTime();
            final long intervalInMs = this.config.interval() * SEC_TO_HOUR * MS_TO_SEC;
            final long lastAfterInterval = lastCheck + intervalInMs;

            this.processInterval(nowInMs, lastAfterInterval);
        } catch (final Exception exception) {
            // Log any unhandled exception to prevent the JVM from reporting them as unhandled.
            AELog.debug(exception);
        }

        AELog.info("Stopping AE2 VersionChecker");
    }

    /**
     * checks if enough time since last check has expired
     *
     * @param nowInMs           now in milli seconds
     * @param lastAfterInterval last version check including the interval defined in the config
     */
    private void processInterval(final long nowInMs, final long lastAfterInterval) {
        if (nowInMs > lastAfterInterval) {
            final String rawModVersion = AEConfig.VERSION;
            final VersionParser parser = new VersionParser();
            final VersionFetcher modFetcher = new ModVersionFetcher(rawModVersion, parser);
            final ReleaseFetcher githubFetcher = new ReleaseFetcher(this.config, parser);

            final Version modVersion = modFetcher.get();
            final FormattedRelease githubRelease = githubFetcher.get();

            this.processVersions(modVersion, githubRelease);
        } else {
            AELog.info("Last check was just recently.");
        }
    }

    /**
     * Checks if the retrieved version is newer as the current mod version.
     * Will notify player if config is enabled.
     *
     * @param modVersion    version of mod
     * @param githubRelease release retrieved through github
     */
    private void processVersions(@Nonnull final Version modVersion, @Nonnull final FormattedRelease githubRelease) {
        final Version githubVersion = githubRelease.version();
        final String modFormatted = modVersion.formatted();
        final String ghFormatted = githubVersion.formatted();

        if (githubVersion.isNewerAs(modVersion)) {
            final String changelog = githubRelease.changelog();

            if (this.config.shouldNotifyPlayer()) {
                AELog.info("Newer version is available: " + ghFormatted + " (found) > " + modFormatted + " (current)");

                if (this.config.shouldPostChangelog()) {
                    AELog.info("Changelog: " + changelog);
                }
            }

            this.interactWithVersionCheckerMod(modFormatted, ghFormatted, changelog);
        } else {
            AELog.info("No newer version is available: " + ghFormatted + "(found) < " + modFormatted + " (current)");
        }
    }

    /**
     * Checks if the version checker mod is installed and handles it depending on that information
     *
     * @param modFormatted mod version formatted as rv2-beta-8
     * @param ghFormatted  retrieved github version formatted as rv2-beta-8
     * @param changelog    retrieved github changelog
     */
    private void interactWithVersionCheckerMod(@Nonnull final String modFormatted, @Nonnull final String ghFormatted, @Nonnull final String changelog) {
        if (Loader.isModLoaded("VersionChecker")) {
            final NBTTagCompound versionInf = new NBTTagCompound();
            versionInf.setString("modDisplayName", AppEng.MOD_NAME);
            versionInf.setString("oldVersion", modFormatted);
            versionInf.setString("newVersion", ghFormatted);
            versionInf.setString("updateUrl", "http://ae-mod.info/builds/appliedenergistics2-" + ghFormatted + ".jar");
            versionInf.setBoolean("isDirectLink", true);

            if (!changelog.isEmpty()) {
                versionInf.setString("changeLog", changelog);
            }

            versionInf.setString("newFileName", "appliedenergistics2-" + ghFormatted + ".jar");
            FMLInterModComms.sendRuntimeMessage(AppEng.instance(), "VersionChecker", "addUpdate", versionInf);

            AELog.info("Reported new version to VersionChecker mod.");
        } else {
            AELog.info("VersionChecker mod is not installed; Proceeding.");
        }
    }
}
