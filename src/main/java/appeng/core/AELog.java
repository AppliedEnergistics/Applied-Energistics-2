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

package appeng.core;


import appeng.core.features.AEFeature;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import javax.annotation.Nonnull;


public final class AELog {
    private static final String LOGGER_PREFIX = "AE2:";
    private static final String SERVER_SUFFIX = "S";
    private static final String CLIENT_SUFFIX = "C";

    private static final Logger SERVER = LogManager.getFormatterLogger(LOGGER_PREFIX + SERVER_SUFFIX);
    private static final Logger CLIENT = LogManager.getFormatterLogger(LOGGER_PREFIX + CLIENT_SUFFIX);

    private static final String BLOCK_UPDATE = "Block Update of %s @ ( %s )";

    private static final String DEFAULT_EXCEPTION_MESSAGE = "Exception: ";

    private AELog() {
    }

    /**
     * Returns a {@link Logger} logger suitable for the effective side (client/server).
     *
     * @return a suitable logger instance
     */
    private static Logger getLogger() {
        return Platform.isServer() ? SERVER : CLIENT;
    }

    /**
     * Indicates of the global log is enabled or disabled.
     * <p>
     * By default it is enabled.
     *
     * @return true when the log is enabled.
     */
    public static boolean isLogEnabled() {
        return AEConfig.instance() == null || AEConfig.instance().isFeatureEnabled(AEFeature.LOGGING);
    }

    /**
     * Logs a formatted message with a specific log level.
     * <p>
     * This uses {@link String#format(String, Object...)} as opposed to the {@link ParameterizedMessage} to allow a more
     * flexible formatting.
     * <p>
     * The output can be globally disabled via the configuration file.
     *
     * @param level   the intended level.
     * @param message the message to be formatted.
     * @param params  the parameters used for {@link String#format(String, Object...)}.
     */
    public static void log(@Nonnull final Level level, @Nonnull final String message, final Object... params) {
        if (AELog.isLogEnabled()) {
            final String formattedMessage = String.format(message, params);
            final Logger logger = getLogger();

            logger.log(level, formattedMessage);
        }
    }

    /**
     * Log an exception with a custom message formated via {@link String#format(String, Object...)}
     * <p>
     * Similar to {@link AELog#log(Level, String, Object...)}.
     *
     * @param level     the intended level.
     * @param exception
     * @param message   the message to be formatted.
     * @param params    the parameters used for {@link String#format(String, Object...)}.
     * @see AELog#log(Level, String, Object...)
     */
    public static void log(@Nonnull final Level level, @Nonnull final Throwable exception, @Nonnull String message, final Object... params) {
        if (AELog.isLogEnabled()) {
            final String formattedMessage = String.format(message, params);
            final Logger logger = getLogger();

            logger.log(level, formattedMessage, exception);
        }
    }

    /**
     * @param format
     * @param params
     * @see AELog#log(Level, String, Object...)
     */
    public static void info(@Nonnull final String format, final Object... params) {
        log(Level.INFO, format, params);
    }

    /**
     * Log exception as {@link Level#INFO}
     *
     * @param exception
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void info(@Nonnull final Throwable exception) {
        log(Level.INFO, exception, DEFAULT_EXCEPTION_MESSAGE);
    }

    /**
     * Log exception as {@link Level#INFO}
     *
     * @param exception
     * @param message
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void info(@Nonnull final Throwable exception, @Nonnull final String message) {
        log(Level.INFO, exception, message);
    }

    /**
     * @param format
     * @param params
     * @see AELog#log(Level, String, Object...)
     */
    public static void warn(@Nonnull final String format, final Object... params) {
        log(Level.WARN, format, params);
    }

    /**
     * Log exception as {@link Level#WARN}
     *
     * @param exception
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void warn(@Nonnull final Throwable exception) {
        log(Level.WARN, exception, DEFAULT_EXCEPTION_MESSAGE);
    }

    /**
     * Log exception as {@link Level#WARN}
     *
     * @param exception
     * @param message
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void warn(@Nonnull final Throwable exception, @Nonnull final String message) {
        log(Level.WARN, exception, message);
    }

    /**
     * @param format
     * @param params
     * @see AELog#log(Level, String, Object...)
     */
    public static void error(@Nonnull final String format, final Object... params) {
        log(Level.ERROR, format, params);
    }

    /**
     * Log exception as {@link Level#ERROR}
     *
     * @param exception
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void error(@Nonnull final Throwable exception) {
        log(Level.ERROR, exception, DEFAULT_EXCEPTION_MESSAGE);
    }

    /**
     * Log exception as {@link Level#ERROR}
     *
     * @param exception
     * @param message
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void error(@Nonnull final Throwable exception, @Nonnull final String message) {
        log(Level.ERROR, exception, message);
    }

    /**
     * Log message as {@link Level#DEBUG}
     *
     * @param format
     * @param data
     * @see AELog#log(Level, String, Object...)
     */
    public static void debug(@Nonnull final String format, final Object... data) {
        if (AELog.isDebugLogEnabled()) {
            log(Level.DEBUG, format, data);
        }
    }

    /**
     * Log exception as {@link Level#DEBUG}
     *
     * @param exception
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void debug(@Nonnull final Throwable exception) {
        if (AELog.isDebugLogEnabled()) {
            log(Level.DEBUG, exception, DEFAULT_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Log exception as {@link Level#DEBUG}
     *
     * @param exception
     * @param message
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void debug(@Nonnull final Throwable exception, @Nonnull final String message) {
        if (AELog.isDebugLogEnabled()) {
            log(Level.DEBUG, exception, message);
        }
    }

    /**
     * Use to check for an enabled debug log.
     * <p>
     * Can be used to prevent the execution of debug logic.
     *
     * @return true when the debug log is enabled.
     */
    public static boolean isDebugLogEnabled() {
        return AEConfig.instance().isFeatureEnabled(AEFeature.DEBUG_LOGGING);
    }

    //
    // Specialized handlers
    //

    /**
     * A specialized logging for grinder recipes, can be disabled inside configuration file.
     *
     * @param message String to be logged
     */
    public static void grinder(@Nonnull final String message, final Object... params) {
        if (AEConfig.instance().isFeatureEnabled(AEFeature.GRINDER_LOGGING)) {
            log(Level.DEBUG, "grinder: " + message, params);
        }
    }

    /**
     * A specialized logging for mod integration errors, can be disabled inside configuration file.
     *
     * @param exception
     */
    public static void integration(@Nonnull final Throwable exception) {
        if (AEConfig.instance().isFeatureEnabled(AEFeature.INTEGRATION_LOGGING)) {
            debug(exception);
        }
    }

    /**
     * Logging of block updates.
     * <p>
     * Off by default, can be enabled inside the configuration file.
     *
     * @param pos
     * @param aeBaseTile
     * @see AELog#log(Level, String, Object...)
     */
    public static void blockUpdate(@Nonnull final BlockPos pos, @Nonnull final AEBaseTile aeBaseTile) {
        if (AEConfig.instance().isFeatureEnabled(AEFeature.UPDATE_LOGGING)) {
            info(BLOCK_UPDATE, aeBaseTile.getClass().getName(), pos);
        }
    }

    /**
     * Use to check for an enabled crafting log.
     * <p>
     * Can be used to prevent the execution of unneeded logic.
     *
     * @return true when the crafting log is enabled.
     */
    public static boolean isCraftingLogEnabled() {
        return AEConfig.instance().isFeatureEnabled(AEFeature.CRAFTING_LOG);
    }

    /**
     * Logging for autocrafting.
     * <p>
     * Off by default, can be enabled inside the configuration file.
     *
     * @param message
     * @param params
     * @see AELog#log(Level, String, Object...)
     */
    public static void crafting(@Nonnull final String message, final Object... params) {
        if (AELog.isCraftingLogEnabled()) {
            log(Level.INFO, message, params);
        }
    }

    /**
     * Use to check for an enabled crafting debug log.
     * <p>
     * Can be used to prevent the execution of unneeded logic.
     *
     * @return true when the crafting debug log is enabled.
     */
    public static boolean isCraftingDebugLogEnabled() {
        return AEConfig.instance().isFeatureEnabled(AEFeature.CRAFTING_LOG) && AEConfig.instance().isFeatureEnabled(AEFeature.DEBUG_LOGGING);
    }

    /**
     * Debug logging for autocrafting.
     * <p>
     * Off by default, can be enabled inside the configuration file.
     *
     * @param message
     * @param params
     * @see AELog#log(Level, String, Object...)
     */
    public static void craftingDebug(@Nonnull final String message, final Object... params) {
        if (AELog.isCraftingDebugLogEnabled()) {
            log(Level.DEBUG, message, params);
        }
    }
}
