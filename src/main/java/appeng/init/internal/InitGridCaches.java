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
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.core.Api;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cache.EnergyGridCache;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.P2PCache;
import appeng.me.cache.PathGridCache;
import appeng.me.cache.SecurityCache;
import appeng.me.cache.SpatialPylonCache;
import appeng.me.cache.StatisticsCache;
import appeng.me.cache.TickManagerCache;

import java.util.function.Function;

public final class InitGridCaches {
    private InitGridCaches() {
    }

    public static void init() {

        var gcr = Api.INSTANCE.registries().gridCache();
        gcr.registerGridCache(ITickManager.class, TickManagerCache.class);
        gcr.registerGridCache(IPathingGrid.class, PathGridCache.class);
        gcr.registerGridCache(IEnergyGrid.class, EnergyGridCache.class);
        gcr.registerGridCache(IStorageGrid.class, GridStorageCache.class);
        gcr.registerGridCache(P2PCache.class, P2PCache.class);
        gcr.registerGridCache(ISpatialCache.class, SpatialPylonCache.class);
        gcr.registerGridCache(ISecurityGrid.class, SecurityCache.class);
        gcr.registerGridCache(ICraftingGrid.class, CraftingGridCache.class);
        gcr.registerGridCache(StatisticsCache.class, StatisticsCache.class);
    }
}
