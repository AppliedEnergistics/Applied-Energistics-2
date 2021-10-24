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

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import appeng.init.InitAdvancementTriggers;
import appeng.init.InitLootConditions;
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
            InitGridServices.init();
            InitBlockEntityMoveStrategies.init();

            // This has to be initialized here because Forge's common setup event will not run in datagens.
            InitStats.init();
            InitAdvancementTriggers.init();
            InitLootConditions.init();
        }
    }

}
