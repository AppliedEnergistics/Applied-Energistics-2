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

package appeng.core.worlddata;

import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;

import appeng.services.CompassService;
import appeng.services.compass.CompassThreadFactory;

/**
 * Singleton access to anything related to world-based data.
 *
 * Data will change depending which world is loaded. Will probably not affect
 * SMP at all since only one world is loaded, but SSP more, cause they play on
 * different worlds.
 *
 * @author thatsIch
 * @version rv3 - 02.11.2015
 * @since rv3 30.05.2015
 */
public final class WorldData implements IWorldData {

    /**
     * Is null while no MinecraftServer exists.
     */
    @Nullable
    private static IWorldData instance;

    @Nullable
    private static MinecraftServer server;

    private final IWorldPlayerData playerData;
    private final IWorldGridStorageData storageData;
    private final IWorldCompassData compassData;

    private WorldData(@Nonnull final ServerWorld overworld) {
        Preconditions.checkNotNull(overworld);

        // Attach shared data to the server's overworld dimension
        if (overworld.getDimensionKey() != ServerWorld.OVERWORLD) {
            throw new IllegalStateException("The server doesn't have an overworld we could store our data on!");
        }

        final PlayerData playerData = overworld.getSavedData().getOrCreate(PlayerData::new, PlayerData.NAME);
        final StorageData storageData = overworld.getSavedData().getOrCreate(StorageData::new, StorageData.NAME);

        final ThreadFactory compassThreadFactory = new CompassThreadFactory();
        final CompassService compassService = new CompassService(server, compassThreadFactory);
        final CompassData compassData = new CompassData(compassService);

        this.playerData = playerData;
        this.storageData = storageData;
        this.compassData = compassData;

    }

    /**
     * @return ae2 data related to a specific world
     *
     * @deprecated do not use singletons which are dependent on specific world state
     */
    @Deprecated
    @Nonnull
    public synchronized static IWorldData instance() {
        // The overworld is lazily loaded, meaning we cannot access it right away
        // when the server is starting, but the first time the instance is accessed,
        // we create the actual world data
        if (instance == null) {
            if (server == null) {
                throw new IllegalStateException("No server set.");
            }

            ServerWorld overworld = server.getWorld(ServerWorld.OVERWORLD);
            instance = new WorldData(overworld);
        }
        return instance;
    }

    /**
     * Requires to start up from external from here
     *
     * drawback of the singleton build style
     */
    public static void onServerStarting(MinecraftServer server) {
        WorldData.server = server;
    }

    @Override
    public void onServerStopping() {
        compassData.service().kill();
    }

    @Override
    public void onServerStoppped() {
        Preconditions.checkNotNull(server);
        instance = null;
        WorldData.server = null;
    }

    @Nonnull
    @Override
    public IWorldGridStorageData storageData() {
        return this.storageData;
    }

    @Nonnull
    @Override
    public IWorldPlayerData playerData() {
        return this.playerData;
    }

    @Nonnull
    @Override
    public IWorldCompassData compassData() {
        return this.compassData;
    }
}
