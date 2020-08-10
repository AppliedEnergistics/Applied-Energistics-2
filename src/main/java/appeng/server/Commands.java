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

import appeng.server.subcommands.ChunkLogger;
import appeng.server.subcommands.SpatialStorageCommand;
import appeng.server.subcommands.Supporters;
import appeng.server.subcommands.TestMeteoritesCommand;
import appeng.server.subcommands.TestOreGenCommand;

public enum Commands {
    Chunklogger(4, new ChunkLogger(), false), Supporters(0, new Supporters(), false),
    TestOreGen(4, new TestOreGenCommand(), true), TestMeteorites(4, new TestMeteoritesCommand(), true),
    Spatial(4, new SpatialStorageCommand(), false);

    public final int level;
    public final ISubCommand command;
    public boolean test;

    Commands(final int level, final ISubCommand w, boolean test) {
        this.level = level;
        this.command = w;
        this.test = test;
    }

    @Override
    public String toString() {
        return this.name();
    }

}
