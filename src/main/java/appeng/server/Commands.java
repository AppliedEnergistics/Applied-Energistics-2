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

package appeng.server;

import java.util.Locale;

import appeng.server.services.compass.TestCompassCommand;
import appeng.server.subcommands.ChannelModeCommand;
import appeng.server.subcommands.ChunkLogger;
import appeng.server.subcommands.GridsCommand;
import appeng.server.subcommands.ReloadConfigCommand;
import appeng.server.subcommands.SetupTestWorldCommand;
import appeng.server.subcommands.SpatialStorageCommand;
import appeng.server.subcommands.TestMeteoritesCommand;
import appeng.server.subcommands.TickMonitoring;

public enum Commands {
    RELOAD_CONFIG(4, "reloadconfig", new ReloadConfigCommand()),

    // Admin
    CHUNK_LOGGER(4, "chunklogger", new ChunkLogger()),
    SPATIAL(4, "spatial", new SpatialStorageCommand()),
    CHANNEL_MODE(4, "channelmode", new ChannelModeCommand()),
    TICK_MONITORING(4, "tickmonitor", new TickMonitoring()),
    GRIDS(4, "grids", new GridsCommand()),

    // Testing
    COMPASS(4, "compass", new TestCompassCommand(), true),
    TEST_METEORITES(4, "testmeteorites", new TestMeteoritesCommand(), true),
    SETUP_TEST_WORLD(4, "setuptestworld", new SetupTestWorldCommand(), true);

    public final int level;
    public final ISubCommand command;
    public final boolean test;
    public final String literal;

    Commands(int level, String literal, ISubCommand w) {
        this(level, literal, w, false);
    }

    Commands(int level, String literal, ISubCommand w, boolean test) {
        this.level = level;
        this.command = w;
        this.test = test;
        this.literal = literal;
    }

    public String literal() {
        return literal.toLowerCase(Locale.ROOT);
    }

}
