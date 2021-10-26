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

import appeng.api.networking.GridServices;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.pathing.IPathingService;
import appeng.api.networking.security.ISecurityService;
import appeng.api.networking.spatial.ISpatialService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.ITickManager;
import appeng.me.service.CraftingService;
import appeng.me.service.EnergyService;
import appeng.me.service.P2PService;
import appeng.me.service.PathingService;
import appeng.me.service.SecurityService;
import appeng.me.service.SpatialPylonService;
import appeng.me.service.StatisticsService;
import appeng.me.service.StorageService;
import appeng.me.service.TickManagerService;

public final class InitGridServices {
    private InitGridServices() {
    }

    public static void init() {
        GridServices.register(ITickManager.class, TickManagerService.class);
        GridServices.register(IPathingService.class, PathingService.class);
        GridServices.register(IEnergyService.class, EnergyService.class);
        GridServices.register(IStorageService.class, StorageService.class);
        GridServices.register(P2PService.class, P2PService.class);
        GridServices.register(ISpatialService.class, SpatialPylonService.class);
        GridServices.register(ISecurityService.class, SecurityService.class);
        GridServices.register(ICraftingService.class, CraftingService.class);
        GridServices.register(StatisticsService.class, StatisticsService.class);
    }
}
