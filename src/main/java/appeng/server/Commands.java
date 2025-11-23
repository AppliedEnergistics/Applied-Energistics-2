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

import net.minecraft.server.permissions.PermissionCheck;

import appeng.server.services.compass.TestCompassCommand;
import appeng.server.subcommands.ChannelModeCommand;
import appeng.server.subcommands.ChunkLogger;
import appeng.server.subcommands.GridsCommand;
import appeng.server.subcommands.SetupTestWorldCommand;
import appeng.server.subcommands.SpatialStorageCommand;
import appeng.server.subcommands.TestMeteoritesCommand;
import appeng.server.subcommands.TickMonitoring;

public enum Commands {
    // Admin
    CHUNK_LOGGER(net.minecraft.commands.Commands.LEVEL_OWNERS, "chunklogger", new ChunkLogger()),
    SPATIAL(net.minecraft.commands.Commands.LEVEL_OWNERS, "spatial", new SpatialStorageCommand()),
    CHANNEL_MODE(net.minecraft.commands.Commands.LEVEL_OWNERS, "channelmode", new ChannelModeCommand()),
    TICK_MONITORING(net.minecraft.commands.Commands.LEVEL_OWNERS, "tickmonitor", new TickMonitoring()),
    GRIDS(net.minecraft.commands.Commands.LEVEL_OWNERS, "grids", new GridsCommand()),

    // Testing
    COMPASS(net.minecraft.commands.Commands.LEVEL_OWNERS, "compass", new TestCompassCommand(), true),
    TEST_METEORITES(net.minecraft.commands.Commands.LEVEL_OWNERS, "testmeteorites", new TestMeteoritesCommand(), true),
    SETUP_TEST_WORLD(net.minecraft.commands.Commands.LEVEL_OWNERS, "setuptestworld", new SetupTestWorldCommand(), true);

    public final PermissionCheck requiredPermission;
    public final ISubCommand command;
    public final boolean test;
    public final String literal;

    Commands(PermissionCheck requiredPermission, String literal, ISubCommand w) {
        this(requiredPermission, literal, w, false);
    }

    Commands(PermissionCheck requiredPermission, String literal, ISubCommand w, boolean test) {
        this.requiredPermission = requiredPermission;
        this.command = w;
        this.test = test;
        this.literal = literal;
    }

    public String literal() {
        return literal.toLowerCase(Locale.ROOT);
    }

}
