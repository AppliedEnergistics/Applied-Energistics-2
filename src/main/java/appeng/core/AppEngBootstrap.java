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

package appeng.core;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.init.InitAdvancementTriggers;
import appeng.init.InitStats;
import appeng.init.internal.InitBlockEntityMoveStrategies;
import appeng.init.internal.InitGridServices;

/**
 * This class is just responsible for initializing the client or server-side mod class.
 */
@Mod(AppEng.MOD_ID)
public class AppEngBootstrap {
    private volatile static boolean bootstrapped;

    public AppEngBootstrap() {
        DistExecutor.unsafeRunForDist(() -> AppEngClient::new, () -> AppEngServer::new);
    }

    /**
     * This is called from a Mixin whenever Minecraft itself Bootstraps.
     */
    public static void runEarlyStartup() {
        if (!bootstrapped) {
            bootstrapped = true;

            AEConfig.load(FMLPaths.CONFIGDIR.get());

            InitGridServices.init();
            InitBlockEntityMoveStrategies.init();

            // Forge will do this later, but we need it now.
            Registry.REGISTRY.stream().forEach(r -> {
                if (r instanceof MappedRegistry mappedRegistry) {
                    mappedRegistry.unfreeze();
                }
            });

            // This has to be initialized here because Forge's common setup event will not run in datagens.
            InitStats.init();
            InitAdvancementTriggers.init();

            CreativeTab.init();

            // Initialize items in order
            // We have to move this here from our mod constructor because item constructors now interact with
            // Registries in a non-thread-safe way.
            AEItems.init();
            AEBlocks.init();
            AEParts.init();
        }
    }

}
