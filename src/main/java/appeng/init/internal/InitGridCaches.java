package appeng.init.internal;

import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridCacheRegistry;
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

public final class InitGridCaches {
    private InitGridCaches() {
    }

    public static void init() {
        final IRegistryContainer registries = Api.INSTANCE.registries();

        final IGridCacheRegistry gcr = registries.gridCache();
        gcr.registerGridCache(ITickManager.class, TickManagerCache::new);
        gcr.registerGridCache(IEnergyGrid.class, EnergyGridCache::new);
        gcr.registerGridCache(IPathingGrid.class, PathGridCache::new);
        gcr.registerGridCache(IStorageGrid.class, GridStorageCache::new);
        gcr.registerGridCache(P2PCache.class, P2PCache::new);
        gcr.registerGridCache(ISpatialCache.class, SpatialPylonCache::new);
        gcr.registerGridCache(ISecurityGrid.class, SecurityCache::new);
        gcr.registerGridCache(ICraftingGrid.class, CraftingGridCache::new);
        gcr.registerGridCache(StatisticsCache.class, StatisticsCache::new);
    }
}
