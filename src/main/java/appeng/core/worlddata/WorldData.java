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


import appeng.core.AEConfig;
import appeng.services.CompassService;
import appeng.services.compass.CompassThreadFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadFactory;


/**
 * Singleton access to anything related to world-based data.
 * <p>
 * Data will change depending which world is loaded. Will probably not affect SMP at all since only one world is loaded,
 * but SSP more, cause they play on
 * different worlds.
 *
 * @author thatsIch
 * @version rv3 - 02.11.2015
 * @since rv3 30.05.2015
 */
public final class WorldData implements IWorldData {
    private static final String AE2_DIRECTORY_NAME = "AE2";
    private static final String SETTING_FILE_NAME = "settings.cfg";
    private static final String SPAWNDATA_DIR_NAME = "spawndata";
    private static final String COMPASS_DIR_NAME = "compass";

    @Nullable
    private static IWorldData instance;

    private final IWorldPlayerData playerData;
    private final IWorldGridStorageData storageData;
    private final IWorldCompassData compassData;
    private final IWorldSpawnData spawnData;

    private final List<IOnWorldStartable> startables;
    private final List<IOnWorldStoppable> stoppables;

    private final File ae2directory;
    private final File spawnDirectory;
    private final File compassDirectory;

    private final Configuration sharedConfig;

    private WorldData(@Nonnull final File worldDirectory) {
        Preconditions.checkNotNull(worldDirectory);
        Preconditions.checkArgument(worldDirectory.isDirectory());

        this.ae2directory = new File(worldDirectory, AE2_DIRECTORY_NAME);
        this.spawnDirectory = new File(this.ae2directory, SPAWNDATA_DIR_NAME);
        this.compassDirectory = new File(this.ae2directory, COMPASS_DIR_NAME);

        final File settingsFile = new File(this.ae2directory, SETTING_FILE_NAME);
        this.sharedConfig = new Configuration(settingsFile, AEConfig.VERSION);

        final PlayerData playerData = new PlayerData(this.sharedConfig);
        final StorageData storageData = new StorageData(this.sharedConfig);

        final ThreadFactory compassThreadFactory = new CompassThreadFactory();
        final CompassService compassService = new CompassService(this.compassDirectory, compassThreadFactory);
        final CompassData compassData = new CompassData(this.compassDirectory, compassService);

        final IWorldSpawnData spawnData = new SpawnData(this.spawnDirectory);

        this.playerData = playerData;
        this.storageData = storageData;
        this.compassData = compassData;
        this.spawnData = spawnData;

        this.startables = Lists.newArrayList(playerData, storageData);
        this.stoppables = Lists.newArrayList(playerData, storageData, compassData);
    }

    /**
     * @return ae2 data related to a specific world
     * @deprecated do not use singletons which are dependent on specific world state
     */
    @Deprecated
    @Nonnull
    public static IWorldData instance() {
        return instance;
    }

    /**
     * Requires to start up from external from here
     * <p>
     * drawback of the singleton build style
     *
     * @param server
     */
    public static void onServerAboutToStart(MinecraftServer server) {
        File worldDirectory = DimensionManager.getCurrentSaveRootDirectory();
        if (worldDirectory == null) {
            worldDirectory = server.getActiveAnvilConverter().getSaveLoader(server.getFolderName(), false).getWorldDirectory();
        }
        final WorldData newInstance = new WorldData(worldDirectory);

        instance = newInstance;
        newInstance.onServerStarting();
    }

    private void onServerStarting() {
        // check if ae2 folder already exists, else create
        if (!this.ae2directory.isDirectory() && !this.ae2directory.mkdir()) {
            throw new IllegalStateException("Failed to create " + this.ae2directory.getAbsolutePath());
        }

        // check if compass folder already exists, else create
        if (!this.compassDirectory.isDirectory() && !this.compassDirectory.mkdir()) {
            throw new IllegalStateException("Failed to create " + this.compassDirectory.getAbsolutePath());
        }

        // check if spawn data dir already exists, else create
        if (!this.spawnDirectory.isDirectory() && !this.spawnDirectory.mkdir()) {
            throw new IllegalStateException("Failed to create " + this.spawnDirectory.getAbsolutePath());
        }

        for (final IOnWorldStartable startable : this.startables) {
            startable.onWorldStart();
        }

        this.startables.clear();
    }

    @Override
    public void onServerStopping() {
        for (final IOnWorldStoppable stoppable : this.stoppables) {
            stoppable.onWorldStop();
        }
    }

    @Override
    public void onServerStoppped() {
        Preconditions.checkNotNull(instance);

        this.stoppables.clear();
        instance = null;
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

    @Nonnull
    @Override
    public IWorldSpawnData spawnData() {
        return this.spawnData;
    }
}
