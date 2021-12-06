package appeng.api.lookup;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

/**
 * AE's platform-independent version of Fabric's BlockApiCache.
 */
public final class AEApiCache<A> {
    private final BlockApiCache<A, Direction> cache;
    private final Direction side;

    AEApiCache(BlockApiLookup<A, Direction> lookup, ServerLevel level, BlockPos pos, Direction side) {
        this.cache = BlockApiCache.create(lookup, level, pos);
        this.side = side;
    }

    @Nullable
    public A find() {
        return cache.find(side);
    }
}
