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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.Platform;

public final class AELog {
    private static final String LOGGER_PREFIX = "AE2:";
    private static final String SERVER_SUFFIX = "S";
    private static final String CLIENT_SUFFIX = "C";

    private static final Logger SERVER = LogManager.getFormatterLogger(LOGGER_PREFIX + SERVER_SUFFIX);
    private static final Logger CLIENT = LogManager.getFormatterLogger(LOGGER_PREFIX + CLIENT_SUFFIX);

    private static final String BLOCK_UPDATE = "Block Update of %s @ ( %s ). State %s -> %s";

    private static final String DEFAULT_EXCEPTION_MESSAGE = "Exception: ";

    private static boolean craftingLogEnabled;
    private static boolean debugLogEnabled;
    private static boolean gridLogEnabled;

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
        return true;
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
    public static void log(Level level, String message, Object... params) {
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
    public static void log(Level level, Throwable exception, String message,
            Object... params) {
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
    public static void info(String format, Object... params) {
        log(Level.INFO, format, params);
    }

    /**
     * Log exception as {@link Level#INFO}
     *
     * @param exception
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void info(Throwable exception) {
        log(Level.INFO, exception, DEFAULT_EXCEPTION_MESSAGE);
    }

    /**
     * Log exception as {@link Level#INFO}
     *
     * @param exception
     * @param message
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void info(Throwable exception, String message) {
        log(Level.INFO, exception, message);
    }

    /**
     * @param format
     * @param params
     * @see AELog#log(Level, String, Object...)
     */
    public static void warn(String format, Object... params) {
        log(Level.WARN, format, params);
    }

    /**
     * Log exception as {@link Level#WARN}
     *
     * @param exception
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void warn(Throwable exception) {
        log(Level.WARN, exception, DEFAULT_EXCEPTION_MESSAGE);
    }

    /**
     * Log exception as {@link Level#WARN}
     *
     * @param exception
     * @param message
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void warn(Throwable exception, String message) {
        log(Level.WARN, exception, message);
    }

    /**
     * @param format
     * @param params
     * @see AELog#log(Level, String, Object...)
     */
    public static void error(String format, Object... params) {
        log(Level.ERROR, format, params);
    }

    /**
     * Log exception as {@link Level#ERROR}
     *
     * @param exception
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void error(Throwable exception) {
        log(Level.ERROR, exception, DEFAULT_EXCEPTION_MESSAGE);
    }

    /**
     * Log exception as {@link Level#ERROR}
     *
     * @param exception
     * @param message
     * @see AELog#log(Level, Throwable, String, Object...)
     */
    public static void error(Throwable exception, String message) {
        log(Level.ERROR, exception, message);
    }

    /**
     * Log message as {@link Level#DEBUG}
     *
     * @param format
     * @param data
     * @see AELog#log(Level, String, Object...)
     */
    public static void debug(String format, Object... data) {
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
    public static void debug(Throwable exception) {
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
    public static void debug(Throwable exception, String message) {
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
        return debugLogEnabled;
    }

    //
    // Specialized handlers
    //

    /**
     * Logging of block updates.
     * <p>
     * Off by default, can be enabled inside the configuration file.
     *
     * @param pos
     * @param currentState
     * @param newState
     * @param blockEntity
     * @see AELog#log(Level, String, Object...)
     */
    public static void blockUpdate(BlockPos pos, BlockState currentState,
            BlockState newState, AEBaseBlockEntity blockEntity) {
        if (AEConfig.instance().isBlockUpdateLogEnabled()) {
            info(BLOCK_UPDATE, blockEntity.getClass().getName(), pos, currentState, newState);
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
        return craftingLogEnabled;
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
    public static void crafting(String message, Object... params) {
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
        return isCraftingLogEnabled() && isDebugLogEnabled();
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
    public static void craftingDebug(String message, Object... params) {
        if (AELog.isCraftingDebugLogEnabled()) {
            log(Level.DEBUG, message, params);
        }
    }

    /**
     * Use to check for an enabled grid log.
     * <p>
     * Can be used to prevent the execution of unneeded logic.
     *
     * @return true when the grid log is enabled.
     */
    public static boolean isGridLogEnabled() {
        return gridLogEnabled;
    }

    /**
     * Logging for grid and grid node structure changes.
     * <p>
     * Off by default, can be enabled inside the configuration file.
     *
     * @see AELog#log(Level, String, Object...)
     */
    public static void grid(String message, Object... params) {
        if (AELog.isGridLogEnabled()) {
            log(Level.INFO, "[AE2 Grid Log] " + message, params);
        }
    }

    public static void setCraftingLogEnabled(boolean newValue) {
        craftingLogEnabled = newValue;
    }

    public static void setDebugLogEnabled(boolean newValue) {
        debugLogEnabled = newValue;
    }

    public static void setGridLogEnabled(boolean newValue) {
        gridLogEnabled = newValue;
    }
}
