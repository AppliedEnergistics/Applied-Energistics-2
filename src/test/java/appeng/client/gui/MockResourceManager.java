/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.gui;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.mockito.Mockito;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;

import appeng.core.AppEng;

/**
 * Fake resource manager that more or less loads AE2 resource pack resources.
 */
public final class MockResourceManager {
    private MockResourceManager() {
    }

    public static ReloadableResourceManager create() {

        var testResourceBasePath = AppEng.class.getResource("/META-INF/neoforge.mods.toml");
        if (testResourceBasePath == null) {
            throw new IllegalStateException("Couldn't find root of assets");
        }

        Path assetRootPath;
        try {
            assetRootPath = Paths.get(testResourceBasePath.toURI()).getParent().getParent();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to convert asset root to a path on disk. (" + testResourceBasePath + ")");
        }

        var packResources = new PathPackResources(
                new PackLocationInfo("ae2", Component.literal("AE2"), PackSource.BUILT_IN, Optional.empty()),
                assetRootPath);

        ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
        resourceManager.createReload(Runnable::run, Runnable::run, CompletableFuture.supplyAsync(() -> Unit.INSTANCE),
                List.of(
                        ServerPacksSource.createVanillaPackSource(),
                        packResources));
        return Mockito.spy(resourceManager);
    }
}
