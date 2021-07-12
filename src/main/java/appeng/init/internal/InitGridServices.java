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

package appeng.init.internal;

import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.core.Api;
import appeng.me.service.CraftingGridService;
import appeng.me.service.EnergyGridService;
import appeng.me.service.GridStorageService;
import appeng.me.service.P2PService;
import appeng.me.service.PathGridService;
import appeng.me.service.SecurityService;
import appeng.me.service.SpatialPylonService;
import appeng.me.service.StatisticsService;
import appeng.me.service.TickManagerService;

public final class InitGridServices {
    private InitGridServices() {
    }

    public static void init() {

        var gcr = Api.INSTANCE.registries().gridService();
        gcr.register(ITickManager.class, TickManagerService.class);
        gcr.register(IPathingGrid.class, PathGridService.class);
        gcr.register(IEnergyGrid.class, EnergyGridService.class);
        gcr.register(IStorageGrid.class, GridStorageService.class);
        gcr.register(P2PService.class, P2PService.class);
        gcr.register(ISpatialService.class, SpatialPylonService.class);
        gcr.register(ISecurityGrid.class, SecurityService.class);
        gcr.register(ICraftingGrid.class, CraftingGridService.class);
        gcr.register(StatisticsService.class, StatisticsService.class);
    }
}
